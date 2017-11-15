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


    lateinit var taskTime: Date
    lateinit var taskDate: Date
    var taskEstTime: Int = 0
    var reminderRepeatOn: String = ""
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
        realm.executeTransaction {
            val task = realm.createObject(Task::class.java, System.currentTimeMillis())
            task.name = name
            task.description = notes
            task.dueDate = Date(taskDate.time + taskTime.time + 3600000L)
            task.priority = taskPriority
            task.category = taskCategory
            subtasksList.forEach { name ->
                val subtask = realm.createObject(SubTask::class.java, Random().nextInt())
                subtask.description = name
                subtask.completed = false
                task.subTasks.add(subtask)
            }
            latLong?.let {
                task.latLong = it
                val coords = it.split(",")
                val transitionType = Geofence.GEOFENCE_TRANSITION_ENTER or  Geofence.GEOFENCE_TRANSITION_EXIT
                val myGeofence = RemindGeoFence(it, coords[0].toDouble(), coords[1].toDouble(), 100f, transitionType)

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
                displaySelected += "<font color='#1B5E20' size='15'>$day </font>"
            }
            else {
                displaySelected += "<font size='14'>$day </font>"
            }
        }
        viewState.showReminderInfo(time, displaySelected);
    }

    fun onAssignReminder() {
        var passedTime = ""
        passedTime = simpledateFormat.format(Calendar.getInstance().time)
        reminderDate?.let { passedTime = simpledateFormat.format(it) }
        viewState.showAssignReminderDialog(passedTime, reminderRepeatOn)
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
        val dateToShow = SimpleDateFormat("HH:mm dd/MM/yyyy").format(Date(taskDate.time + taskTime.time + 3600000L))
        viewState.setDateText(dateToShow)
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
        viewState.addSubtaskToView(name)
    }

    fun onRemoveSubtask(query: String) {
        subtasksList.remove(query)
        viewState.removeSubtaskFromView(query)
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
            val url = URL("https://maps.googleapis.com/maps/api/staticmap?center=${place.latLng.latitude},${place.latLng.longitude}&zoom=15&size=850x200&markers=color:red%7C${place.latLng.latitude},${place.latLng.longitude}")
            mapBitmap = await { BitmapFactory.decodeStream(url.openConnection().getInputStream()) }
            latLong = place.latLng.latitude.toString() + "," + place.latLng.longitude.toString()
            viewState.setMapImage(mapBitmap!!)
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
}