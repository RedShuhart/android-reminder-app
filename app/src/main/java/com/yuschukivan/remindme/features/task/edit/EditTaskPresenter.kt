package com.yuschukivan.remindme.features.task.edit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.yuschukivan.remindme.services.NotificationReceiver
import com.yuschukivan.remindme.services.RemindGeoFence
import io.realm.Realm
import java.io.ByteArrayOutputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by yusch on 21.11.2017.
 */
@InjectViewState
class EditTaskPresenter @Inject constructor(): MvpPresenter<EditTaskView>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    init {
        RemindApp.appComponent.inject(this)
    }



    lateinit var taskTime: Date
    lateinit var taskDate: Date
    var taskEstTime: Int? = null
    var reminderRepeatOn: String? = null
    var reminderDate: Date? = null
    var fragmentPosition: Int = 0
    var mapBitmap: Bitmap? = null
    var latLong: String? = null
    var address: String? = null
    lateinit var taskCategory: Categoty
    lateinit var taskPriority: String
    var reminderChanged: Boolean = false

    lateinit var editingTask: Task

    val priorityCategories: List<String> = ArrayList<String>().apply {
        add(Util.Priority.LOW)
        add(Util.Priority.NORMAL)
        add(Util.Priority.HIGH)
    }

    companion object {
        data class SubtaskHolder(val id: Long, val name: String, val done: Boolean)
    }
    val subtasksList: MutableList<SubtaskHolder> = mutableListOf()
    val simpledateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")

    fun onPreloadInfo(id: Long) {
        editingTask = realm.where(Task::class.java).equalTo("id", id).findFirst()
        preloadTime(editingTask)
        setCategoryPosition(editingTask)
        preloadParams(editingTask)

        viewState.setDateText(simpledateFormat.format(editingTask.dueDate))
    }

    private fun preloadTime(task: Task) {

        val cal = Calendar.getInstance()
        cal.time = (task.dueDate)

        val timeCal = Calendar.getInstance()
        timeCal.clear()
        timeCal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
        timeCal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
        taskTime = timeCal.time

        val dateCal = Calendar.getInstance()
        dateCal.clear()
        dateCal.set(Calendar.MONTH, cal.get(Calendar.MONTH))
        dateCal.set(Calendar.YEAR, cal.get(Calendar.YEAR))
        dateCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
        taskDate = dateCal.time
    }

    private fun setCategoryPosition(task: Task) {
        val categories = realm.where(Categoty::class.java).findAll()
        var position = categories.indexOf(task.category)
        viewState.setTypeSpinner(position)
        viewState.setPriotirySpinner(priorityCategories.indexOf(task.priority))
        fragmentPosition = position + 1
    }

    private fun preloadParams(task: Task) {
        viewState.showName(task.name)
        viewState.showDesc(task.description)
        task.estimatedTime?.let {
            taskEstTime = it
            viewState.showEstText("${it / 60}H ${it % 60}MIN")
        }

        task.category?.let {
            taskCategory = it
            setCategoryPosition(task)
        }
        task.address?.let { address = it}
        task.latLong?.let { latLong = it}
        task.mapImage?.let {
            mapBitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            viewState.setMapImage(mapBitmap!!)
        }
        if(task.subTasks.isNotEmpty()) {
            subtasksList.addAll( task.subTasks.map { SubtaskHolder(it.id, it.description, it.completed)}.toMutableList())
            viewState.addSubtaskToView(subtasksList.map { it.name })
        }
        task.reminder?.let {
            reminderDate = it.date
            reminderRepeatOn = it.selectedDays
            val stringDate = SimpleDateFormat("HH:mm dd/MM/yyyy").format(reminderDate)
            var displaySelected = ""

            Util.Days.WEEKDAYS.forEachIndexed { index, day ->
                if(reminderRepeatOn!!.contains(index.toString())){
                    displaySelected += "<b><font color='#1B5E20' size='18'>$day </font></b>"
                }
                else {
                    displaySelected += "<font size='14'>$day </font>"
                }
            }
            viewState.showReminderInfo(stringDate, displaySelected)
        }
    }
    fun onSaveTask(name: String, notes: String) {
        if(name.isEmpty()) {
            viewState.showError("Task", "Please Enter Name")
            return
        }
        realm.executeTransaction {
            editingTask.name = name
            editingTask.description = notes
            editingTask.dueDate = Date(taskDate.time + taskTime.time + 3600000L)
            editingTask.priority = taskPriority
            editingTask.category = taskCategory

            if(reminderChanged) {
                editingTask.reminder?.let {
                    val reminder = realm.where(Reminder::class.java).equalTo("id", it.id).findFirst()
                    reminder.deleteFromRealm()
                }
                reminderDate?.let {
                    editingTask.reminder = createReminder(name, it, reminderRepeatOn!!, notes)
                }
            }

            editingTask.subTasks.map { it.id }.forEach {
                realm.where(SubTask::class.java).equalTo("id", it).findFirst().deleteFromRealm()
            }

            subtasksList.forEach { st ->
                val subtask = realm.createObject(SubTask::class.java, if(st.id == 0L) Random().nextInt() else st.id)
                subtask.description = st.name
                subtask.completed = st.done
                editingTask.subTasks.add(subtask)
            }

            taskEstTime?.let {
                editingTask.estimatedTime = taskEstTime
            }

            latLong?.let {
                if(it.isNotEmpty()){
                    editingTask.latLong = it
                    val coords = it.split(",")
                    val transitionType = Geofence.GEOFENCE_TRANSITION_ENTER or  Geofence.GEOFENCE_TRANSITION_EXIT
                    val myGeofence = RemindGeoFence(it, coords[0].toDouble(), coords[1].toDouble(), 100f, transitionType)

                    val geofencingService = Intent(context, GeoService::class.java)
                    geofencingService.setAction(Math.random().toString())
                    geofencingService.putExtra(GeoService.EXTRA_ACTION, GeoService.Action.ADD)
                    geofencingService.putExtra(GeoService.EXTRA_GEOFENCE, myGeofence)

                    context.startService(geofencingService)
                }
            }

            address?.let { editingTask.address = it }
            if(mapBitmap != null) {
                val stream = ByteArrayOutputStream()
                mapBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                editingTask.mapImage = stream.toByteArray()
            }

            viewState.finishWithOk(fragmentPosition)
        }
    }

    fun onAddReminder(time: String, selectedDays: String) {
        reminderChanged = true
        reminderRepeatOn = selectedDays
        reminderDate = simpledateFormat.parse(time)

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
        realm.executeTransaction {
            editingTask.reminder?.let {
                val reminder = realm.where(Reminder::class.java).equalTo("id", it.id).findFirst()
                reminder.deleteFromRealm()
            }
        }

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
        val dateToShow = simpledateFormat.format(Date(taskDate.time + taskTime.time + 3600000L))
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
        if(subtasksList.find { it.name == name } != null) {
            viewState.showError("Subtask", "Subtask already exists")
            return
        }
        subtasksList.add(SubtaskHolder(0, name, false))
        viewState.addSubtaskToView(subtasksList.map { it.name })
    }

    fun onRemoveSubtask(query: String) {
        subtasksList.remove(subtasksList.find { it.name == query })
        viewState.removeSubtaskFromView(subtasksList.map { it.name })
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
        return reminder;
    }
}