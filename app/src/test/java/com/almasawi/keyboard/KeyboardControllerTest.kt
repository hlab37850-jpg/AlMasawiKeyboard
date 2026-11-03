package com.almasawi.keyboard

import com.almasawi.keyboard.keyboard.EnterAction
import com.almasawi.keyboard.keyboard.KeyboardController
import com.almasawi.keyboard.keyboard.ShiftState
import com.almasawi.keyboard.keyboard.TextSink
import com.almasawi.keyboard.layouts.Key
import com.almasawi.keyboard.layouts.KeyType
import com.almasawi.keyboard.layouts.LayoutFactory
import com.almasawi.keyboard.layouts.LayoutId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class FakeSink : TextSink {
    val text = StringBuilder()
    val events = mutableListOf<String>()
    override fun commit(text: String) { this.text.append(text); events += "c:$text" }
    override fun deleteBackward() { if (text.isNotEmpty()) text.setLength(text.length - 1); events += "del" }
    override fun sendEnter() { events += "enter" }
    override fun performEditorAction(actionId: Int) { events += "action:$actionId" }
    override fun moveCursor(delta: Int) { events += "move:$delta" }
}

class KeyboardControllerTest {
    private lateinit var sink: FakeSink
    private lateinit var c: KeyboardController
    private var changes = 0

    @Before fun setUp() {
        sink = FakeSink()
        changes = 0
        c = KeyboardController(sink, onChanged = { changes++ })
    }

    private fun key(label: String, layout: LayoutId = c.layout) =
        LayoutFactory.build(layout).rows.flatten().first { it.label == label }

    private fun type(t: KeyType) =
        LayoutFactory.build(c.layout).rows.flatten().first { it.type == t }

    @Test fun startsOnArabic() = assertEquals(LayoutId.ARABIC, c.layout)

    @Test fun typesArabicText() {
        "مرحبا".forEach { c.onKey(key(it.toString())) }
        assertEquals("مرحبا", sink.text.toString())
    }

    @Test fun spaceInsertsSpace() {
        c.onKey(key("ب")); c.onKey(type(KeyType.SPACE)); c.onKey(key("ب"))
        assertEquals("ب ب", sink.text.toString())
    }

    @Test fun deleteRemovesLastChar() {
        c.onKey(key("ب")); c.onKey(key("ت")); c.onKey(type(KeyType.DELETE))
        assertEquals("ب", sink.text.toString())
    }

    @Test fun deleteOnEmptyTextDoesNotCrash() {
        c.onKey(type(KeyType.DELETE))
        assertEquals("", sink.text.toString())
    }

    @Test fun languageKeyTogglesBetweenArabicAndEnglishAndBack() {
        c.onKey(type(KeyType.LANGUAGE)); assertEquals(LayoutId.ENGLISH, c.layout)
        c.onKey(type(KeyType.LANGUAGE)); assertEquals(LayoutId.ARABIC, c.layout)
    }

    @Test fun switchingLanguageKeepsAlreadyTypedText() {
        c.onKey(key("ب"))
        c.onKey(type(KeyType.LANGUAGE))
        c.onKey(key("a"))
        assertEquals("بa", sink.text.toString())
    }

    @Test fun shiftOnceCapitalisesOneLetterThenResets() {
        c.onKey(type(KeyType.LANGUAGE))
        c.onKey(type(KeyType.SHIFT))
        assertEquals(ShiftState.ONCE, c.shift)
        c.onKey(key("a")); c.onKey(key("b"))
        assertEquals("Ab", sink.text.toString())
        assertEquals(ShiftState.OFF, c.shift)
    }

    @Test fun capsLockPersists() {
        c.onKey(type(KeyType.LANGUAGE))
        c.onKey(type(KeyType.SHIFT)); c.onKey(type(KeyType.SHIFT))
        assertEquals(ShiftState.CAPS, c.shift)
        c.onKey(key("a")); c.onKey(key("b"))
        assertEquals("AB", sink.text.toString())
        c.onKey(type(KeyType.SHIFT))
        assertEquals(ShiftState.OFF, c.shift)
    }

    @Test fun arabicShiftTypesFatha() {
        c.onKey(type(KeyType.SHIFT))
        val fatha = LayoutFactory.build(LayoutId.ARABIC).rows.flatten().first { it.label == "ق" }
        c.onKey(fatha)
        assertEquals(1, sink.text.length)
        assertEquals(true, sink.text[0] in '\u064B'..'\u0652')
    }

    @Test fun symbolsAndBack() {
        c.onKey(type(KeyType.LANGUAGE)) // English
        c.onKey(type(KeyType.TO_SYMBOLS)); assertEquals(LayoutId.SYMBOLS, c.layout)
        c.onKey(type(KeyType.TO_SYMBOLS_MORE)); assertEquals(LayoutId.SYMBOLS_MORE, c.layout)
        c.onKey(type(KeyType.TO_SYMBOLS_BASE)); assertEquals(LayoutId.SYMBOLS, c.layout)
        c.onKey(type(KeyType.TO_LETTERS)); assertEquals(LayoutId.ENGLISH, c.layout)
    }

    @Test fun languageKeyAlwaysResetsShift() {
        c.onKey(type(KeyType.SHIFT))
        c.onKey(type(KeyType.LANGUAGE))
        assertEquals(ShiftState.OFF, c.shift)
    }

    @Test fun enterSendsNewlineByDefault() {
        c.onKey(type(KeyType.ENTER))
        assertEquals(listOf("enter"), sink.events)
    }

    @Test fun enterPerformsEditorActionWhenAvailable() {
        c.onStartInput(EnterAction(3, "بحث", false), null)
        c.onKey(type(KeyType.ENTER))
        assertEquals(listOf("action:3"), sink.events)
        assertEquals("بحث", c.enterAction.label)
    }

    @Test fun numberFieldOpensSymbols() {
        c.onStartInput(EnterAction.NEWLINE, LayoutId.SYMBOLS)
        assertEquals(LayoutId.SYMBOLS, c.layout)
    }

    @Test fun returningToLettersAfterNumberFieldRestoresLanguage() {
        c.onKey(type(KeyType.LANGUAGE)) // English
        c.onStartInput(EnterAction.NEWLINE, LayoutId.SYMBOLS)
        c.onStartInput(EnterAction.NEWLINE, null)
        assertEquals(LayoutId.ENGLISH, c.layout)
    }

    @Test fun alternateTextIsCommitted() {
        c.onText("أ")
        assertEquals("أ", sink.text.toString())
    }

    @Test fun spaceDragMovesCursor() {
        c.onSpaceCursor(-3)
        assertEquals(listOf("move:-3"), sink.events)
    }

    @Test fun symbolsDoNotApplyShift() {
        c.onKey(type(KeyType.SHIFT))
        c.onKey(type(KeyType.TO_SYMBOLS))
        c.onKey(Key("-"))
        assertEquals("-", sink.text.toString())
    }
}
