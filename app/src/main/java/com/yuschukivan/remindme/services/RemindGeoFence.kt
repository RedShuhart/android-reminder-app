package com.yuschukivan.remindme.services

import com.google.android.gms.location.Geofence
import java.io.Serializable


class RemindGeoFence(private val id: String, private val latitude: Double, private val longitude: Double, private val radius: Float, val transitionType: Int) : Serializable {

    fun toGeofence(): Geofence {
        return Geofence.Builder()
                .setRequestId(id.toString())
                .setTransitionTypes(transitionType)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(ONE_MINUTE.toLong())
                .build()
    }

    companion object {
        private val ONE_MINUTE = 60000
    }
}