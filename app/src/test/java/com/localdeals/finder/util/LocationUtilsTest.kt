package com.localdeals.finder.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationUtilsTest {

    @Test
    fun haversineDistance_samePoint_isZero() {
        val lat = 23.8103
        val lon = 90.4125
        val distance = LocationUtils.haversineDistance(lat, lon, lat, lon)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun haversineDistance_dhakaToChittagong_isCorrect() {
        val dhakaLat = 23.8103
        val dhakaLon = 90.4125
        val chittagongLat = 22.3569
        val chittagongLon = 91.7832
        
        val distance = LocationUtils.haversineDistance(dhakaLat, dhakaLon, chittagongLat, chittagongLon)
        // Distance is roughly ~210 km
        assertEquals(214.0, distance, 5.0) 
    }

    @Test
    fun calculateBearing_north_isZero() {
        val lat1 = 0.0
        val lon1 = 0.0
        val lat2 = 1.0
        val lon2 = 0.0
        val bearing = LocationUtils.calculateBearing(lat1, lon1, lat2, lon2)
        assertEquals(0.0, bearing, 0.001)
    }

    @Test
    fun calculateBearing_east_is90() {
        val lat1 = 0.0
        val lon1 = 0.0
        val lat2 = 0.0
        val lon2 = 1.0
        val bearing = LocationUtils.calculateBearing(lat1, lon1, lat2, lon2)
        assertEquals(90.0, bearing, 0.001)
    }
}
