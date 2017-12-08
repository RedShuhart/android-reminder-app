package com.yuschukivan.remindme.features.task.create

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by yusch on 08.11.2017.
 */
interface CreateTaskView: MvpView {
    fun updateCategoriesSpinner(data: MutableList<Categoty>)
    fun showTimePicker()
    fun showDatePicker()
    fun showEstPicker()
    fun showEstText(time: String)
    fun setDateText(date: String)
    fun showError(title: String, message: String)
    fun showReminderInfo(time: String, repeats: String)
    fun removeReminderInfo()
    fun addSubtaskToView(names: List<String>)
    fun removeSubtaskFromView(names: List<String>)
    fun showCreateSubtaskDialog()
    fun setMapImage(map: Bitmap)
    fun removeMapImage()
    @StateStrategyType(SkipStrategy::class)
    fun finishWithOk(position: Int)
    fun showAssignReminderDialog(date: String, repeats: String?, wasAssigned: Boolean)
}
