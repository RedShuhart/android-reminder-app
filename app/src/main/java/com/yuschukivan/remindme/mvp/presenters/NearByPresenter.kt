package com.yuschukivan.remindme.mvp.presenters

import android.content.Context
import com.arellomobile.mvp.MvpPresenter
import com.yuschukivan.remindme.mvp.views.NearByView
import io.realm.Realm
import javax.inject.Inject
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import com.arellomobile.mvp.InjectViewState
import com.google.android.gms.maps.model.*
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.ReminderLocationPair
import java.lang.Math.*
import android.content.Intent




/**
 * Created by yusch on 29.10.2017.
 */
@InjectViewState
class NearByPresenter @Inject constructor():  MvpPresenter<NearByView>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    init {
        RemindApp.appComponent.inject(this)
    }

    var myLongitude: Double = 0.0
    var myLatitude: Double = 0.0
    lateinit var closeReminders: MutableList<ReminderLocationPair>

    fun onMapLoad() {
        loadNearByReminders()
        viewState.showMarkers(closeReminders)
    }

    fun onReminderInfo(id: String) {
        val reminder = closeReminders.find { it.reminder.id == id.toLong() }
        viewState.showInfo(reminder!!.reminder)
    }

    fun getCurrentLocation( ) {
        requestCoordinates { longitude, latitude ->
            this.myLongitude = longitude
            this.myLatitude = latitude
            viewState.moveToCurrentLocation(longitude, latitude)
        }
    }

    private fun loadNearByReminders() {
        closeReminders = mutableListOf()
        val reminders = realm.where(Reminder::class.java).findAll()
        reminders.forEach { reminder ->
            val longLat = reminder.latLong.split(",")
            val reminderLat = longLat[0].toDouble()
            val reminderLong = longLat[1].toDouble()
            closeReminders.add(ReminderLocationPair(reminder, createMarker(reminderLat, reminderLong, reminder.id, reminder.priority)))
        }
    }


    private fun createMarker(latitude: Double, longitude: Double, address: Long, priority: String) = MarkerOptions()
            .position(LatLng(latitude, longitude))
            .title(address.toString())
            .icon(BitmapDescriptorFactory.defaultMarker(
                    when(priority) {
                        Util.Priority.HIGH -> BitmapDescriptorFactory.HUE_RED
                        Util.Priority.NORMAL -> BitmapDescriptorFactory.HUE_YELLOW
                        Util.Priority.LOW -> BitmapDescriptorFactory.HUE_GREEN
                        else -> BitmapDescriptorFactory.HUE_GREEN
                    }
            ))


    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
        val R = 6372.8e3
        val λ1 = toRadians(lat1)
        val λ2 = toRadians(lat2)
        val Δλ = toRadians(lat2 - lat1)
        val Δφ = toRadians(lon2 - lon1)
        return ceil(2 * R * asin(sqrt(pow(sin(Δλ / 2), 2.0) + pow(sin(Δφ / 2), 2.0) * cos(λ1) * cos(λ2)))).toInt()
    }

    private fun requestCoordinates(callback: (Double, Double) -> Unit) {
        val lm: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val criteria: Criteria = Criteria().apply {
            accuracy = Criteria.ACCURACY_MEDIUM
        }

        val locationListener = object: LocationListener {
            override fun onLocationChanged(location: Location) {

                val longitude = location.getLongitude()
                val latitude = location.getLatitude()
                callback(longitude, latitude)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                //viewState.showMessage("Houston we have a problem")
            }

            override fun onProviderEnabled(provider: String?) {
                //viewState.showMessage("Houston we have a problem")
            }

            override fun onProviderDisabled(provider: String?) {
                //viewState.showMessage("Houston we have a problem")
            }
        }

        lm.requestSingleUpdate(criteria,locationListener, Looper.getMainLooper())
    }

    fun findRoutes(latLong: String, address: String) {
        val coords = latLong.split(",")
        val latitude = coords[0].toDouble()
        val longitude = coords[1].toDouble()
        val gmmIntentUri = Uri.parse("geo:$longitude,$latitude?q=${Uri.encode(address)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.`package` = "com.google.android.apps.maps"
        context.startActivity(mapIntent);
    }
}
