package com.yuschukivan.remindme.features.task.assignreminder

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.yuschukivan.remindme.common.utils.Util
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by yusch on 15.11.2017.
 */

@InjectViewState
class AssignReminderPresenter: MvpPresenter<AssignReminderView>() {
    lateinit var loadedDate: Date
    lateinit var assignedTime: Date
    lateinit var assignedDate: Date
    lateinit var finalDate: Date

    val buttons = mutableMapOf<Int, Boolean>()

    fun onLoad(time: String, repeats: String) {
        viewState.setDateText(time);
        loadedDate = SimpleDateFormat("HH:mm dd/MM/yyyy").parse(time)
        loadButtons(repeats)
    }

    fun loadButtons(repeats: String) {

        Util.Days.WEEKDAYS.forEachIndexed { index, day ->
            if(repeats.contains(index.toString())){
                buttons.put(index, true)
                viewState.addButton(index.toString(), day)
                viewState.highLight(index, true)
            }
            else {
                buttons.put(index, false)
                viewState.addButton(index.toString(), day)
                viewState.highLight(index, false)
            }
        }
    }

    fun onShowChooseDate() {
        viewState.showDatePicker()
    }

    fun onDateChoosen(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.MONTH, monthOfYear)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val date = calendar.time
        assignedDate = date
        viewState.showTimePicker()
    }

    fun onTimeChoosen(hourOfDay: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.clear()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        val date = calendar.time
        assignedTime = date
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        viewState.setDateText(simpleDateFormat.format(date))
    }

    fun onApply() {
        var days = "";
        buttons.forEach { key, value -> if (value) days += key }
        val remindDate = assignedTime.time + assignedDate.time + 3600000L
        val stringDate = SimpleDateFormat("HH:mm dd/MM/yyyy").format(Date(remindDate))
        viewState.finishWithAssign(stringDate, days)
    }

    fun onDayClick(id: Int) {
        val state = buttons[id]
        when(state ?: false) {
            true -> {
                viewState.highLight(id, false)
                buttons.put(id,false)
            }
            false -> {
                viewState.highLight(id, true)
                buttons.put(id,true)
            }
        }
    }
}
