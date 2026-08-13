package ph.edu.benilde.fbms.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.benilde.fbms.data.RetrofitClient
import ph.edu.benilde.fbms.data.SecureDeviceStore

/**
 * Masks a device secret so only the last 4 characters are visible.
 * e.g. "sec_abc1234a210" → "sec_****a210"
 */
private fun maskSecret(secret: String?): String {
    if (secret.isNullOrBlank()) return "—"
    return if (secret.length > 4) {
        secret.dropLast(4).replace(Regex("."), "*") + secret.takeLast(4)
    } else {
        "****"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onDeactivate: () -> Unit
) {
    val context = LocalContext.current
    val secureStore = remember { SecureDeviceStore(context) }
    val coroutineScope = rememberCoroutineScope()

    val deviceSecret = remember { secureStore.getDeviceSecret() }
    val deviceCode = remember { secureStore.getDeviceCode() }

    var showUnlinkDialog by remember { mutableStateOf(false) }
    var syncLoading by remember { mutableStateOf(false) }
    var heartbeatLoading by remember { mutableStateOf(false) }

    // Confirmation dialog for unlinking the device
    if (showUnlinkDialog) {
        AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            title = { Text("Unlink Device?") },
            text = {
                Text(
                    "This will erase the stored Device Secret and return to the Activation screen. " +
                    "You will need a new activation token to use this kiosk again."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUnlinkDialog = false
                        secureStore.clear()
                        onDeactivate()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Unlink")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Card 1: Device Status ─────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Device Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", style = MaterialTheme.typography.bodyMedium)
                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "ACTIVE",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Device Code", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            deviceCode ?: "Unknown",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Device Secret", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            maskSecret(deviceSecret),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            // ── Button 1: Sync Config ─────────────────────────────────────
            Button(
                onClick = {
                    coroutineScope.launch {
                        syncLoading = true
                        try {
                            val api = RetrofitClient.getApiService(context)
                            val response = withContext(Dispatchers.IO) { api.getActiveSurvey() }
                            if (response.code() == 401) {
                                Toast.makeText(context, "Device deactivated or invalid credentials. Returning to activation screen...", Toast.LENGTH_LONG).show()
                                secureStore.clear()
                                onDeactivate()
                            } else {
                                val message = if (response.isSuccessful) {
                                    "Config synced successfully!"
                                } else {
                                    "Sync failed: HTTP ${response.code()}"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Sync failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            syncLoading = false
                        }
                    }
                },
                enabled = !syncLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (syncLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (syncLoading) "Syncing..." else "Sync Config")
            }

            // ── Button 2: Test Heartbeat ──────────────────────────────────
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        heartbeatLoading = true
                        try {
                            val api = RetrofitClient.getApiService(context)
                            val response = withContext(Dispatchers.IO) { api.heartbeat() }
                            if (response.code() == 401) {
                                Toast.makeText(context, "Device deactivated or invalid credentials. Returning to activation screen...", Toast.LENGTH_LONG).show()
                                secureStore.clear()
                                onDeactivate()
                            } else {
                                val message = if (response.isSuccessful) {
                                    "Heartbeat OK — server is reachable"
                                } else {
                                    "Heartbeat failed: HTTP ${response.code()}"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Heartbeat failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            heartbeatLoading = false
                        }
                    }
                },
                enabled = !heartbeatLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (heartbeatLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (heartbeatLoading) "Sending..." else "Test Heartbeat")
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Button 3: Unlink Device (destructive) ─────────────────────
            Button(
                onClick = { showUnlinkDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Unlink Device")
            }
        }
    }
}
