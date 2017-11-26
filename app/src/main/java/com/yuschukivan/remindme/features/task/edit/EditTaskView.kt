package com.yuschukivan.remindme.features.task.edit

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by yusch on 21.11.2017.
 */
interface EditTaskView: MvpView {
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
    fun setMapImage(bmp: Bitmap)
    fun removeMapImage()
    fun finishWithOk(position: Int)
    fun showAssignReminderDialog(date: String, repeats: String?, wasAssigned: Boolean)
    fun  setTypeSpinner(position: Int)
    fun setPriotirySpinner(position: Int)
    fun showName(name: String)
    fun showDesc(description: String)
}