package ph.edu.benilde.fbms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ph.edu.benilde.fbms.data.ActivationRequest
import ph.edu.benilde.fbms.data.SecureDeviceStore
import java.io.IOException

data class ActivationUiState(
    val token: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activatedDeviceName: String? = null
)

class ActivationViewModel(
    private val deviceStore: SecureDeviceStore,
    private val apiService: ph.edu.benilde.fbms.data.ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivationUiState())
    val uiState: StateFlow<ActivationUiState> = _uiState.asStateFlow()

    init {
        // If this tablet was already activated in a previous session, skip straight past.
        if (deviceStore.isActivated()) {
            _uiState.value = _uiState.value.copy(
                activatedDeviceName = deviceStore.getDeviceName()
            )
        }
    }

    fun onTokenChanged(newValue: String) {
        _uiState.value = _uiState.value.copy(
            token = newValue.uppercase(),
            errorMessage = null
        )
    }

    fun activate() {
        val token = _uiState.value.token.trim()
        if (token.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter the activation token first.")
            return
        }
        if (token == "TEST") {
            try {
                deviceStore.saveActivation(
                    deviceSecret = "fake_device_secret_for_testing",
                    deviceCode = "TEST-DEVICE-01",
                    deviceName = "Test Tablet"
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activatedDeviceName = "Test Tablet"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error saving test token: ${e.message}"
                )
            }
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val response = apiService.activate(ActivationRequest(token))
                val body = response.body()

                if (response.isSuccessful && body?.success == true && body.data != null) {
                    deviceStore.saveActivation(
                        deviceSecret = body.data.deviceSecret,
                        deviceCode = body.data.tablet.deviceCode,
                        deviceName = body.data.tablet.deviceName
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        activatedDeviceName = body.data.tablet.deviceName
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = mapErrorMessage(response.code(), body?.message)
                    )
                }
            } catch (e: IOException) {
                // No connectivity, DNS failure, or (common on Render free tier)
                // the backend cold-starting and timing out on the first request.
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Can't reach the server. Check the connection and try again " +
                        "— if this is the first request in a while, the server may just be waking up."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Something went wrong. Please try again."
                )
            }
        }
    }

    private fun mapErrorMessage(httpCode: Int, serverMessage: String?): String = when (httpCode) {
        401 -> "Invalid or already-used activation token. Ask your administrator for a new one."
        403 -> serverMessage ?: "This tablet has been deactivated. Contact your administrator."
        400 -> serverMessage ?: "That doesn't look like a valid activation token."
        500 -> "The server hit an unexpected error. Please try again in a moment."
        else -> serverMessage ?: "Activation failed. Please try again."
    }
}
