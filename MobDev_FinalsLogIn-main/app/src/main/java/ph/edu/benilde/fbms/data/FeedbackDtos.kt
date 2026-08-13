package ph.edu.benilde.fbms.data

import com.google.gson.annotations.SerializedName

// ---------- GET /mobile/survey ----------

data class SurveyResponse(
    val success: Boolean,
    val message: String,
    val data: SurveyResponseData
)

data class SurveyResponseData(
    val survey: SurveyDto,
    val questions: List<QuestionDto>
)

data class SurveyDto(
    @SerializedName("_id") val id: String,
    val title: String,
    val assignmentType: String,
    val status: String,
    val questionCount: Int
)

data class QuestionDto(
    @SerializedName("_id") val id: String,
    val questionText: String,
    val questionType: String, // "rating" | "yes_no" | "multiple_choice" | "short_text" | "long_text"
    val required: Boolean,
    val order: Int,
    val options: List<String> = emptyList()
)

// ---------- POST /mobile/feedback ----------

data class SubmitFeedbackRequest(
    val surveyId: String,
    val submittedAt: String, // ISO-8601, e.g. Instant.now().toString()
    val completedAt: String,
    val answers: List<AnswerDto>
)

data class AnswerDto(
    val questionId: String,
    val answer: Any
)

data class SubmitFeedbackResponse(
    val success: Boolean,
    val message: String,
    val data: SubmitFeedbackResponseData
)

data class SubmitFeedbackResponseData(
    val feedbackSession: Map<String, Any?>?,
    val answers: List<Map<String, Any?>>?,
    val referenceCode: String
)

// ---------- Shared error envelope (if different from ActivationEnvelope) ----------

data class ApiErrorResponse(
    val success: Boolean,
    val message: String,
    val errors: List<ApiFieldError> = emptyList()
)
