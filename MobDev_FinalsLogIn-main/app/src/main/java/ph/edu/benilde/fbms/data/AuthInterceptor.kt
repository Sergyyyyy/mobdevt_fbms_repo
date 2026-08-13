package ph.edu.benilde.fbms.data

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val secureStore: SecureDeviceStore,
    private val onUnauthorized: (() -> Unit)? = null
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Do not intercept if it's the activate endpoint
        if (request.url.encodedPath.endsWith("api/v1/mobile/activate")) {
            return chain.proceed(request)
        }

        val deviceSecret = secureStore.getDeviceSecret()
        val newRequestBuilder = request.newBuilder()

        if (deviceSecret != null) {
            newRequestBuilder.addHeader("Authorization", "Device $deviceSecret")
        }

        val response = chain.proceed(newRequestBuilder.build())

        if (response.code == 401) {
            if (secureStore.isActivated()) {
                secureStore.clear()
                onUnauthorized?.invoke()
            }
        }

        return response
    }
}

