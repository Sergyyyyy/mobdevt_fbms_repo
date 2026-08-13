package com.example.infodbm_finals.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Placeholder auth: attaches `Authorization: Device <deviceSecret>` to every
 * request. Swap `deviceSecretProvider` for whatever teammate builds the real
 * activation flow (e.g. reading from Android Keystore) — this interceptor
 * itself never needs to change.
 */
class DeviceAuthInterceptor(
    private val deviceSecretProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val secret = deviceSecretProvider()
        val request = chain.request().newBuilder().apply {
            if (secret != null) {
                addHeader("Authorization", "Device $secret")
            }
        }.build()
        return chain.proceed(request)
    }
}

object ApiClient {

    // TODO: replace with your deployed API URL, or 10.0.2.2 for the emulator
    // reaching your dev machine's localhost per the API doc.
    private const val BASE_URL = "https://fbms21-api.onrender.com/api/v1/"

    fun create(deviceSecretProvider: () -> String?): FeedbackApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(DeviceAuthInterceptor(deviceSecretProvider))
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(FeedbackApiService::class.java)
    }
}
