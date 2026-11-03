package com.almasawi.keyboard

import com.almasawi.keyboard.layouts.DigitStyle
import com.almasawi.keyboard.layouts.KeyType
import com.almasawi.keyboard.layouts.LayoutFactory
import com.almasawi.keyboard.layouts.LayoutId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutFactoryTest {

    private fun labels(id: LayoutId) =
        LayoutFactory.build(id).rows.flatten().filter { it.type == KeyType.CHAR }.map { it.label }

    @Test fun arabicLayoutContainsAll28Letters() {
        val letters = labels(LayoutId.ARABIC)
        "ابتثجحخدذرزسشصضطظعغفقكلمنهوي".forEach {
            assertTrue("missing $it", it.toString() in letters)
        }
    }

    @Test fun arabicIsRtlAndEnglishIsNot() {
        assertTrue(LayoutFactory.build(LayoutId.ARABIC).isRtl)
        assertFalse(LayoutFactory.build(LayoutId.ENGLISH).isRtl)
    }

    @Test fun englishLayoutContainsAll26Letters() {
        val letters = labels(LayoutId.ENGLISH)
        ('a'..'z').forEach { assertTrue("missing $it", it.toString() in letters) }
    }

    @Test fun hamzaFormsAvailableOnLongPressOfAlef() {
        val alef = LayoutFactory.build(LayoutId.ARABIC).rows.flatten().first { it.label == "ا" }
        assertEquals(listOf("أ", "إ", "آ", "ء"), alef.alternates)
    }

    @Test fun arabicShiftLayerProducesHarakat() {
        val first = LayoutFactory.build(LayoutId.ARABIC, numberRow = false).rows[0][0]
        assertEquals("ض", first.outputFor(false))
        assertEquals("\u064E", first.outputFor(true))
        assertNotNull(first.shiftLabel)
    }

    @Test fun englishShiftUppercases() {
        val q = LayoutFactory.build(LayoutId.ENGLISH).rows.flatten().first { it.label == "q" }
        assertEquals("Q", q.outputFor(true))
        assertEquals("q", q.outputFor(false))
    }

    @Test fun numberRowCanBeToggled() {
        assertEquals(5, LayoutFactory.build(LayoutId.ARABIC, numberRow = true).rows.size)
        assertEquals(4, LayoutFactory.build(LayoutId.ARABIC, numberRow = false).rows.size)
    }

    @Test fun digitStyleIsApplied() {
        val indic = LayoutFactory.build(LayoutId.ENGLISH, DigitStyle.ARABIC_INDIC).rows[0]
        assertEquals("١", indic.first().label)
        assertEquals("١٢٣٤٥٦٧٨٩٠", indic.joinToString("") { it.label })
        // long-press gives the other numeral system (mixed typing)
        assertEquals(listOf("1"), indic.first().alternates)
    }

    @Test fun everyLayoutHasEnterSpaceAndDelete() {
        LayoutId.values().forEach { id ->
            val keys = LayoutFactory.build(id).rows.flatten()
            assertEquals("$id enter", 1, keys.count { it.type == KeyType.ENTER })
            assertEquals("$id space", 1, keys.count { it.type == KeyType.SPACE })
            assertEquals("$id delete", 1, keys.count { it.type == KeyType.DELETE })
        }
    }

    @Test fun languageKeyExistsOnLetterLayoutsOnly() {
        assertTrue(LayoutFactory.build(LayoutId.ARABIC).rows.flatten().any { it.type == KeyType.LANGUAGE })
        assertTrue(LayoutFactory.build(LayoutId.ENGLISH).rows.flatten().any { it.type == KeyType.LANGUAGE })
    }

    @Test fun spaceOutputsASpace() {
        LayoutId.values().forEach { id ->
            val space = LayoutFactory.build(id).rows.flatten().first { it.type == KeyType.SPACE }
            assertEquals(" ", space.output)
        }
    }
}
