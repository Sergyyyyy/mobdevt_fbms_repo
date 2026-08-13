package ph.edu.benilde.fbms.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Colors pulled from the kiosk design references (activation screen, welcome
 * screen, survey screen, thank-you screen). Kept separate from the app's
 * default Material theme (Theme.kt) since the kiosk screens intentionally
 * don't follow standard Material color roles.
 */
object KioskColors {
    // Dark green "chrome" screens (activation, welcome, thank-you, waiting/error states)
    val darkGreenTop = Color(0xFF0A1F14)
    val darkGreenMid = Color(0xFF1B4632)
    val darkGreenGlow = Color(0xFFB7DD7C) // warm yellow-green glow at the bottom

    // Light green survey/question screen
    val surveyBackground = Color(0xFFB7DD7C)
    val ink = Color(0xFF16241B)
    val inkSoft = Color(0xFF2C3B2A)
    val starFilled = Color(0xFF16281B)
    val scaleTrack = Color(0x4016281B) // ink at ~25% alpha

    val cardWhite = Color(0xFFFFFFFF)
}
