package ph.edu.benilde.fbms.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/v1/mobile/activate")
    suspend fun activate(
        @Body request: ActivationRequest
    ): Response<ActivationEnvelope>

    @GET("api/v1/mobile/survey")
    suspend fun getActiveSurvey(): Response<SurveyResponse>

    @POST("api/v1/mobile/feedback")
    suspend fun submitFeedback(
        @Body body: SubmitFeedbackRequest
    ): Response<SubmitFeedbackResponse>

    @POST("api/v1/mobile/heartbeat")
    suspend fun heartbeat(): Response<Void>
}
