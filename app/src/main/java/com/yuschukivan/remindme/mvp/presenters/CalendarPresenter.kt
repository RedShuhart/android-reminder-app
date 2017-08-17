package com.yuschukivan.remindme.mvp.presenters

import android.content.Context
import android.graphics.Color
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.github.sundeepk.compactcalendarview.domain.Event
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.common.utils.RealmConfig
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.mvp.views.CalendarView
import io.realm.Realm
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by Ivan on 5/23/2017.
 */
@InjectViewState
class CalendarPresenter @Inject constructor(): MvpPresenter<CalendarView>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    init {
        RemindApp.appComponent.inject(this)
    }



    fun  onDatePick(date: Date) {
        val reminds = realm.where(Reminder::class.java).findAll()
        val cal = Calendar.getInstance()
        cal.time = date
        val reminders = ArrayList<Reminder>()
        for(remind in reminds) {
            var remindDate = Calendar.getInstance()
            remindDate.time = remind.date
            if(remindDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                remindDate.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH) &&
                remindDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                reminders.add(remind)
            }
        }
        viewState.setDateName(SimpleDateFormat("MM/dd/YYYY").format(cal.getTime()))
        viewState.updateReminders(reminders)
    }

    fun  onMonthChange(date: Date) {
        val cal = Calendar.getInstance()
        cal.time = date
        viewState.updateMonth(SimpleDateFormat("MMM - YYYY").format(cal.getTime()))
    }

    fun loadEvents() {
        val reminds = realm.where(Reminder::class.java).findAll()
        val events = ArrayList<Event>()
        for (reminder in reminds) {
            var color = Color.parseColor("#D50000")
            when(reminder.priority) {
                Util.Priority.HIGH -> color = Color.parseColor("#D50000")
                Util.Priority.NORMAL -> color = Color.parseColor("#f57c00")
                Util.Priority.LOW -> color = Color.parseColor("#1B5E20")
            }
            events.add(Event(color, reminder.date!!.time))
        }

        viewState.showEvents(events)
    }

    fun onRemindClick(reminder : Reminder) {
        viewState.showDialog(reminder)
    }

}