package ph.edu.benilde.fbms.ui.feedback

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ph.edu.benilde.fbms.data.RetrofitClient
import ph.edu.benilde.fbms.data.repository.FeedbackRepository

class FeedbackViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return FeedbackViewModel(
            FeedbackRepository(
                RetrofitClient.getApiService(appContext.applicationContext)
            )
        ) as T
    }
}
