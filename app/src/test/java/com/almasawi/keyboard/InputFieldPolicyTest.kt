package com.almasawi.keyboard

import android.text.InputType
import com.almasawi.keyboard.layouts.LayoutId
import com.almasawi.keyboard.privacy.InputFieldPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InputFieldPolicyTest {
    private val text = InputType.TYPE_CLASS_TEXT

    @Test fun passwordFieldsAreSensitive() {
        assertTrue(InputFieldPolicy.isSensitive(text or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        assertTrue(InputFieldPolicy.isSensitive(text or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD))
        assertTrue(InputFieldPolicy.isSensitive(text or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD))
        assertTrue(InputFieldPolicy.isSensitive(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD))
    }

    @Test fun ordinaryFieldsAreNotSensitive() {
        assertFalse(InputFieldPolicy.isSensitive(text))
        assertFalse(InputFieldPolicy.isSensitive(text or InputType.TYPE_TEXT_FLAG_MULTI_LINE))
        assertFalse(InputFieldPolicy.isSensitive(InputType.TYPE_CLASS_NUMBER))
        assertFalse(InputFieldPolicy.isSensitive(text or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS))
    }

    @Test fun startLayoutPerFieldType() {
        assertEquals(LayoutId.SYMBOLS, InputFieldPolicy.startLayout(InputType.TYPE_CLASS_NUMBER))
        assertEquals(LayoutId.SYMBOLS, InputFieldPolicy.startLayout(InputType.TYPE_CLASS_PHONE))
        assertEquals(LayoutId.ENGLISH, InputFieldPolicy.startLayout(text or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS))
        assertEquals(LayoutId.ENGLISH, InputFieldPolicy.startLayout(text or InputType.TYPE_TEXT_VARIATION_URI))
        assertEquals(LayoutId.ENGLISH, InputFieldPolicy.startLayout(text or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        assertNull(InputFieldPolicy.startLayout(text))
    }
}
