package ph.edu.benilde.fbms.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onStartSurvey: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showPinDialog by remember { mutableStateOf(false) }

    if (showPinDialog) {
        AdminPinDialog(
            onDismiss = { showPinDialog = false },
            onSuccess = {
                showPinDialog = false
                onNavigateToSettings()
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content — centered
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to FBMS",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tap the button below to start the survey",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onStartSurvey,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(64.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Survey", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Settings gear icon — bottom-left corner
        IconButton(
            onClick = { showPinDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Admin Settings",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
        }
    }
}
