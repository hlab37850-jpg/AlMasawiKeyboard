package com.almasawi.keyboard.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.almasawi.keyboard.keyboard.EnterAction

/** Maps the editor's IME options to what the Enter key should do and how it is labelled. */
object EnterKeyResolver {
    fun resolve(imeOptions: Int, inputType: Int): EnterAction {
        val isText = (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT
        val multiLine = isText && (inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0
        val noAction = (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (multiLine || noAction) return EnterAction.NEWLINE

        val action = imeOptions and EditorInfo.IME_MASK_ACTION
        val label = when (action) {
            EditorInfo.IME_ACTION_GO -> "اذهب"
            EditorInfo.IME_ACTION_SEARCH -> "بحث"
            EditorInfo.IME_ACTION_SEND -> "إرسال"
            EditorInfo.IME_ACTION_NEXT -> "التالي"
            EditorInfo.IME_ACTION_DONE -> "تم"
            EditorInfo.IME_ACTION_PREVIOUS -> "السابق"
            else -> return EnterAction.NEWLINE
        }
        return EnterAction(action, label, isNewline = false)
    }
}
