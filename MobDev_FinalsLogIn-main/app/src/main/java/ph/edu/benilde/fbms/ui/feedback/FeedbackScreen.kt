package ph.edu.benilde.fbms.ui.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.benilde.fbms.ui.feedback.components.QuestionInput
import ph.edu.benilde.fbms.ui.theme.KioskColors

/** Dark green gradient used by the activation/welcome/thank-you "chrome" screens. */
private val darkChromeBrush = Brush.verticalGradient(
    colors = listOf(
        KioskColors.darkGreenTop,
        KioskColors.darkGreenMid,
        KioskColors.darkGreenGlow.copy(alpha = 0.35f)
    )
)

/**
 * @param onSessionFinished called after a successful submission (with a short
 *   delay/confirmation handled by the caller) OR after the visitor dismisses
 *   the "survey changed" notice — wire this to your idle-screen navigation.
 */
@Composable
fun FeedbackScreen(
    viewModel: FeedbackViewModel,
    onSessionFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        is FeedbackUiState.Loading ->
            ChromeScreen {
                CircularProgressIndicator(color = Color.White)
            }

        is FeedbackUiState.NoSurveyAvailable ->
            ChromeScreen {
                Text(
                    "No survey is available for this station right now.",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Please check back shortly.", color = Color.White.copy(alpha = 0.8f))
            }

        is FeedbackUiState.Error ->
            ChromeScreen {
                Text("Something went wrong: ${s.message}", color = Color.White)
                Button(onClick = { viewModel.loadSurvey() }) { Text("Retry") }
            }

        is FeedbackUiState.InProgress ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KioskColors.surveyBackground)
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(s.survey.questions) { index, question ->
                            QuestionInput(
                                questionNumber = index + 1,
                                question = question,
                                currentAnswer = s.answers[question.id],
                                isMissing = question.id in s.missingRequired,
                                onAnswer = { viewModel.setAnswer(question.id, it) }
                            )
                        }
                    }

                    s.submitError?.let {
                        Text(text = it, color = Color(0xFFB3261E), modifier = Modifier.padding(bottom = 8.dp))
                    }

                    Button(
                        onClick = { viewModel.submit() },
                        enabled = !s.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KioskColors.ink,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (s.isSubmitting) "Submitting..." else "Submit")
                    }
                }
            }

        is FeedbackUiState.Submitted ->
            ChromeScreen {
                Text(
                    "Thank you for the feedback!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("Reference: ${s.referenceCode}", color = Color.White.copy(alpha = 0.85f))
                Button(onClick = onSessionFinished) { Text("Done") }
            }

        is FeedbackUiState.SurveyChangedMidSession ->
            ChromeScreen {
                Text(
                    "This survey was just updated by staff.",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Please redo your feedback with the new questions.", color = Color.White.copy(alpha = 0.85f))
                Button(onClick = { viewModel.loadSurvey() }) { Text("Start again") }
            }
    }
}

/** Shared dark-green gradient shell for the loading/waiting/error/thank-you states. */
@Composable
private fun ChromeScreen(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(darkChromeBrush).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}
