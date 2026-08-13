package com.example.infodbm_finals.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Covers only the feedback-flow endpoints (survey retrieval + submission).
 * Assumes it is attached to a Retrofit instance whose OkHttpClient already
 * has the `Authorization: Device <deviceSecret>` interceptor installed
 * elsewhere in the app (activation/auth is out of scope here).
 */
interface FeedbackApiService {

    @GET("mobile/survey")
    suspend fun getActiveSurvey(): Response<SurveyResponse>

    @POST("mobile/feedback")
    suspend fun submitFeedback(@Body body: SubmitFeedbackRequest): Response<SubmitFeedbackResponse>
}