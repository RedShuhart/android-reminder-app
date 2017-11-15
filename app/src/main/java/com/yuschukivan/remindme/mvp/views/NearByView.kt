package com.yuschukivan.remindme.mvp.views

import android.content.Intent
import com.arellomobile.mvp.MvpView
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.ReminderLocationPair

/**
 * Created by yusch on 29.10.2017.
 */
interface NearByView: MvpView {
    fun moveToCurrentLocation(longitude: Double, latitude: Double)
    fun showMarkers(closeReminders: MutableList<ReminderLocationPair>)
    fun showInfo(reminder: Reminder)
    fun dispatchIntent(mapIntent: Intent)
}
