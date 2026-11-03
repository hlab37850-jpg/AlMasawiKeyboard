package com.almasawi.keyboard.layouts

enum class KeyType {
    CHAR, SHIFT, DELETE, ENTER, SPACE, LANGUAGE,
    TO_SYMBOLS, TO_LETTERS, TO_SYMBOLS_MORE, TO_SYMBOLS_BASE
}

enum class LayoutId {
    ARABIC, ENGLISH, SYMBOLS, SYMBOLS_MORE;

    val isLetters: Boolean get() = this == ARABIC || this == ENGLISH
}

enum class DigitStyle { WESTERN, ARABIC_INDIC }

data class Key(
    val label: String,
    val type: KeyType = KeyType.CHAR,
    val weight: Float = 1f,
    val output: String = label,
    val shiftOutput: String? = null,
    val shiftLabel: String? = shiftOutput,
    val alternates: List<String> = emptyList(),
    val shiftAlternates: List<String> = emptyList(),
) {
    fun outputFor(shifted: Boolean): String = if (shifted && shiftOutput != null) shiftOutput else output
    fun labelFor(shifted: Boolean): String = if (shifted && shiftLabel != null) shiftLabel else label
    fun alternatesFor(shifted: Boolean): List<String> = if (shifted) shiftAlternates else alternates
}

data class KeyboardLayout(
    val id: LayoutId,
    val rows: List<List<Key>>,
    val isRtl: Boolean,
)
