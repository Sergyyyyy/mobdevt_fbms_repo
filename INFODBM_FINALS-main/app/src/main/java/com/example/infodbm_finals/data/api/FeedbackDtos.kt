package com.example.infodbm_finals.data.api

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
    val title: String,
    val assignmentType: String,
    val status: String,
    val questionCount: Int
)

data class QuestionDto(
    // The API doesn't show an explicit questionId field name in the abridged
    // sample, but /feedback requires answers[].questionId to match a question
    // from this survey — adjust the @SerializedName below if your backend's
    // actual field name differs (e.g. "_id" if Mongo docs are returned raw).
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

// "answer" is intentionally Any? here — the caller must put an Int, Boolean,
// or String matching the question's type. Gson will serialize each correctly.
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
    val feedbackSession: Map<String, Any?>, // not modeled in detail — unused by the kiosk UI
    val answers: List<Map<String, Any?>>,
    val referenceCode: String
)

// ---------- Shared error envelope ----------

data class ApiErrorResponse(
    val success: Boolean,
    val message: String,
    val errors: List<ApiFieldError> = emptyList()
)

data class ApiFieldError(
    val field: String?,
    val message: String
)
