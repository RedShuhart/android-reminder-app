package com.yuschukivan.remindme.features.nearby

import android.content.Intent
import com.arellomobile.mvp.MvpView
import com.yuschukivan.remindme.models.*

/**
 * Created by yusch on 29.10.2017.
 */
interface NearByView: MvpView {
    fun moveToCurrentLocation(longitude: Double, latitude: Double)
    fun showMarkers(closeReminders: MutableList<TaskLocationPair>)
    fun showInfo(task: Task)
    fun dispatchIntent(mapIntent: Intent)
    fun updateSubtasksView(subtasks: List<SubTask>)
    fun updateSubtasksItem(position: Int)
    fun showSubtasks()
    fun hideSubtasks()
    fun startEditing(intent: Intent)
    fun showActionsDialog(task: Task) {}
    fun setStateIcon(task: Task)
    fun hideInfo()
}
