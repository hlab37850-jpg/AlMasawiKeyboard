package com.almasawi.keyboard

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeRegistrationTest {
    @Test fun imeServiceIsRegisteredWithTheSystem() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val found = imm.inputMethodList.any {
            it.packageName == ctx.packageName && it.serviceName == "com.almasawi.keyboard.ime.AlMasawiImeService"
        }
        assertTrue("IME service not registered", found)
    }
}
