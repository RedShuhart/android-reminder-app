package com.yuschukivan.remindme.features.calendar

import android.content.Intent
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.github.sundeepk.compactcalendarview.domain.Event
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.models.TaskShownPair

/**
 * Created by Ivan on 5/23/2017.
 */
interface CalendarView: MvpView {

    fun updateReminders(reminders:List<Reminder>)
    fun updateMonth(month: String)
    fun  showEvents(events: List<Event>)

    fun showDialog(reminder: Reminder)
    fun  setDateName(format: String)
    fun updateTasks(tasks: MutableList<TaskShownPair>)
    fun showActionsDialog(task: Task)
    fun showPlaceholder(show: Boolean)
    fun reloadAdapter()
    fun startEditing(intent: Intent)
}
