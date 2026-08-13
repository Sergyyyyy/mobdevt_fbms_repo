package com.example.infodbm_finals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.infodbm_finals.data.api.ApiClient
import com.example.infodbm_finals.data.repository.FeedbackRepository
import com.example.infodbm_finals.ui.feedback.FeedbackScreen
import com.example.infodbm_finals.ui.feedback.FeedbackViewModel
import com.example.infodbm_finals.ui.theme.INFODBM_FINALS_CRUZTheme

/**
 * TEMPORARY entry point for testing the feedback flow in isolation, before
 * activation/idle screens exist. Replace this with real navigation once
 * those screens are ready — this Activity should not ship as-is.
 */
class MainActivity : ComponentActivity() {

    // TODO: swap for the real activation flow's stored Device Secret
    // (Android Keystore) once that teammate's work is merged.
    private val placeholderDeviceSecret = "dvc_YnsLJsDgxEa6h9Vh1C095BzZY4B98mMV0ZxJ4mz9-rE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            INFODBM_FINALS_CRUZTheme {
                val viewModel: FeedbackViewModel = viewModel(
                    factory = FeedbackViewModelFactory(placeholderDeviceSecret)
                )
                FeedbackScreen(
                    viewModel = viewModel,
                    onSessionFinished = {
                        // TODO: navigate back to idle screen once it exists.
                        // For now, just start a fresh session.
                        viewModel.loadSurvey()
                    }
                )
            }
        }
    }
}

private class FeedbackViewModelFactory(
    private val deviceSecret: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = ApiClient.create { deviceSecret }
        val repository = FeedbackRepository(api)
        @Suppress("UNCHECKED_CAST")
        return FeedbackViewModel(repository) as T
    }
}
