package ph.edu.benilde.fbms.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://fbms21-api.onrender.com/"
    
    private var apiService: ApiService? = null
    var onUnauthorizedListener: (() -> Unit)? = null

    fun getApiService(context: android.content.Context): ApiService {
        if (apiService == null) {
            val secureStore = SecureDeviceStore(context)
            val authInterceptor = AuthInterceptor(secureStore) {
                onUnauthorizedListener?.invoke()
            }
            
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            apiService = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
        return apiService!!
    }

    fun reset() {
        apiService = null
    }
}

