package com.yuschukivan.remindme.services

import com.google.android.gms.location.Geofence
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import com.yuschukivan.remindme.R.mipmap.ic_launcher
import android.app.PendingIntent
import com.yuschukivan.remindme.activities.MainActivity
import android.content.Intent
import com.google.android.gms.location.GeofencingEvent
import android.app.IntentService
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.yuschukivan.remindme.R


class ReceiveTransmitionsIntentService : IntentService(TRANSITION_INTENT_SERVICE) {

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.e(TRANSITION_INTENT_SERVICE, "Location Services error: " + geofencingEvent.errorCode)
            return
        }

        val transitionType = geofencingEvent.geofenceTransition

        val triggeredGeofences = geofencingEvent.triggeringGeofences

        for (geofence in triggeredGeofences) {
            Log.d("GEO", "onHandle:" + geofence.requestId)
            processGeofence(geofence, transitionType)
        }
    }

    private fun processGeofence(geofence: Geofence, transitionType: Int) {

        val notificationBuilder = NotificationCompat.Builder(applicationContext)

        val openActivityIntetnt = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        val id = Integer.parseInt(geofence.requestId)

        val transitionTypeString = getTransitionTypeString(transitionType)
        notificationBuilder
                .setContentTitle("Geofence id: " + id)
                .setContentText("Transition type: " + transitionTypeString)
                .setVibrate(longArrayOf(500, 500))
                .setContentIntent(openActivityIntetnt)
                .setAutoCancel(true)

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(transitionType * 100 + id, notificationBuilder.build())

        Log.d("GEO", String.format("notification built:%d %s", id, transitionTypeString))
    }

    private fun getTransitionTypeString(transitionType: Int): String {
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> return "enter"
            Geofence.GEOFENCE_TRANSITION_EXIT -> return "exit"
            Geofence.GEOFENCE_TRANSITION_DWELL -> return "dwell"
            else -> return "unknown"
        }
    }

    companion object {

        val TRANSITION_INTENT_SERVICE = "TransitionsService"
    }


}