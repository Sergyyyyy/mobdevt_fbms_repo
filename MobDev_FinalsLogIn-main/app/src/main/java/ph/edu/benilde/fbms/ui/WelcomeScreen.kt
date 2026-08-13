package ph.edu.benilde.fbms.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ph.edu.benilde.fbms.ui.theme.KioskColors

// ── Background gradient (same as FeedbackScreen's ChromeScreen) ─────────────
private val darkChromeBrush = Brush.verticalGradient(
    colors = listOf(
        KioskColors.darkGreenTop,
        KioskColors.darkGreenMid,
        KioskColors.darkGreenGlow.copy(alpha = 0.35f)
    )
)

// ── Glow orb color ────────────────────────────────────────────────────────────
private val glowColor = KioskColors.darkGreenGlow // warm yellow-green

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

    // ── Shared infinite transition for all animations ─────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "idle_anim")

    // 1. Glow orb — slow breathe (scale + alpha)
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_scale"
    )
    val orbAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_alpha"
    )

    // 2. CTA button — gentle scale pulse
    val btnScale by infiniteTransition.animateFloat(
        initialValue = 1.00f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btn_scale"
    )

    // 3. Hint text — fade in/out
    val hintAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.90f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_alpha"
    )

    // ── Root container — tapping anywhere also starts the survey ─────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkChromeBrush)
            .pointerInput(Unit) {
                detectTapGestures { onStartSurvey() }
            }
    ) {
        // ── Animated glow orb (centered behind CTA) ───────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(320.dp)
                .scale(orbScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = orbAlpha),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // ── Main content column ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // School name
            Text(
                text = "De La Salle–College of Saint Benilde",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // App / system name
            Text(
                text = "FBMS",
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                color = glowColor,
                letterSpacing = 4.sp
            )

            Text(
                text = "Feedback Management System",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.45f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            // CTA button — pulsing
            Button(
                onClick = onStartSurvey,
                modifier = Modifier
                    .widthIn(min = 220.dp)
                    .height(62.dp)
                    .scale(btnScale),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = glowColor,
                    contentColor = KioskColors.ink
                )
            ) {
                Text(
                    text = "Tap to Begin",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Hint text — fading
            Text(
                text = "Tap anywhere to rate your experience",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = hintAlpha),
                textAlign = TextAlign.Center
            )
        }

        // ── Hidden admin settings gear — bottom-left ──────────────────────────
        IconButton(
            onClick = { showPinDialog = true },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Admin Settings",
                tint = Color.White.copy(alpha = 0.18f)
            )
        }
    }
}
