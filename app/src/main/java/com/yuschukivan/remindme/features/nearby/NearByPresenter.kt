package com.yuschukivan.remindme.features.nearby

import android.content.Context
import com.arellomobile.mvp.MvpPresenter
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
import java.lang.Math.*
import android.content.Intent
import com.yuschukivan.remindme.features.task.edit.EditTaskActivity
import com.yuschukivan.remindme.models.*
import com.yuschukivan.remindme.services.NotificationReceiver
import java.util.*


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
    lateinit var closeTasks: MutableList<TaskLocationPair>
    var subtasksShown = false

    lateinit var subtasksList: MutableList<SubTask>
    lateinit var taskShown: Task

    fun onMapLoad() {
        loadNearByReminders()
        viewState.showMarkers(closeTasks)
    }

    fun getCurrentLocation( ) {
        requestCoordinates { longitude, latitude ->
            this.myLongitude = longitude
            this.myLatitude = latitude
            viewState.moveToCurrentLocation(longitude, latitude)
        }
    }

    fun onTaskInfo(id: String) {
        taskShown = realm.where(Task::class.java).equalTo("id", id.toLong()).findFirst()
        subtasksList = mutableListOf()
        subtasksList.addAll(taskShown.subTasks)
        viewState.showInfo(taskShown)
        viewState.updateSubtasksView(subtasksList)
    }

    fun onTaskInfo() {
        subtasksList = mutableListOf()
        subtasksList.addAll(taskShown.subTasks)
        viewState.showInfo(taskShown)
        viewState.updateSubtasksView(subtasksList)
    }

    fun completesSubtask(subtaskId: Long, position: Int) {

        realm.executeTransaction {
            val subtask = realm.where(SubTask::class.java).equalTo("id", subtaskId).findFirst()
            subtask.completed = !subtask.completed
            updateSubtasks(taskShown)
        }
    }

    fun updateSubtasks(task: Task) {
        viewState.updateSubtasksView(task.subTasks)
    }

    fun showSubtasks(task: Task) {
        if(task.subTasks.isNotEmpty()) {
            subtasksShown = !subtasksShown
            viewState.showSubtasks()
        }
    }

    fun hideSubtasks(task: Task) {
        subtasksShown = !subtasksShown
        viewState.hideSubtasks()
    }


    private fun loadNearByReminders() {
        closeTasks = mutableListOf()
        val tasks = realm.where(Task::class.java).findAll()

        tasks.filter { it.doneDate == null }.forEach { task ->
            task.latLong?.let {
                val longLat = it.split(",")
                val reminderLat = longLat[0].toDouble()
                val reminderLong = longLat[1].toDouble()
                closeTasks.add(TaskLocationPair(task, createMarker(reminderLat, reminderLong, task.id, task.priority)))
            }
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

    fun onEditTask(task: Task) {
        val intent = Intent(context, EditTaskActivity::class.java)
        intent.putExtra("task_id", task.id)
        viewState.startEditing(intent)
    }

    fun onDeleteTask(task: Task) {
        realm.executeTransaction {
            task.reminder?.let {
                val notifications = it.notifications.split(" ")
                for(notification in notifications) {
                    if (notification.isNotEmpty()) {
                        NotificationReceiver.Companion.removeNotification(context, notification)
                    }
                }
                it.deleteFromRealm()
            }
            task.deleteFromRealm()
            viewState.hideInfo()
        }
    }

    fun onDoneTask(task: Task) {
        realm.executeTransaction {
            task.doneDate = Calendar.getInstance().time
            task.subTasks.forEach { it.completed = true }
        }
        viewState.showInfo(task)
    }

    fun onUndoTask(task: Task) {
        realm.executeTransaction {
            task.doneDate = null
            task.subTasks.forEach { it.completed = false }
        }
        viewState.showInfo(task)
    }

    fun onShowActions(): Boolean {
        viewState.showActionsDialog(taskShown)
        return true
    }


}
