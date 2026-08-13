package ph.edu.benilde.fbms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ph.edu.benilde.fbms.ui.theme.BenildeGray
import ph.edu.benilde.fbms.ui.theme.BenildeGreen
import ph.edu.benilde.fbms.ui.theme.BenildeGreenDark
import ph.edu.benilde.fbms.ui.theme.ErrorRed

/**
 * First screen the kiosk shows. This is the "sign in" step for this app in the
 * sense that it's the one-time gate before the tablet is usable — but per the
 * FBMS mobile protocol, it authenticates the DEVICE (Activation Token -> Device
 * Secret), not a person. There is no username/password/email here by design.
 *
 * onActivated is called once the Device Secret is saved, so the parent
 * composable/nav graph can move on to Config -> Survey.
 */
@Composable
fun ActivationScreen(
    onActivated: (deviceName: String) -> Unit,
    viewModel: ActivationViewModel = viewModel(
        factory = ActivationViewModelFactory(androidx.compose.ui.platform.LocalContext.current)
    )
) {
    val state by viewModel.uiState.collectAsState()

    // Already activated (e.g. app restarted) — hand off immediately.
    LaunchedEffect(state.activatedDeviceName) {
        state.activatedDeviceName?.let { onActivated(it) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BenildeGreenDark
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .padding(32.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "De La Salle-College of Saint Benilde",
                        fontSize = 14.sp,
                        color = BenildeGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "FBMS Tablet Setup",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = BenildeGreenDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Enter the activation Token given to you by an administrator " +
                            "to set up this kiosk.",
                        fontSize = 14.sp,
                        color = BenildeGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = state.token,
                        onValueChange = viewModel::onTokenChanged,
                        label = { Text("Activation Token") },
                        placeholder = { Text("TAB-XXXXXXXX") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        isError = state.errorMessage != null,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BenildeGreen,
                            cursorColor = BenildeGreen
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.errorMessage ?: "",
                            color = ErrorRed,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.activate() },
                        enabled = !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = BenildeGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color_White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Activate Tablet", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

private val Color_White = androidx.compose.ui.graphics.Color.White
