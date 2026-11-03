package com.almasawi.keyboard.ime

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

object ImeStatus {
    fun isEnabled(context: Context): Boolean {
        val imm = context.getSystemService(InputMethodManager::class.java) ?: return false
        return imm.enabledInputMethodList.any { it.packageName == context.packageName }
    }

    fun isSelected(context: Context): Boolean {
        val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return id?.startsWith(context.packageName + "/") == true
    }
}
