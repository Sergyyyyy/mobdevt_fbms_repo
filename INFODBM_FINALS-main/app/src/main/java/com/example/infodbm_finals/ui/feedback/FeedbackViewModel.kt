package com.example.infodbm_finals.ui.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.infodbm_finals.data.repository.FeedbackRepository
import com.example.infodbm_finals.data.repository.SubmitResult
import com.example.infodbm_finals.data.repository.SurveyResult
import com.example.infodbm_finals.domain.AnswerMap
import com.example.infodbm_finals.domain.AnswerValue
import com.example.infodbm_finals.domain.Survey
import com.example.infodbm_finals.domain.unansweredRequiredQuestions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

sealed class FeedbackUiState {
    object Loading : FeedbackUiState()
    object NoSurveyAvailable : FeedbackUiState() // 404 waiting-room state
    data class Error(val message: String) : FeedbackUiState()
    data class InProgress(
        val survey: Survey,
        val answers: AnswerMap = emptyMap(),
        val missingRequired: Set<String> = emptySet(), // question ids flagged after a failed submit attempt
        val isSubmitting: Boolean = false,
        val submitError: String? = null
    ) : FeedbackUiState()
    data class Submitted(val referenceCode: String) : FeedbackUiState()
    // Shown when POST /feedback returns 409 — the survey changed mid-session.
    object SurveyChangedMidSession : FeedbackUiState()
}

class FeedbackViewModel(
    private val repository: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedbackUiState>(FeedbackUiState.Loading)
    val uiState: StateFlow<FeedbackUiState> = _uiState

    private var sessionStartedAt: Instant = Instant.now()

    init {
        loadSurvey()
    }

    /** Call on first load, and again to start a fresh session after a submission or a 409. */
    fun loadSurvey() {
        _uiState.update { FeedbackUiState.Loading }
        sessionStartedAt = Instant.now()
        viewModelScope.launch {
            when (val result = repository.getActiveSurvey()) {
                is SurveyResult.Success ->
                    _uiState.update { FeedbackUiState.InProgress(survey = result.survey) }
                is SurveyResult.NoSurveyPublished ->
                    _uiState.update { FeedbackUiState.NoSurveyAvailable }
                is SurveyResult.Error ->
                    _uiState.update { FeedbackUiState.Error(result.message) }
            }
        }
    }

    fun setAnswer(questionId: String, value: AnswerValue) {
        val current = _uiState.value as? FeedbackUiState.InProgress ?: return
        _uiState.update {
            current.copy(
                answers = current.answers + (questionId to value),
                missingRequired = current.missingRequired - questionId
            )
        }
    }

    fun submit() {
        val current = _uiState.value as? FeedbackUiState.InProgress ?: return

        val missing = current.survey.unansweredRequiredQuestions(current.answers)
        if (missing.isNotEmpty()) {
            _uiState.update {
                current.copy(missingRequired = missing.map { it.id }.toSet())
            }
            return
        }

        _uiState.update { current.copy(isSubmitting = true, submitError = null) }

        viewModelScope.launch {
            when (val result = repository.submitFeedback(
                surveyId = current.survey.id,
                answers = current.answers,
                sessionStartedAt = sessionStartedAt
            )) {
                is SubmitResult.Success ->
                    _uiState.update { FeedbackUiState.Submitted(result.referenceCode) }

                // Per the API's own guidance: never blindly retry. Ask the visitor to redo it.
                is SubmitResult.SurveyChanged ->
                    _uiState.update { FeedbackUiState.SurveyChangedMidSession }

                is SubmitResult.ValidationFailed ->
                    _uiState.update {
                        current.copy(isSubmitting = false, submitError = result.message)
                    }

                is SubmitResult.Error ->
                    _uiState.update {
                        current.copy(isSubmitting = false, submitError = result.message)
                    }
            }
        }
    }
}
