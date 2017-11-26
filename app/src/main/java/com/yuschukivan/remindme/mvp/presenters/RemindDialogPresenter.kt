package com.yuschukivan.remindme.mvp.presenters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.util.Log
import co.metalab.asyncawait.async
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.android.gms.location.places.Place
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.common.utils.RealmConfig
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.mvp.views.RemindDialogView
import com.yuschukivan.remindme.services.NotificationReceiver
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import android.app.PendingIntent
import android.content.Intent
import com.yuschukivan.remindme.activities.MapsActivity


/**
 * Created by Ivan on 5/30/2017.
 */
@InjectViewState
class RemindDialogPresenter : MvpPresenter<RemindDialogView>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    init {
        RemindApp.appComponent.inject(this)
    }

    val buttons = mutableMapOf<Int, Boolean>()
    lateinit var time: Date
    lateinit var date: Date
    var dateChanged = false
    var typeCahnged = false
    var priorityChanged = false
    var tempReminder = Reminder()
    var fragmentPosition = 0


    fun loadButtons(id: Long) {

        val reminder = realm.where(Reminder::class.java).equalTo("id", id).findFirst()

        val cal = Calendar.getInstance()
        cal.time = (reminder.date)
        val simpleTimeFormat = SimpleDateFormat("HH:mm")
        viewState.setTimeText(simpleTimeFormat.format(cal.time))
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        viewState.setDateText(simpleDateFormat.format(cal.time))

        val timeCal = Calendar.getInstance()
        timeCal.clear()
        timeCal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
        timeCal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
        this.time = timeCal.time

        val dateCal = Calendar.getInstance()
        dateCal.clear()
        dateCal.set(Calendar.MONTH, cal.get(Calendar.MONTH))
        dateCal.set(Calendar.YEAR, cal.get(Calendar.YEAR))
        timeCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
        this.date = dateCal.time

        val days = reminder.selectedDays
        buttons.clear()

        Util.Days.WEEKDAYS.forEachIndexed { index, day ->
            if(days.contains(index.toString())){
                buttons.put(index, true)
                viewState.addButton(index.toString(), day)
                viewState.highLight(index, true)
            }
            else {
                buttons.put(index, false)
                viewState.addButton(index.toString(), day)
                viewState.highLight(index, false)
            }
        }
    }

    fun  onDayClick(id: Int) {
        val state = buttons[id]
        viewState.showApply()
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

    fun  deleteReminder(id: Long) {
        realm.executeTransaction{
            val  reminder = realm.where(Reminder::class.java).equalTo("id", id).findFirst()
            val notifications = reminder.notifications.split(" ")
            for(notification in notifications) {
                if (notification.isNotEmpty()) {
                    NotificationReceiver.Companion.removeNotification(context, notification)
                }
            }
            reminder.deleteFromRealm()
            viewState.close()
        }
    }

    fun onTimeChoosen(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        val date = calendar.time
        this.time = date
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        dateChanged = true
        viewState.showApply()
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
        dateChanged = true
        viewState.showApply()
        viewState.setDateText(simpleDateFormat.format(date))
    }

    fun onShowChooseTime() {
        viewState.showTimePicker()
        viewState.showApply()
    }

    fun onShowChooseDate() {
        viewState.showDatePicker()
        viewState.showApply()
    }

    fun onSetType(type: Categoty, position: Int) {
        tempReminder.type = type.title
        val categories = realm.where(Categoty::class.java)
        tempReminder.category = categories.equalTo("id", type.id).findFirst()
        typeCahnged = true
        viewState.showApply()
    }

    fun onSetPriority(priority: String) {
        tempReminder.priority = priority
        priorityChanged = true
        viewState.showApply()
    }

    fun getCategories() {
        var data = mutableListOf<Categoty>()
        data.addAll(realm.where(Categoty::class.java).findAll())
        viewState.updateCategoriesSpinner(data)
    }

    fun onSetTypePosition(id: Long) {
        val category = realm.where(Reminder::class.java).equalTo("id", id).findFirst().category
        val categories = realm.where(Categoty::class.java).findAll()
        var position = categories.indexOf(category)
        viewState.setTypeSpinner(position)
        fragmentPosition = position + 1
    }

    fun onApply(id: Long, nameInput: String) {
        if(nameInput.isEmpty()) {
            viewState.showError("Please Enter Name")
            return
        }
        realm.executeTransaction {
            val reminder = realm.where(Reminder::class.java).equalTo("id", id).findFirst()
            reminder.notifications = ""
            reminder.title = nameInput
            if(dateChanged) reminder.date = Date(date.time + time.time + 3600000L)
            if(typeCahnged) reminder.priority = tempReminder.priority
            if(priorityChanged) reminder.type = tempReminder.type
            reminder.category = tempReminder.category
            reminder.selectedDays = ""
            val notifId = Util.NotificationUtil.generateNotification(context, nameInput, "", date.time + time.time + 3600000L, Random().nextInt())
            if(notifId != -1) reminder.notifications += " " + notifId
            buttons.forEach {
                if(it.value) {
                    reminder.notifications += " " + Util.NotificationUtil.generateNotificationRepeat(context, nameInput, "", it.key, Random().nextInt())
                    reminder.selectedDays += it.key.toString()
                }
            }
            viewState.close()
        }
    }
}
