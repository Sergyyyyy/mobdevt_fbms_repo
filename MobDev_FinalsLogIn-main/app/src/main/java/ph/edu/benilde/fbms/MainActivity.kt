package ph.edu.benilde.fbms

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import ph.edu.benilde.fbms.data.SecureDeviceStore
import ph.edu.benilde.fbms.ui.SettingsScreen
import ph.edu.benilde.fbms.ui.WelcomeScreen
import ph.edu.benilde.fbms.ui.feedback.FeedbackScreen
import ph.edu.benilde.fbms.ui.feedback.FeedbackViewModel
import ph.edu.benilde.fbms.ui.feedback.FeedbackViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    val context = LocalContext.current
                    val secureStore = remember { SecureDeviceStore(context) }

                    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
                    val currentBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = currentBackStackEntry?.destination?.route

                    LaunchedEffect(lastInteractionTime, currentRoute) {
                        if (currentRoute != "welcome" && currentRoute != "activation") {
                            delay(30_000L) // 30 seconds inactivity timeout
                            navController.navigate("welcome") {
                                popUpTo(0)
                            }
                        }
                    }

                    LaunchedEffect(Unit) {
                        ph.edu.benilde.fbms.data.RetrofitClient.onUnauthorizedListener = {
                            runOnUiThread {
                                Toast.makeText(
                                    context,
                                    "Device credentials invalid or deactivated. Please re-activate.",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigate("activation") {
                                    popUpTo(0)
                                }
                            }
                        }
                    }
                    
                    val startDestination = if (secureStore.isActivated()) "welcome" else "activation"

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent()
                                        lastInteractionTime = System.currentTimeMillis()
                                    }
                                }
                            }
                    ) {
                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("activation") {
                            ActivationScreen(
                                onActivated = { deviceName ->
                                    Toast.makeText(context, "Activated as: $deviceName", Toast.LENGTH_LONG).show()
                                    navController.navigate("welcome") {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }
                        composable("welcome") {
                            WelcomeScreen(
                                onStartSurvey = {
                                    navController.navigate("survey")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("survey") {
                            val feedbackViewModel: FeedbackViewModel = viewModel(
                                factory = FeedbackViewModelFactory(context)
                            )
                            FeedbackScreen(
                                viewModel = feedbackViewModel,
                                onSessionFinished = {
                                    navController.popBackStack("welcome", inclusive = false)
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onDeactivate = {
                                    navController.navigate("activation") {
                                        popUpTo(0)
                                    }
                                }
                            )
                        }
                    }
                    }
                }
            }
        }
    }
}
