package com.yuschukivan.remindme.mvp.views

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by Ivan on 5/10/2017.
 */
interface AddReminderView: MvpView{

    fun showTimePicker()
    fun showDatePicker()
    fun setTimeText(time: String)
    fun setDateText(date: String)
    fun finishActivity(position: Int)
    fun  updateCategoriesSpinner(data: MutableList<Categoty>)
    fun  addButton(index: String, day: String)
    fun  highLight(id: Int, enable: Boolean)
    fun  setMapImage(bmp: Bitmap)
    fun  showError(s: String)

}