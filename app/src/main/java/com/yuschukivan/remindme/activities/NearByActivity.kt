package com.yuschukivan.remindme.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.mvp.views.NearByView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.ReminderLocationPair
import com.yuschukivan.remindme.mvp.presenters.NearByPresenter
import java.text.SimpleDateFormat


/**
 * Created by yusch on 29.10.2017.
 */
class NearByActivity: BaseActivity(), OnMapReadyCallback, NearByView, GoogleMap.OnMarkerClickListener {


    @InjectPresenter
    lateinit var presenter: NearByPresenter

    lateinit var mMap: GoogleMap

    val toolbar by lazy { (findViewById(R.id.location_toolbar) as Toolbar).apply {
        title = "Near By"
    } }

    val reminderCard by lazy { findViewById(R.id.card_view) as CardView}
    val reminderName by lazy { findViewById(R.id.title_text) as TextView }
    val reminderInfo by lazy { findViewById(R.id.description_text) as TextView }
    val reminderPriority by lazy { findViewById(R.id.priority_text) as TextView }
    val reminderDate by lazy { findViewById(R.id.date_text) as TextView }
    val reminderAddress by lazy { findViewById(R.id.address_text) as TextView }
    val navigationButton by lazy { findViewById(R.id.buttonNavigate) as ImageButton}


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby)
        setSupportActionBar(toolbar)
        reminderCard.visibility = View.GONE
        navigationButton.visibility = View.GONE
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true;
        presenter.getCurrentLocation()
        presenter.onMapLoad()
    }

    override fun showMarkers(closeReminders: MutableList<ReminderLocationPair>) {
        closeReminders.forEach { reminder ->
            mMap.addMarker(reminder.marker)
        }
    }

    override fun moveToCurrentLocation(longitude: Double, latitude: Double) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 13f))

        val cameraPosition = CameraPosition.Builder()
                .target(LatLng(latitude, longitude))      // Sets the center of the map to location user
                .zoom(17f)                   // Sets the zoom
                .bearing(90f)                // Sets the orientation of the camera to east
                .tilt(40f)                   // Sets the tilt of the camera to 30 degrees
                .build()                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        presenter.onReminderInfo(marker.title)
        return true
    }

    override fun showInfo(reminder: Reminder) {
        when(reminder.priority) {
            Util.Priority.HIGH -> reminderPriority.setTextColor(Color.parseColor("#D50000"))
            Util.Priority.NORMAL -> reminderPriority.setTextColor(Color.parseColor("#f57c00"))
            Util.Priority.LOW -> reminderPriority.setTextColor(Color.parseColor("#1B5E20"))
        }
        reminderName.text = reminder.title
        reminderInfo.text = reminder.description
        reminderPriority.text = reminder.priority
        reminderDate.text = SimpleDateFormat("dd/MM/yyyy HH:mm").format(reminder.date)
        reminderAddress.text = reminder.address
        reminderCard.visibility = View.VISIBLE
        navigationButton.visibility = View.VISIBLE
        navigationButton.setOnClickListener { presenter.findRoutes(reminder.latLong, reminder.address) }
    }

    override fun dispatchIntent(mapIntent: Intent) {
        startActivity(intent)
    }
}