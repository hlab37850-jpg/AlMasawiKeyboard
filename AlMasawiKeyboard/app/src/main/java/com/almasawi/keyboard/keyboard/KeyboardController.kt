package com.almasawi.keyboard.keyboard

import com.almasawi.keyboard.layouts.Key
import com.almasawi.keyboard.layouts.KeyType
import com.almasawi.keyboard.layouts.LayoutId

/** Abstraction over InputConnection so the logic can be unit-tested on the JVM. */
interface TextSink {
    fun commit(text: String)
    fun deleteBackward()
    fun sendEnter()
    fun performEditorAction(actionId: Int)
    /** Positive = move right on screen, negative = left. */
    fun moveCursor(delta: Int)
}

enum class ShiftState { OFF, ONCE, CAPS }

data class EnterAction(val actionId: Int, val label: String, val isNewline: Boolean) {
    companion object {
        val NEWLINE = EnterAction(0, "", true)
    }
}

class KeyboardController(
    private val sink: TextSink,
    private val letterLayouts: List<LayoutId> = listOf(LayoutId.ARABIC, LayoutId.ENGLISH),
    private val onChanged: () -> Unit = {},
) {
    init {
        require(letterLayouts.isNotEmpty() && letterLayouts.all { it.isLetters })
    }

    var layout: LayoutId = letterLayouts.first()
        private set
    var lastLetter: LayoutId = letterLayouts.first()
        private set
    var shift: ShiftState = ShiftState.OFF
        private set
    var enterAction: EnterAction = EnterAction.NEWLINE
        private set

    private fun setLayout(id: LayoutId) {
        layout = id
        if (id.isLetters) lastLetter = id
        shift = ShiftState.OFF
        onChanged()
    }

    fun onStartInput(enter: EnterAction, startLayout: LayoutId?) {
        enterAction = enter
        shift = ShiftState.OFF
        val target = when {
            startLayout == null -> lastLetter
            startLayout.isLetters && startLayout !in letterLayouts -> lastLetter
            else -> startLayout
        }
        setLayout(target)
    }

    private val isShifted: Boolean get() = shift != ShiftState.OFF && layout.isLetters

    private fun afterCommit() {
        if (shift == ShiftState.ONCE) {
            shift = ShiftState.OFF
            onChanged()
        }
    }

    fun onKey(key: Key) {
        when (key.type) {
            KeyType.CHAR -> {
                sink.commit(key.outputFor(isShifted))
                afterCommit()
            }
            KeyType.SPACE -> {
                sink.commit(" ")
                afterCommit()
            }
            KeyType.DELETE -> sink.deleteBackward()
            KeyType.ENTER ->
                if (enterAction.isNewline) sink.sendEnter() else sink.performEditorAction(enterAction.actionId)
            KeyType.SHIFT -> {
                shift = when (shift) {
                    ShiftState.OFF -> ShiftState.ONCE
                    ShiftState.ONCE -> ShiftState.CAPS
                    ShiftState.CAPS -> ShiftState.OFF
                }
                onChanged()
            }
            KeyType.LANGUAGE -> {
                val current = if (layout.isLetters) layout else lastLetter
                val next = letterLayouts[(letterLayouts.indexOf(current) + 1) % letterLayouts.size]
                setLayout(next)
            }
            KeyType.TO_SYMBOLS, KeyType.TO_SYMBOLS_BASE -> setLayout(LayoutId.SYMBOLS)
            KeyType.TO_SYMBOLS_MORE -> setLayout(LayoutId.SYMBOLS_MORE)
            KeyType.TO_LETTERS -> setLayout(lastLetter)
        }
    }

    /** Text chosen from the long-press alternates popup. */
    fun onText(text: String) {
        sink.commit(text)
        afterCommit()
    }

    fun onSpaceCursor(delta: Int) = sink.moveCursor(delta)
}
