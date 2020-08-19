package com.example.myapplication.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.pow
import kotlin.math.roundToInt

fun calculateDistance(rssi: Double): Double {
    /*  Selon cette etude, nous pouvons supposer les meilleurs coefficients pour approximer
        une distance avec la technique d'analyse de l'ajustement d'une courbe
        source: https://www.radiusnetworks.com/2018-11-19-fundamentals-of-beacon-ranging */
    val coefficient1 = 0.89976
    val coefficient2 = 7.7095
    val coefficient3 = 0.111
    /* Cette meme etude pretend que a 1 metre de distance, la puissance moyenne serait de 59dBm */
    val txPower = 59

    val ratio = rssi / txPower
    val distance = when {
        ratio < 1.0 -> {
            // ratio par defaut
            ratio.pow(10.0)
        }
        ratio >= 1.0 -> {
            // rssi > txPower
            (coefficient1) * ratio.pow(coefficient2) + coefficient3
        }
        else -> {
            0.0
        }
    }
    // arrondir a 2 decimales pres
    return (distance * 100.0).roundToInt() / 100.0
}

// bearing (direction) -> angle 0 = North, 90 - East, 180 - South, 270 - West
fun newLocationOnMap(longitude: Double, latitude: Double, distanceInMeters: Double, bearing: Double): LatLng {
    /* source: http://www.movable-type.co.uk/scripts/latlong.html */
    val radiusOfEarthInMeters = 6371000
    val latitudeRadians = Math.toRadians(latitude)
    val longitudeRadians = Math.toRadians(longitude)
    val bearingRadians = Math.toRadians(bearing)
    val ratio = distanceInMeters / radiusOfEarthInMeters
    val newLatitude = kotlin.math.asin(
        kotlin.math.sin(latitudeRadians) * kotlin.math.cos(ratio) + kotlin.math.cos(latitudeRadians) * kotlin.math.sin(ratio) * kotlin.math.cos(
            bearingRadians
        )
    )
    val a: Double = kotlin.math.atan2(
        kotlin.math.sin(bearingRadians) * kotlin.math.sin(ratio) * kotlin.math.cos(latitudeRadians),
        kotlin.math.cos(ratio) - kotlin.math.sin(latitudeRadians) * kotlin.math.sin(newLatitude)
    )
    val newLongitude: Double = (longitudeRadians + a + 3 * Math.PI) % (2 * Math.PI) - Math.PI
    return LatLng(Math.toDegrees(newLatitude), Math.toDegrees(newLongitude))
}