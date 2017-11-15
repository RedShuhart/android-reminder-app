package com.yuschukivan.remindme.features.task.assignreminder

import com.arellomobile.mvp.MvpView

/**
 * Created by yusch on 15.11.2017.
 */
interface AssignReminderView: MvpView {

    fun showDatePicker()
    fun highLight(id: Int, enable: Boolean)
    fun addButton(index: String, day: String)
    fun finishWithAssign(date: String, repeats: String)
    fun setDateText(date: String)
    fun showTimePicker()
}