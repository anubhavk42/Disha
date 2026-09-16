package com.example.ui.theme

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

/**
 * Semantic haptic feedback intents used across Disha. Backed by [android.view.View.performHapticFeedback]
 * rather than Compose's own HapticFeedbackType, since the latter only exposes LongPress/TextHandleMove at
 * the Compose version this app is pinned to - the View API exposes the full platform constant set (tick,
 * confirm, reject) needed to make different interactions feel distinct rather than uniformly buzzy.
 */
enum class Haptic {
    /** Light tap - buttons, nav tab switches, card taps, chip add/remove. */
    Click,
    /** A switch, checkbox, or segmented option flipping state. */
    Toggle,
    /** A positive/successful completion - login, save, apply, referral sent. */
    Confirm,
    /** Validation failure or a blocked action. */
    Reject,
    /** Destructive or high-stakes actions - delete, logout, erase data. */
    Warning
}

/** Returns a callback that triggers the given [Haptic] on the current view. */
@Composable
fun rememberHaptics(): (Haptic) -> Unit {
    val view = LocalView.current
    return { haptic ->
        val constant = when (haptic) {
            Haptic.Click -> HapticFeedbackConstants.VIRTUAL_KEY
            Haptic.Toggle -> HapticFeedbackConstants.CLOCK_TICK
            Haptic.Confirm -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
            Haptic.Reject -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.REJECT
            } else {
                HapticFeedbackConstants.LONG_PRESS
            }
            Haptic.Warning -> HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
    }
}
