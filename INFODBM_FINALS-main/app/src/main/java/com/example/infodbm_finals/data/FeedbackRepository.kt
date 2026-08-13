package com.example.infodbm_finals.data.repository

import com.example.infodbm_finals.data.api.AnswerDto
import com.example.infodbm_finals.data.api.ApiErrorResponse
import com.example.infodbm_finals.data.api.FeedbackApiService
import com.example.infodbm_finals.data.api.QuestionDto
import com.example.infodbm_finals.data.api.SubmitFeedbackRequest
import com.example.infodbm_finals.domain.AnswerMap
import com.example.infodbm_finals.domain.Question
import com.example.infodbm_finals.domain.QuestionType
import com.example.infodbm_finals.domain.Survey
import com.google.gson.Gson
import java.time.Instant

sealed class SurveyResult {
    data class Success(val survey: Survey) : SurveyResult()
    object NoSurveyPublished : SurveyResult()       // 404
    data class Error(val message: String) : SurveyResult() // network / 5xx / unexpected
}

sealed class SubmitResult {
    data class Success(val referenceCode: String) : SubmitResult()
    object SurveyChanged : SubmitResult()           // 409 — caller should re-fetch GET /survey
    data class ValidationFailed(val message: String) : SubmitResult() // 400
    data class Error(val message: String) : SubmitResult()
}

class FeedbackRepository(
    private val api: FeedbackApiService,
    private val gson: Gson = Gson()
) {

    suspend fun getActiveSurvey(): SurveyResult {
        return try {
            val response = api.getActiveSurvey()
            when {
                response.isSuccessful -> {
                    val body = response.body()?.data
                        ?: return SurveyResult.Error("Empty response from server.")
                    SurveyResult.Success(body.survey.toDomain(body.questions))
                }
                response.code() == 404 -> SurveyResult.NoSurveyPublished
                else -> SurveyResult.Error(parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            SurveyResult.Error(e.message ?: "Network error while fetching survey.")
        }
    }

    suspend fun submitFeedback(
        surveyId: String,
        answers: AnswerMap,
        sessionStartedAt: Instant
    ): SubmitResult {
        val body = SubmitFeedbackRequest(
            surveyId = surveyId,
            submittedAt = sessionStartedAt.toString(),
            completedAt = Instant.now().toString(),
            answers = answers.map { (questionId, value) ->
                AnswerDto(questionId = questionId, answer = value.toRawValue())
            }
        )

        return try {
            val response = api.submitFeedback(body)
            when {
                response.isSuccessful -> {
                    val referenceCode = response.body()?.data?.referenceCode
                        ?: return SubmitResult.Error("Empty response from server.")
                    SubmitResult.Success(referenceCode)
                }
                response.code() == 409 -> SubmitResult.SurveyChanged
                response.code() == 400 -> SubmitResult.ValidationFailed(
                    parseErrorMessage(response.errorBody()?.string())
                )
                else -> SubmitResult.Error(parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            SubmitResult.Error(e.message ?: "Network error while submitting feedback.")
        }
    }

    private fun parseErrorMessage(rawBody: String?): String {
        if (rawBody.isNullOrBlank()) return "Something went wrong. Please try again."
        return try {
            gson.fromJson(rawBody, ApiErrorResponse::class.java).message
        } catch (e: Exception) {
            "Something went wrong. Please try again."
        }
    }
}

// ---------- DTO -> domain mapping ----------

private fun com.example.infodbm_finals.data.api.SurveyDto.toDomain(questionDtos: List<QuestionDto>): Survey =
    Survey(
        id = "", // NOTE: the abridged sample response doesn't show a survey id field.
                 // Confirm the real field name with your backend team (likely "_id")
                 // and wire it through SurveyDto — surveyId is REQUIRED on submit.
        title = title,
        questions = questionDtos
            .sortedBy { it.order }
            .map { it.toDomain() }
    )

private fun QuestionDto.toDomain(): Question =
    Question(
        id = id,
        text = questionText,
        type = when (questionType) {
            "rating" -> QuestionType.RATING
            "yes_no" -> QuestionType.YES_NO
            "multiple_choice" -> QuestionType.MULTIPLE_CHOICE
            "short_text" -> QuestionType.SHORT_TEXT
            "long_text" -> QuestionType.LONG_TEXT
            else -> QuestionType.SHORT_TEXT // defensive fallback; shouldn't happen per API contract
        },
        required = required,
        order = order,
        options = options
    )
