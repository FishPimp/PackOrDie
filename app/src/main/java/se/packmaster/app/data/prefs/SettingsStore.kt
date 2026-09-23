package se.packmaster.app.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Små lokala inställningar som inte hör hemma i Room-databasen. */
class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("packmaster_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_THEME, null) } ?: ThemeMode.SYSTEM
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
        _themeMode.value = mode
    }

    var isSeeded: Boolean
        get() = prefs.getBoolean(KEY_SEEDED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_SEEDED, value).apply()
        }

    private companion object {
        const val KEY_THEME = "theme_mode"
        const val KEY_SEEDED = "seeded_v1"
    }
}
