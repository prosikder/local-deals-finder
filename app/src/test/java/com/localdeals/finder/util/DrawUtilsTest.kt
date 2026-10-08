package com.localdeals.finder.util

import androidx.compose.ui.geometry.Size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DrawUtilsTest {

    @Test
    fun calculateSafeTextBounds_insideCanvas_returnsCorrectSize() {
        val size = DrawUtils.calculateSafeTextBounds(100f, 200f, 1080f, 1920f)
        assertEquals(Size(980f, 1720f), size)
    }

    @Test
    fun calculateSafeTextBounds_outsideCanvasLeft_returnsNull() {
        val size = DrawUtils.calculateSafeTextBounds(-10f, 200f, 1080f, 1920f)
        assertNull(size)
    }

    @Test
    fun calculateSafeTextBounds_outsideCanvasRight_returnsNull() {
        val size = DrawUtils.calculateSafeTextBounds(1100f, 200f, 1080f, 1920f)
        assertNull(size)
    }

    @Test
    fun calculateSafeTextBounds_outsideCanvasTop_returnsNull() {
        val size = DrawUtils.calculateSafeTextBounds(100f, -10f, 1080f, 1920f)
        assertNull(size)
    }

    @Test
    fun calculateSafeTextBounds_outsideCanvasBottom_returnsNull() {
        val size = DrawUtils.calculateSafeTextBounds(100f, 2000f, 1080f, 1920f)
        assertNull(size)
    }

    @Test
    fun calculateSafeTextBounds_exactlyOnEdge_returnsZeroSize() {
        val size = DrawUtils.calculateSafeTextBounds(1080f, 1920f, 1080f, 1920f)
        assertEquals(Size(0f, 0f), size)
    }
}
