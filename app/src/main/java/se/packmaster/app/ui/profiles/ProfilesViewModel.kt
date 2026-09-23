package se.packmaster.app.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.packmaster.app.data.prefs.SettingsStore
import se.packmaster.app.data.prefs.ThemeMode
import se.packmaster.app.ui.appContainer
import se.packmaster.domain.model.Profile
import se.packmaster.domain.repository.CatalogRepository

class ProfilesViewModel(
    private val repository: CatalogRepository,
    private val settings: SettingsStore,
) : ViewModel() {

    val profiles: StateFlow<List<Profile>?> = repository.observeProfiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val themeMode: StateFlow<ThemeMode> = settings.themeMode

    fun saveProfile(profile: Profile) {
        viewModelScope.launch { repository.saveProfile(profile) }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch { repository.deleteProfile(profile) }
    }

    fun setThemeMode(mode: ThemeMode) = settings.setThemeMode(mode)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                ProfilesViewModel(container.catalogRepository, container.settings)
            }
        }
    }
}
