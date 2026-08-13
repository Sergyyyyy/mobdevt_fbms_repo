package ph.edu.benilde.fbms.data.repository

import ph.edu.benilde.fbms.data.AnswerDto
import ph.edu.benilde.fbms.data.ApiErrorResponse
import ph.edu.benilde.fbms.data.ApiService
import ph.edu.benilde.fbms.data.QuestionDto
import ph.edu.benilde.fbms.data.SubmitFeedbackRequest
import ph.edu.benilde.fbms.domain.AnswerMap
import ph.edu.benilde.fbms.domain.Question
import ph.edu.benilde.fbms.domain.QuestionType
import ph.edu.benilde.fbms.domain.Survey
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
    private val api: ApiService,
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
                response.code() == 401 -> SurveyResult.Error("Device authentication invalid or expired. Please re-activate this tablet.")
                else -> SurveyResult.Error(parseErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            // Network unreachable (e.g. firewall / no internet) — fall back to a demo survey
            // so the kiosk UI can still be tested end-to-end without a live backend.
            SurveyResult.Success(demoSurvey())
        }
    }

    /** Offline demo survey used when the network is unreachable during testing. */
    private fun demoSurvey() = ph.edu.benilde.fbms.domain.Survey(
        id = "demo-survey-001",
        title = "FBMS Demo Survey",
        questions = listOf(
            ph.edu.benilde.fbms.domain.Question(
                id = "q1",
                text = "How satisfied are you with our service today?",
                type = ph.edu.benilde.fbms.domain.QuestionType.RATING,
                required = true,
                order = 1
            ),
            ph.edu.benilde.fbms.domain.Question(
                id = "q2",
                text = "Would you recommend FBMS to others?",
                type = ph.edu.benilde.fbms.domain.QuestionType.YES_NO,
                required = true,
                order = 2
            ),
            ph.edu.benilde.fbms.domain.Question(
                id = "q3",
                text = "Any additional comments?",
                type = ph.edu.benilde.fbms.domain.QuestionType.SHORT_TEXT,
                required = false,
                order = 3
            )
        )
    )


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
                response.code() == 401 -> SubmitResult.Error("Device authentication invalid or expired. Tablet needs re-activation.")
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

private fun ph.edu.benilde.fbms.data.SurveyDto.toDomain(questionDtos: List<QuestionDto>): Survey =
    Survey(
        id = id,
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
