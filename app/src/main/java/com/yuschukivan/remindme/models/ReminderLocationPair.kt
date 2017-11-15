package com.yuschukivan.remindme.models

import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Created by yusch on 29.10.2017.
 */
data class ReminderLocationPair(
        val reminder: Reminder,
        val marker: MarkerOptions
)