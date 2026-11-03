package com.almasawi.keyboard

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.almasawi.keyboard.ime.EnterKeyResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnterKeyResolverTest {
    private val text = InputType.TYPE_CLASS_TEXT

    @Test fun searchField() {
        val a = EnterKeyResolver.resolve(EditorInfo.IME_ACTION_SEARCH, text)
        assertFalse(a.isNewline)
        assertEquals(EditorInfo.IME_ACTION_SEARCH, a.actionId)
        assertEquals("بحث", a.label)
    }

    @Test fun sendNextDoneGo() {
        assertEquals("إرسال", EnterKeyResolver.resolve(EditorInfo.IME_ACTION_SEND, text).label)
        assertEquals("التالي", EnterKeyResolver.resolve(EditorInfo.IME_ACTION_NEXT, text).label)
        assertEquals("تم", EnterKeyResolver.resolve(EditorInfo.IME_ACTION_DONE, text).label)
        assertEquals("اذهب", EnterKeyResolver.resolve(EditorInfo.IME_ACTION_GO, text).label)
    }

    @Test fun multiLineAlwaysNewline() {
        val a = EnterKeyResolver.resolve(EditorInfo.IME_ACTION_SEND, text or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
        assertTrue(a.isNewline)
    }

    @Test fun noEnterActionFlagMeansNewline() {
        val a = EnterKeyResolver.resolve(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION, text)
        assertTrue(a.isNewline)
    }

    @Test fun unspecifiedIsNewline() {
        assertTrue(EnterKeyResolver.resolve(EditorInfo.IME_ACTION_UNSPECIFIED, text).isNewline)
        assertTrue(EnterKeyResolver.resolve(EditorInfo.IME_ACTION_NONE, text).isNewline)
    }
}
