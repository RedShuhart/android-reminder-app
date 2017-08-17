package com.yuschukivan.remindme.mvp.views

import android.content.Intent
import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by Ivan on 5/30/2017.
 */
interface RemindDialogView: MvpView {
    fun close()
    fun showTimePicker()
    fun showDatePicker()
    fun setTimeText(time: String)
    fun setDateText(date: String)
    fun  updateCategoriesSpinner(data: MutableList<Categoty>)
    fun  addButton(index: String, day: String)
    fun  highLight(id: Int, enable: Boolean)
    fun  setMapImage(bmp: Bitmap)
    fun showApply()
    fun  setTypeSpinner(position: Int)
    fun  showError(s: String)
    fun  goToActivity(intent: Intent)
}