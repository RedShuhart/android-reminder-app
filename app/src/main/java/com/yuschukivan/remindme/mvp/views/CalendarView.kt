package com.yuschukivan.remindme.mvp.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.github.sundeepk.compactcalendarview.domain.Event
import com.yuschukivan.remindme.models.Reminder

/**
 * Created by Ivan on 5/23/2017.
 */
interface CalendarView:MvpView {

    fun updateReminders(reminders:List<Reminder>)
    fun updateMonth(month: String)
    fun  showEvents(events: List<Event>)

    fun showDialog(reminder: Reminder)
    fun  setDateName(format: String)
}
