package com.almasawi.keyboard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.almasawi.keyboard.settings.KeyboardSettings
import com.almasawi.keyboard.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository(app)

    val settings: StateFlow<KeyboardSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KeyboardSettings())

    fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        viewModelScope.launch { repo.update(transform) }
    }
}
