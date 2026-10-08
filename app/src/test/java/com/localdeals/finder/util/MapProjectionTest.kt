package com.localdeals.finder.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MapProjectionTest {

    @Test
    fun `center point maps exactly to canvas center`() {
        val centerLat = 23.8103
        val centerLng = 90.4125
        val canvasWidth = 1000f
        val canvasHeight = 1000f
        val zoom = 1.0f

        val (x, y) = MapProjection.latLngToPixel(
            lat = centerLat, lng = centerLng,
            centerLat = centerLat, centerLng = centerLng,
            zoom = zoom,
            canvasWidth = canvasWidth, canvasHeight = canvasHeight
        )

        assertEquals(500f, x, 0.1f)
        assertEquals(500f, y, 0.1f)
    }

    @Test
    fun `pixel to latlng reverses correctly`() {
        val centerLat = 23.8103
        val centerLng = 90.4125
        val canvasWidth = 1000f
        val canvasHeight = 1000f
        val zoom = 2.5f

        val targetLat = 23.8200
        val targetLng = 90.4200

        val (x, y) = MapProjection.latLngToPixel(
            lat = targetLat, lng = targetLng,
            centerLat = centerLat, centerLng = centerLng,
            zoom = zoom,
            canvasWidth = canvasWidth, canvasHeight = canvasHeight
        )

        val (reversedLat, reversedLng) = MapProjection.pixelToLatLng(
            x = x, y = y,
            centerLat = centerLat, centerLng = centerLng,
            zoom = zoom,
            canvasWidth = canvasWidth, canvasHeight = canvasHeight
        )

        assertEquals(targetLat, reversedLat, 0.0001)
        assertEquals(targetLng, reversedLng, 0.0001)
    }
}
