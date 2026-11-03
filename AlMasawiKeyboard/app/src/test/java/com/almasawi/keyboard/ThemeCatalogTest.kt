package com.almasawi.keyboard

import com.almasawi.keyboard.themes.ThemeCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ThemeCatalogTest {
    @Test fun hasEightThemesWithUniqueIds() {
        assertEquals(8, ThemeCatalog.all.size)
        assertEquals(8, ThemeCatalog.all.map { it.id }.toSet().size)
    }

    @Test fun unknownIdFallsBackToDefault() {
        assertEquals(ThemeCatalog.DEFAULT_ID, ThemeCatalog.byId("does_not_exist").id)
    }

    @Test fun textIsReadableOnKeys() {
        ThemeCatalog.all.forEach { assertNotEquals(it.id, it.key, it.text) }
    }
}
