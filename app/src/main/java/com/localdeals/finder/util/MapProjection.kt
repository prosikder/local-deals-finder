package com.localdeals.finder.util

import kotlin.math.cos

object MapProjection {
    /**
     * Converts a geographic coordinate to a local 2D pixel coordinate relative to a center point.
     * Uses a simple equirectangular projection, which is accurate enough for city-scale mapping.
     * 
     * @param lat The latitude of the point to project
     * @param lng The longitude of the point to project
     * @param centerLat The latitude of the map center
     * @param centerLng The longitude of the map center
     * @param zoom The zoom level (scale factor). E.g., 1.0f means 1 meter = 1 pixel.
     * @param canvasWidth The width of the canvas in pixels
     * @param canvasHeight The height of the canvas in pixels
     * @return Pair of (x, y) coordinates on the canvas
     */
    fun latLngToPixel(
        lat: Double, lng: Double,
        centerLat: Double, centerLng: Double,
        zoom: Float,
        canvasWidth: Float, canvasHeight: Float
    ): Pair<Float, Float> {
        val latDiff = lat - centerLat
        val lngDiff = lng - centerLng

        // Meters per degree approximation
        val metersPerDegreeLat = 111320.0
        val metersPerDegreeLng = 111320.0 * cos(Math.toRadians(centerLat))

        val xOffset = (lngDiff * metersPerDegreeLng * zoom).toFloat()
        // Screen Y axis goes down, but latitude goes up. So we invert it.
        val yOffset = (-latDiff * metersPerDegreeLat * zoom).toFloat()

        return Pair(canvasWidth / 2f + xOffset, canvasHeight / 2f + yOffset)
    }

    /**
     * Converts a local 2D pixel coordinate back to a geographic coordinate.
     */
    fun pixelToLatLng(
        x: Float, y: Float,
        centerLat: Double, centerLng: Double,
        zoom: Float,
        canvasWidth: Float, canvasHeight: Float
    ): Pair<Double, Double> {
        val xOffset = x - canvasWidth / 2f
        val yOffset = y - canvasHeight / 2f

        val metersPerDegreeLat = 111320.0
        val metersPerDegreeLng = 111320.0 * cos(Math.toRadians(centerLat))

        val lngDiff = xOffset / (metersPerDegreeLng * zoom)
        val latDiff = -yOffset / (metersPerDegreeLat * zoom)

        return Pair(centerLat + latDiff, centerLng + lngDiff)
    }
}
