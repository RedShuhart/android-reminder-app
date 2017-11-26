package com.yuschukivan.remindme.mvp.presenters

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import co.metalab.asyncawait.async
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.android.gms.location.places.Place
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.activities.MainActivity
import com.yuschukivan.remindme.common.utils.RealmConfig
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.mvp.views.AddReminderView
import com.yuschukivan.remindme.services.NotificationReceiver
import io.realm.Realm
import io.realm.RealmChangeListener
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus
import com.google.android.gms.cast.CastRemoteDisplayLocalService.startService
import android.R.attr.radius
import com.yuschukivan.remindme.services.GeoService
import com.yuschukivan.remindme.services.RemindGeoFence
import com.google.android.gms.location.Geofence




/**
 * Created by Ivan on 5/10/2017.
 */
@InjectViewState
class AddReminderPresenter @Inject constructor() : MvpPresenter<AddReminderView>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    init {
        RemindApp.appComponent.inject(this)
    }


    var tempReminder: Reminder = Reminder()
    lateinit var time: Date
    lateinit var date: Date
    var fragmentPosition = 0
    val buttons = mutableMapOf<Int, Boolean>()

    fun loadButtons() {

        val dateCal = Calendar.getInstance()
        dateCal.clear()
        dateCal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        dateCal.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE))
        val time = dateCal.time
        this.time = time
        val simpleTimeFormat = SimpleDateFormat("HH:mm")
        viewState.setTimeText(simpleTimeFormat.format(time))

        val timeCal = Calendar.getInstance()
        timeCal.clear()
        timeCal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH))
        timeCal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
        timeCal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        val date = timeCal.time
        this.date = date
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        viewState.setDateText(simpleDateFormat.format(date))

        buttons.clear()

        Util.Days.WEEKDAYS.forEachIndexed { index, day ->
            buttons.put(index, false)
            viewState.addButton(index.toString(), day)
        }
    }

    fun  onDayClick(id: Int) {
        val state = buttons[id]
            when(state ?: false) {
                true -> {
                    viewState.highLight(id, false)
                    buttons.put(id,false)
                }
                false -> {
                    viewState.highLight(id, true)
                    buttons.put(id,true)
                }
            }
    }

    fun onAddReminder(nameInput: String, dateInput: String, timeInput: String) {
        if(nameInput.isEmpty()) {
            viewState.showError("Please Enter Name")
            return
        }
        realm.executeTransaction {
            val reminder = realm.createObject(Reminder::class.java, System.currentTimeMillis())
            reminder.notifications = ""
            reminder.title = nameInput
            reminder.date = Date(date.time + time.time + 3600000L)
            reminder.priority = tempReminder.priority
            reminder.type = tempReminder.type
            reminder.category = tempReminder.category
            reminder.selectedDays = ""

            val notifId = Util.NotificationUtil.generateNotification(context, nameInput, "", date.time + time.time + 3600000L, Random().nextInt())
            Log.d("NotificationPresenter", "id: " + notifId)
            if(notifId != -1) reminder.notifications += "" + notifId
            buttons.forEach {
                if(it.value) {
                    reminder.notifications += " " + Util.NotificationUtil.generateNotificationRepeat(context, nameInput, "", it.key, Random().nextInt())
                    reminder.selectedDays += it.key.toString()
                }
            }
            viewState.finishActivity(fragmentPosition)
        }
    }

    fun onShowChooseTime() {
        viewState.showTimePicker()
    }

    fun onShowChooseDate() {
        viewState.showDatePicker()
    }

    fun onTimeChoosen(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        val date = calendar.time
        this.time = date
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        viewState.setTimeText(simpleDateFormat.format(date))
    }

    fun onDateChoosen(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val date = calendar.time
        this.date = date
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        viewState.setDateText(simpleDateFormat.format(date))
    }

    fun onSetType(type: Categoty, position: Int) {
        tempReminder.type = type.title
        tempReminder.category = realm.where(Categoty::class.java).equalTo("id", type.id).findFirst()
        fragmentPosition = position + 1
    }

    fun onSetPriority(priority: String) {
        tempReminder.priority = priority
    }

    fun getCategories() {
        var data = mutableListOf<Categoty>()
        data.addAll(realm.where(Categoty::class.java).findAll())
        viewState.updateCategoriesSpinner(data)
    }
}