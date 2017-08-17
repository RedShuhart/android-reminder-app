package com.yuschukivan.remindme.activities

import android.app.Activity
import android.content.Intent
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.location.places.ui.PlacePicker

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.common.utils.find

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null

    val toolbar by lazy { (findViewById(R.id.location_toolbar) as Toolbar).apply {
        setTitle("Location")
    } }

    val resetButton by lazy { findViewById(R.id.change_location) as Button }
    val nameText by lazy { findViewById(R.id.name) as TextView }
    val addressText by lazy { findViewById(R.id.address) as TextView }

    lateinit var name: String
    lateinit var address: String
    var lat: Double = 0.0
    var lon: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        name = intent.getStringExtra("name")
        address = intent.getStringExtra("address")
        lat = intent.getDoubleExtra("lat", 0.0)
        lon = intent.getDoubleExtra("long", 0.0)

        nameText.text = name
        addressText.text = address

        resetButton.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this), AddReminderActivity.PLACE_PICKER_REQUEST);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == AddReminderActivity.PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val location = LatLng(lat, lon)
        mMap!!.addMarker(MarkerOptions().position(location).title(address))
        mMap!!.moveCamera(CameraUpdateFactory.zoomTo(15.0f))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(location))
    }
}
