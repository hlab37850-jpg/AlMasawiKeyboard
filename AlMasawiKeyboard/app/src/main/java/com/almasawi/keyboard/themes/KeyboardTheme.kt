package com.almasawi.keyboard.themes

data class KeyboardTheme(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val background: Int,
    val key: Int,
    val keyPressed: Int,
    val functionKey: Int,
    val text: Int,
    val hint: Int,
    val accent: Int,
    val onAccent: Int,
)

object ThemeCatalog {
    const val DEFAULT_ID = "midnight_black"

    private fun c(v: Long): Int = v.toInt()

    private fun t(
        id: String, ar: String, en: String,
        bg: Long, key: Long, pressed: Long, fn: Long,
        text: Long, hint: Long, accent: Long, onAccent: Long,
    ) = KeyboardTheme(id, ar, en, c(bg), c(key), c(pressed), c(fn), c(text), c(hint), c(accent), c(onAccent))

    val all: List<KeyboardTheme> = listOf(
        t("midnight_black", "أسود منتصف الليل", "Midnight Black",
            0xFF0B0E14, 0xFF1A1F2B, 0xFF2C3447, 0xFF121620, 0xFFF2F4F8, 0xFF8A93A6, 0xFF5B8CFF, 0xFFFFFFFF),
        t("ocean_blue", "أزرق المحيط", "Ocean Blue",
            0xFF0A2540, 0xFF15406E, 0xFF1F5A96, 0xFF0E3257, 0xFFFFFFFF, 0xFF9CC3E8, 0xFF38BDF8, 0xFF06243D),
        t("royal_purple", "بنفسجي ملكي", "Royal Purple",
            0xFF1E1040, 0xFF35206B, 0xFF4B2E94, 0xFF2A1856, 0xFFF5F0FF, 0xFFB7A5E6, 0xFFA78BFA, 0xFF1E1040),
        t("emerald", "زمردي", "Emerald",
            0xFF0B2E24, 0xFF14513F, 0xFF1D705A, 0xFF0F3F31, 0xFFECFDF5, 0xFF9FD4C0, 0xFF34D399, 0xFF06261C),
        t("pure_white", "أبيض نقي", "Pure White",
            0xFFE9ECF2, 0xFFFFFFFF, 0xFFD5DAE6, 0xFFCDD3DE, 0xFF1B2130, 0xFF6B7385, 0xFF2F5BFF, 0xFFFFFFFF),
        t("amoled_dark", "داكن AMOLED", "AMOLED Dark",
            0xFF000000, 0xFF121212, 0xFF262626, 0xFF0A0A0A, 0xFFEDEDED, 0xFF7A7A7A, 0xFF7C9CFF, 0xFF000000),
        t("golden_luxury", "ذهبي فاخر", "Golden Luxury",
            0xFF14110A, 0xFF2A2415, 0xFF3D3520, 0xFF1D1910, 0xFFF6E7B4, 0xFFA89868, 0xFFD4AF37, 0xFF1A1405),
        t("minimal_arabic", "عربي بسيط", "Minimal Arabic",
            0xFFF4EFE6, 0xFFFBF8F2, 0xFFE6DECF, 0xFFE3DCCD, 0xFF2B2B2B, 0xFF7D766A, 0xFF1F6F5C, 0xFFFFFFFF),
    )

    fun byId(id: String): KeyboardTheme = all.firstOrNull { it.id == id } ?: all.first()

    val default: KeyboardTheme get() = byId(DEFAULT_ID)
}
