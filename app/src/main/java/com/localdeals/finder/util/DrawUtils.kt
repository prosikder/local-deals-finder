package com.localdeals.finder.util

import androidx.compose.ui.geometry.Size

object DrawUtils {
    /**
     * Calculates a safe size for drawText to prevent IllegalArgumentException when drawing
     * near or slightly outside the canvas bounds.
     * Returns null if the top-left coordinate is completely outside the canvas.
     */
    fun calculateSafeTextBounds(
        topLeftX: Float,
        topLeftY: Float,
        canvasWidth: Float,
        canvasHeight: Float
    ): Size? {
        if (topLeftX < 0f || topLeftX > canvasWidth || topLeftY < 0f || topLeftY > canvasHeight) {
            return null
        }
        val width = (canvasWidth - topLeftX).coerceAtLeast(0f)
        val height = (canvasHeight - topLeftY).coerceAtLeast(0f)
        return Size(width, height)
    }
}
