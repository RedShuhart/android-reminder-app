package com.yuschukivan.remindme.features.task.create

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
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
    fun addSubtaskToView(name: String)
    fun removeSubtaskFromView(name: String)
    fun showCreateSubtaskDialog()
    fun setMapImage(bmp: Bitmap)
    fun removeMapImage()
    fun finishWithOk(position: Int)
    fun showAssignReminderDialog(date: String, repeats: String)
}
