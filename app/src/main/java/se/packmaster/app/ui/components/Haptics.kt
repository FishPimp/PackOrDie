package se.packmaster.app.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Haptisk återkoppling (vibration) när objekt bockas av. */
class Haptics(context: Context) {

    private val vibrator: Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    /** Kort "klick" när ett objekt packas. */
    fun tick() = vibrate(
        predefined = VibrationEffect.EFFECT_CLICK,
        fallback = { VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE) },
    )

    /** Lättare knack när ett objekt avmarkeras. */
    fun softTick() = vibrate(
        predefined = VibrationEffect.EFFECT_TICK,
        fallback = { VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE) },
    )

    /** Dubbelpuls när allt är packat. */
    fun celebrate() {
        val v = vibrator?.takeIf { it.hasVibrator() } ?: return
        v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 80, 60), -1))
    }

    private fun vibrate(predefined: Int, fallback: () -> VibrationEffect) {
        val v = vibrator?.takeIf { it.hasVibrator() } ?: return
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(predefined)
        } else {
            fallback()
        }
        v.vibrate(effect)
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val context = LocalContext.current
    return remember(context) { Haptics(context) }
}
