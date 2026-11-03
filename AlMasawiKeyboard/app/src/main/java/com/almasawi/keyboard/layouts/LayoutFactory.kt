package com.almasawi.keyboard.layouts

object LayoutFactory {

    private const val AR1 = "ضصثقفغعهخحج"
    private const val AR2 = "شسيبلاتنمك"
    private const val AR3 = "ظطذدزروةى"
    private const val EN1 = "qwertyuiop"
    private const val EN2 = "asdfghjkl"
    private const val EN3 = "zxcvbnm"
    private const val INDIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"

    // Shift layer of the Arabic layout: harakat on the first row, hamza forms on the second,
    // Arabic punctuation on the third.
    private val SH_AR1 = listOf(
        "\u064E", "\u064B", "\u064F", "\u064C", "\u0650", "\u064D",
        "\u0651", "\u0652", "\u0640", "\u061B", "\u061F",
    )
    private val SH_AR2 = listOf("أ", "إ", "آ", "ء", "ئ", "ؤ", "ى", "ة", "لا", "\u0670")
    private val SH_AR3 = listOf("،", "!", "«", "»", "(", ")", ":", "\"", "-")

    private val ALT_AR = mapOf(
        "ا" to listOf("أ", "إ", "آ", "ء"),
        "و" to listOf("ؤ"),
        "ي" to listOf("ئ", "ى"),
        "ه" to listOf("ة"),
        "ب" to listOf("پ"),
        "ج" to listOf("چ"),
        "ف" to listOf("ڤ"),
        "ك" to listOf("گ"),
        "ل" to listOf("لا"),
    )

    private val ALT_EN = mapOf(
        "a" to listOf("à", "á", "â", "ä", "æ", "ã", "å", "ā"),
        "e" to listOf("è", "é", "ê", "ë", "ē"),
        "i" to listOf("ì", "í", "î", "ï"),
        "o" to listOf("ò", "ó", "ô", "ö", "œ", "õ"),
        "u" to listOf("ù", "ú", "û", "ü"),
        "c" to listOf("ç"),
        "n" to listOf("ñ"),
        "y" to listOf("ý", "ÿ"),
    )

    fun digit(n: Int, style: DigitStyle): String =
        if (style == DigitStyle.WESTERN) n.toString() else INDIC_DIGITS[n].toString()

    private fun opposite(style: DigitStyle) =
        if (style == DigitStyle.WESTERN) DigitStyle.ARABIC_INDIC else DigitStyle.WESTERN

    fun numberRow(style: DigitStyle): List<Key> = "1234567890".map { ch ->
        val n = ch - '0'
        Key(label = digit(n, style), alternates = listOf(digit(n, opposite(style))))
    }

    private fun isHaraka(s: String) = s.length == 1 && (s[0] in '\u064B'..'\u0652' || s[0] == '\u0670')

    private fun arKey(c: String, shift: String): Key = Key(
        label = c,
        shiftOutput = shift,
        shiftLabel = if (isHaraka(shift)) "\u25CC$shift" else shift,
        alternates = ALT_AR[c].orEmpty(),
    )

    private fun enKey(c: String): Key {
        val alts = ALT_EN[c].orEmpty()
        return Key(
            label = c,
            shiftOutput = c.uppercase(),
            alternates = alts,
            shiftAlternates = alts.map { it.uppercase() },
        )
    }

    private fun shiftKey(w: Float) = Key("⇧", KeyType.SHIFT, w)
    private fun deleteKey(w: Float) = Key("⌫", KeyType.DELETE, w)
    private fun enterKey() = Key("", KeyType.ENTER, 1.5f)
    private fun sym(vararg s: String) = s.map { Key(it) }

    private fun arabicBottom() = listOf(
        Key("123", KeyType.TO_SYMBOLS, 1.5f),
        Key("EN", KeyType.LANGUAGE, 1.2f),
        Key("،", alternates = listOf("؛", "؟", "!", "…")),
        Key("العربية", KeyType.SPACE, 4.8f, output = " "),
        Key(".", alternates = listOf("؟", "!", "…", "،")),
        enterKey(),
    )

    private fun englishBottom() = listOf(
        Key("123", KeyType.TO_SYMBOLS, 1.5f),
        Key("ع", KeyType.LANGUAGE, 1.2f),
        Key(",", alternates = listOf(";", ":")),
        Key("English", KeyType.SPACE, 3.8f, output = " "),
        Key(".", alternates = listOf("?", "!", "…")),
        enterKey(),
    )

    private fun symbolsBottom(lettersLabel: String) = listOf(
        Key(lettersLabel, KeyType.TO_LETTERS, 1.5f),
        Key(",", alternates = listOf("،")),
        Key("", KeyType.SPACE, 5f, output = " "),
        Key(".", alternates = listOf("…")),
        enterKey(),
    )

    fun build(
        id: LayoutId,
        digitStyle: DigitStyle = DigitStyle.WESTERN,
        numberRow: Boolean = true,
        lettersLabel: String = "أبج",
    ): KeyboardLayout = when (id) {
        LayoutId.ARABIC -> KeyboardLayout(
            id,
            listOfNotNull(
                if (numberRow) numberRow(digitStyle) else null,
                AR1.mapIndexed { i, c -> arKey(c.toString(), SH_AR1[i]) },
                AR2.mapIndexed { i, c -> arKey(c.toString(), SH_AR2[i]) },
                listOf(shiftKey(1f)) +
                    AR3.mapIndexed { i, c -> arKey(c.toString(), SH_AR3[i]) } +
                    listOf(deleteKey(1f)),
                arabicBottom(),
            ),
            isRtl = true,
        )

        LayoutId.ENGLISH -> KeyboardLayout(
            id,
            listOfNotNull(
                if (numberRow) numberRow(digitStyle) else null,
                EN1.map { enKey(it.toString()) },
                EN2.map { enKey(it.toString()) },
                listOf(shiftKey(1.5f)) + EN3.map { enKey(it.toString()) } + listOf(deleteKey(1.5f)),
                englishBottom(),
            ),
            isRtl = false,
        )

        LayoutId.SYMBOLS -> KeyboardLayout(
            id,
            listOf(
                numberRow(digitStyle),
                sym("-", "/", ":", ";", "(", ")", "\$", "&", "@", "\""),
                listOf(Key("=\\<", KeyType.TO_SYMBOLS_MORE, 1.5f)) +
                    sym(".", "،", "؟", "!", "'", "؛", "…") + listOf(deleteKey(1.5f)),
                symbolsBottom(lettersLabel),
            ),
            isRtl = false,
        )

        LayoutId.SYMBOLS_MORE -> KeyboardLayout(
            id,
            listOf(
                numberRow(digitStyle),
                sym("[", "]", "{", "}", "#", "%", "^", "*", "+", "=") ,
                listOf(Key("123", KeyType.TO_SYMBOLS_BASE, 1.5f)) +
                    sym("_", "\\", "|", "~", "<", ">", "€") + listOf(deleteKey(1.5f)),
                symbolsBottom(lettersLabel),
            ),
            isRtl = false,
        )
    }
}
