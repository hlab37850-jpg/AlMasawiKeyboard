package com.almasawi.keyboard.settings

import android.content.Context
import java.io.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.almasawi.keyboard.layouts.DigitStyle
import com.almasawi.keyboard.themes.ThemeCatalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class KeyboardSettings(
    val themeId: String = ThemeCatalog.DEFAULT_ID,
    val haptic: Boolean = true,
    val sound: Boolean = false,
    val numberRow: Boolean = true,
    val digitStyle: DigitStyle = DigitStyle.WESTERN,
    val keyHeightPercent: Int = 100,
)

private val Context.settingsStore by preferencesDataStore(name = "keyboard_settings")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsStore

    val settings: Flow<KeyboardSettings> = store.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it.toSettings() }

    suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        store.edit { prefs -> transform(prefs.toSettings()).writeTo(prefs) }
    }

    private companion object {
        val THEME = stringPreferencesKey("theme_id")
        val HAPTIC = booleanPreferencesKey("haptic")
        val SOUND = booleanPreferencesKey("sound")
        val NUMBER_ROW = booleanPreferencesKey("number_row")
        val DIGITS = stringPreferencesKey("digit_style")
        val HEIGHT = intPreferencesKey("key_height_percent")
    }

    private fun Preferences.toSettings(): KeyboardSettings {
        val d = KeyboardSettings()
        return KeyboardSettings(
            themeId = this[THEME] ?: d.themeId,
            haptic = this[HAPTIC] ?: d.haptic,
            sound = this[SOUND] ?: d.sound,
            numberRow = this[NUMBER_ROW] ?: d.numberRow,
            digitStyle = runCatching { DigitStyle.valueOf(this[DIGITS] ?: "") }.getOrDefault(d.digitStyle),
            keyHeightPercent = (this[HEIGHT] ?: d.keyHeightPercent).coerceIn(80, 130),
        )
    }

    private fun KeyboardSettings.writeTo(p: MutablePreferences) {
        p[THEME] = themeId
        p[HAPTIC] = haptic
        p[SOUND] = sound
        p[NUMBER_ROW] = numberRow
        p[DIGITS] = digitStyle.name
        p[HEIGHT] = keyHeightPercent.coerceIn(80, 130)
    }
}
