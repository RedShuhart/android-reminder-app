package com.yuschukivan.remindme.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.support.annotation.NonNull
import android.util.Log
import android.widget.Toast

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import java.io.Serializable

class GeoService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private val mGeofenceListsToAdd = mutableListOf<Geofence>()
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mAction: Action? = null
    private var transitionType: Int = 0

    private val pendingIntent: PendingIntent
        get() {
            val transitionService = Intent(this, ReceiveTransmitionsIntentService::class.java)
            return PendingIntent
                    .getService(this, 0, transitionService, PendingIntent.FLAG_UPDATE_CURRENT)
        }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Log.d("GEO", "Location service started")

        mAction = intent.getSerializableExtra(EXTRA_ACTION) as Action

        if (mAction == Action.ADD) {
            val newGeofence = intent.getSerializableExtra(EXTRA_GEOFENCE) as RemindGeoFence
            transitionType = newGeofence.transitionType
            mGeofenceListsToAdd.add(newGeofence.toGeofence())
        }

        mGoogleApiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build()
        mGoogleApiClient!!.connect()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onConnected(bundle: Bundle?) {
        if (mAction == Action.ADD) {
            val builder = GeofencingRequest.Builder()
            builder.setInitialTrigger(
                    if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                        GeofencingRequest
                                .INITIAL_TRIGGER_ENTER
                    else
                        GeofencingRequest.INITIAL_TRIGGER_EXIT)
            builder.addGeofences(mGeofenceListsToAdd)
            val build = builder.build()
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, build,
                    pendingIntent)
                    .setResultCallback(object : ResultCallback<Status> {
                        override fun onResult(@NonNull status: Status) {
                            if (status.isSuccess()) {
                                val msg = "Geofences added: " + status.getStatusMessage()
                                Log.e("GEO", msg)
                                Toast.makeText(this@GeoService, msg, Toast.LENGTH_SHORT)
                                        .show()
                            }
                            this@GeoService.onResult(status)
                        }
                    })

        }
    }

    override fun onConnectionSuspended(i: Int) {
        Log.e("GEO", "onConnectionSuspended i = " + i)
    }

    override fun onConnectionFailed(@NonNull connectionResult: ConnectionResult) {
        Log.e("GEO", "Location client connection failed: " + connectionResult.errorCode)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d("GEO", "Location service destroyed")
        super.onDestroy()
    }

    fun onResult(@NonNull status: Status) {
        Log.d("GEO", "Geofences onResult" + status.toString())
        if (status.isSuccess()) {
            mGoogleApiClient!!.disconnect()
            stopSelf()
        } else {
            val text = "Error while geofence: " + status.getStatusMessage()
            Log.e("GEO", text)
            Toast.makeText(this@GeoService, text, Toast.LENGTH_SHORT).show()
        }

    }

    enum class Action: Serializable {
        ADD, REMOVE
    }

    companion object {

        val EXTRA_REQUEST_IDS = "requestId"
        val EXTRA_GEOFENCE = "geofence"
        val EXTRA_ACTION = "action"
    }

}