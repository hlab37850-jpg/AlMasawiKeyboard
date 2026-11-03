package com.almasawi.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import com.almasawi.keyboard.keyboard.KeyboardController
import com.almasawi.keyboard.keyboard.KeyboardView
import com.almasawi.keyboard.keyboard.TextSink
import com.almasawi.keyboard.layouts.DigitStyle
import com.almasawi.keyboard.layouts.Key
import com.almasawi.keyboard.layouts.LayoutFactory
import com.almasawi.keyboard.layouts.LayoutId
import com.almasawi.keyboard.privacy.InputFieldPolicy
import com.almasawi.keyboard.settings.KeyboardSettings
import com.almasawi.keyboard.settings.SettingsRepository
import com.almasawi.keyboard.themes.ThemeCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AlMasawiImeService : InputMethodService() {

    private data class BuiltKey(
        val layout: LayoutId,
        val digits: DigitStyle,
        val numberRow: Boolean,
        val lettersLabel: String,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var settings = KeyboardSettings()
    private var keyboardView: KeyboardView? = null
    private var builtKey: BuiltKey? = null
    private lateinit var controller: KeyboardController

    private val sink = object : TextSink {
        override fun commit(text: String) {
            currentInputConnection?.commitText(text, 1)
        }

        override fun deleteBackward() {
            val ic = currentInputConnection ?: return
            val selected = ic.getSelectedText(0)
            if (!selected.isNullOrEmpty()) ic.commitText("", 1)
            else sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
        }

        override fun sendEnter() = sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)

        override fun performEditorAction(actionId: Int) {
            currentInputConnection?.performEditorAction(actionId)
        }

        override fun moveCursor(delta: Int) {
            val code = if (delta > 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
            repeat(kotlin.math.abs(delta)) { sendDownUpKeyEvents(code) }
        }
    }

    override fun onCreate() {
        super.onCreate()
        controller = KeyboardController(sink, onChanged = ::refresh)
        val repo = SettingsRepository(applicationContext)
        scope.launch {
            repo.settings.collect {
                settings = it
                applySettings()
            }
        }
    }

    override fun onCreateInputView(): View {
        builtKey = null
        val view = KeyboardView(this)
        view.listener = object : KeyboardView.Listener {
            override fun onKey(key: Key) = controller.onKey(key)
            override fun onText(text: String) = controller.onText(text)
            override fun onSpaceCursor(delta: Int) = controller.onSpaceCursor(delta)
        }
        keyboardView = view
        applySettings()
        return view
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        controller.onStartInput(
            EnterKeyResolver.resolve(info.imeOptions, info.inputType),
            InputFieldPolicy.startLayout(info.inputType),
        )
    }

    // Avoid the full-screen extract UI in landscape.
    override fun onEvaluateFullscreenMode(): Boolean = false

    private fun applySettings() {
        val v = keyboardView ?: return
        v.keyboardTheme = ThemeCatalog.byId(settings.themeId)
        v.hapticEnabled = settings.haptic
        v.soundEnabled = settings.sound
        v.heightScale = settings.keyHeightPercent / 100f
        refresh()
    }

    private fun refresh() {
        val v = keyboardView ?: return
        val label = if (controller.lastLetter == LayoutId.ENGLISH) "ABC" else "أبج"
        val key = BuiltKey(controller.layout, settings.digitStyle, settings.numberRow, label)
        if (key != builtKey) {
            v.layout = LayoutFactory.build(key.layout, key.digits, key.numberRow, key.lettersLabel)
            builtKey = key
        }
        v.shiftState = controller.shift
        v.enterLabel = controller.enterAction.label
    }

    override fun onDestroy() {
        scope.cancel()
        keyboardView = null
        super.onDestroy()
    }
}
