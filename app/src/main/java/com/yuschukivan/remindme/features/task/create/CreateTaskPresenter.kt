package com.yuschukivan.remindme.features.task.create

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Html
import android.util.Log
import co.metalab.asyncawait.async
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.places.Place
import com.squareup.picasso.Picasso
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.SubTask
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.services.GeoService
import com.yuschukivan.remindme.services.RemindGeoFence
import io.realm.Realm
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by yusch on 08.11.2017.
 */
@InjectViewState
class CreateTaskPresenter @Inject constructor() : MvpPresenter<CreateTaskView>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    init {
        RemindApp.appComponent.inject(this)
    }


    var taskTime: Date? = null
    var taskDate: Date? = null
    var taskEstTime: Int? = null
    var reminderRepeatOn: String? = null
    var reminderDate: Date? = null
    var fragmentPosition: Int = 0
    var mapBitmap: Bitmap? = null
    var latLong: String? = null
    var address: String? = null
    lateinit var taskCategory: Categoty
    lateinit var taskPriority: String


    val subtasksList: MutableList<String> = mutableListOf()
    val simpledateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")

    fun onPreloadInfo() {
        preloadTime()
        viewState.setDateText(SimpleDateFormat("HH:mm dd/MM/yyyy").format(Calendar.getInstance()))
    }

    fun onCreateTask(name: String, notes: String) {
        if(name.isEmpty()) {
            viewState.showError("Task", "Please Enter Name")
            return
        }
        if(taskDate == null) {
            viewState.showError("Task", "Please Enter Due Date")
            return
        }
        realm.executeTransaction {
            val task = realm.createObject(Task::class.java, System.currentTimeMillis())
            task.name = name
            task.description = notes

            taskTime?.let { time ->
                taskDate?.let { date ->
                    task.dueDate = Date(date.time + time.time + 3600000L)
                }
            }

            task.priority = taskPriority
            task.category = taskCategory

            reminderDate?.let {
                task.reminder = createReminder(name, it, reminderRepeatOn!!, notes)
            }
            subtasksList.forEach { name ->
                val subtask = realm.createObject(SubTask::class.java, Random().nextInt())
                subtask.description = name
                subtask.completed = false
                task.subTasks.add(subtask)
            }
            taskEstTime?.let {
                task.estimatedTime = taskEstTime
            }
            latLong?.let {
                task.latLong = it
                val coords = it.split(",")
                val transitionType = Geofence.GEOFENCE_TRANSITION_ENTER or  Geofence.GEOFENCE_TRANSITION_EXIT
                val myGeofence = RemindGeoFence(it, coords[0].toDouble(), coords[1].toDouble(), 500f, transitionType)

                val geofencingService = Intent(context, GeoService::class.java)
                geofencingService.setAction(Math.random().toString())
                geofencingService.putExtra(GeoService.EXTRA_ACTION, GeoService.Action.ADD)
                geofencingService.putExtra(GeoService.EXTRA_GEOFENCE, myGeofence)

                context.startService(geofencingService)
            }
            address?.let { task.address = it }
            if(mapBitmap != null) {
                val stream = ByteArrayOutputStream()
                mapBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                task.mapImage = stream.toByteArray()
            }
            viewState.finishWithOk(fragmentPosition)
        }
    }

    fun onAddReminder(time: String, selectedDays: String) {
        reminderRepeatOn = selectedDays
        reminderDate = SimpleDateFormat("HH:mm dd/MM/yyyy").parse(time)

        var displaySelected = ""

        Util.Days.WEEKDAYS.forEachIndexed { index, day ->
            if(selectedDays.contains(index.toString())){
                displaySelected += "<b><font color='#1B5E20' size='18'>$day </font></b>"
            }
            else {
                displaySelected += "<font size='14'>$day </font>"
            }
        }
        viewState.showReminderInfo(time, displaySelected);
    }
    fun onDeleteReminder() {
        reminderRepeatOn = null
        reminderDate = null
        viewState.removeReminderInfo()
    }

    fun onAssignReminder() {
        var passedTime = ""
        passedTime = simpledateFormat.format(Calendar.getInstance().time)
        reminderDate?.let { passedTime = simpledateFormat.format(it) }
        viewState.showAssignReminderDialog(passedTime, reminderRepeatOn, reminderDate != null)
    }

    fun onDateChoosen(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val date = calendar.time
        taskDate = date
        viewState.showTimePicker()
    }

    fun onTimeChoosen(hourOfDay: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        val date = calendar.time
        taskTime = date
        taskTime?.let { time ->
            taskDate?.let { date ->
                val dateToShow = SimpleDateFormat("HH:mm dd/MM/yyyy").format(Date(date.time + time.time + 3600000L))
                viewState.setDateText(dateToShow)
            }
        }
    }

    fun onEstChosen(hourOfDay: Int, minute: Int) {
        taskEstTime = minute + hourOfDay * 60
        viewState.showEstText("${hourOfDay}H ${minute} MIN")
    }

    fun getCategories() {
        var data = mutableListOf<Categoty>()
        data.addAll(realm.where(Categoty::class.java).findAll())
        viewState.updateCategoriesSpinner(data)
    }

    fun onAddSubtask() {
        viewState.showCreateSubtaskDialog()
    }

    fun onCreateSubtask(name: String) {
        if(subtasksList.contains(name)) {
            viewState.showError("Subtask", "Subtask already exists")
            return
        }
        subtasksList.add(name)
        viewState.addSubtaskToView(subtasksList)
    }

    fun onRemoveSubtask(query: String) {
        subtasksList.remove(query)
        viewState.removeSubtaskFromView(subtasksList)
    }

    fun onShowChooseDate() {
        viewState.showDatePicker()
    }

    fun onShowChooseEst() {
        viewState.showEstPicker()
    }

    fun onLocationSelected(place: Place) {
        async {
            address = place.address.toString()
           // val url = URL("https://maps.googleapis.com/maps/api/staticmap?center=${place.latLng.latitude},${place.latLng.longitude}&zoom=15&size=850x200&markers=color:blue%7C${place.latLng.latitude},${place.latLng.longitude}")
            //mapBitmap = await { BitmapFactory.decodeStream(url.openConnection().getInputStream()) }
            val path = "https://maps.googleapis.com/maps/api/staticmap?center=${place.latLng.latitude},${place.latLng.longitude}&zoom=15&size=850x200&markers=color:blue%7C${place.latLng.latitude},${place.latLng.longitude}"
            val mBit = await { Picasso.with(context).load(path).get() }
            Log.d("MAP", mBit.toString() + mBit.byteCount)
            latLong = place.latLng.latitude.toString() + "," + place.latLng.longitude.toString()
            mapBitmap = mBit
            viewState.setMapImage(mBit)
        }
    }

    fun onRemoveMap() {
        mapBitmap = null
        viewState.removeMapImage()
    }

    fun onSetType(categoty: Categoty, selectedItemPosition: Int) {
        taskCategory = categoty
        fragmentPosition = selectedItemPosition + 1
    }

    fun onSetPriority(priority: String) {
        taskPriority = priority
    }

    private fun preloadTime() {
        val dateCal = Calendar.getInstance()
        dateCal.clear()
        dateCal.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        dateCal.set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE))
        val time = dateCal.time
        taskTime = time

        val timeCal = Calendar.getInstance()
        timeCal.clear()
        timeCal.set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH))
        timeCal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
        timeCal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        val date = timeCal.time
        taskDate = date
    }

    private fun createReminder(name: String, date: Date, repeats: String, notes: String): Reminder {
        val reminder = realm.createObject(Reminder::class.java, System.currentTimeMillis())
        reminder.notifications = ""
        reminder.title = "Assigned to: " + name
        reminder.date = date
        reminder.priority = taskPriority
        reminder.type = taskCategory.title
        reminder.category = taskCategory
        reminder.selectedDays = ""

        val notifId = Util.NotificationUtil.generateNotification(context, name, notes, date.time, Random().nextInt())
        Log.d("NotificationPresenter", "id: " + notifId)
        if(notifId != -1) reminder.notifications += "" + notifId
        reminderRepeatOn?.let { repeat ->
            Util.Days.WEEKDAYS.forEachIndexed { index, day ->
                if(repeat.contains(index.toString())){
                    reminder.notifications += " " + Util.NotificationUtil.generateNotificationRepeat(context, name, notes, index, Random().nextInt())
                    reminder.selectedDays += index.toString()
                }
            }
        }
        return reminder
    }
}