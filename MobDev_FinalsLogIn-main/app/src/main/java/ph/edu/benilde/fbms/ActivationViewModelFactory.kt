package ph.edu.benilde.fbms

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ph.edu.benilde.fbms.data.SecureDeviceStore

class ActivationViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ActivationViewModel(
            SecureDeviceStore(appContext.applicationContext),
            ph.edu.benilde.fbms.data.RetrofitClient.getApiService(appContext.applicationContext)
        ) as T
    }
}
