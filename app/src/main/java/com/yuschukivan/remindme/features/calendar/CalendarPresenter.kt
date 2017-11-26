package com.yuschukivan.remindme.features.calendar

import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.github.sundeepk.compactcalendarview.domain.Event
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.features.task.edit.EditTaskActivity
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.models.TaskShownPair
import com.yuschukivan.remindme.services.NotificationReceiver
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

    lateinit var lastDatePicked: Date

    val tasks: MutableList<TaskShownPair> = mutableListOf()
    val reminders: MutableList<Reminder> = mutableListOf()

    fun  onDatePick(date: Date) {
        lastDatePicked = date
        val allReminders = realm.where(Reminder::class.java).findAll()
        val allTasks = realm.where(Task::class.java).findAll()

        tasks.clear()
        reminders.clear()

        val cal = Calendar.getInstance()
        cal.time = date

        for(task in allTasks) {
            var taskDate = Calendar.getInstance()
            taskDate.time = task.dueDate
            if(taskDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                    taskDate.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH) &&
                    taskDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                tasks.add(TaskShownPair(task, false))
            }
        }

        for(remind in allReminders) {
            var remindDate = Calendar.getInstance()
            remindDate.time = remind.date
            if(remindDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                remindDate.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH) &&
                remindDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                reminders.add(remind)
            }
        }
        if(reminders.isEmpty() and tasks.isEmpty()) {
            viewState.showPlaceholder(true)
        } else viewState.showPlaceholder(false)

        viewState.setDateName(SimpleDateFormat("MM/dd/YYYY").format(cal.getTime()))
        viewState.updateReminders(reminders)
        viewState.updateTasks(tasks)
    }

    fun  onMonthChange(date: Date) {
        val cal = Calendar.getInstance()
        cal.time = date
        viewState.updateMonth(SimpleDateFormat("MMM - YYYY").format(cal.getTime()))
    }

    fun loadEvents() {
        val allReminders = realm.where(Reminder::class.java).findAll()
        val allTasks = realm.where(Task::class.java).findAll()
        val events = ArrayList<Event>()

        for (task in allTasks) {
            var color = Color.parseColor("#D50000")
            when(task.priority) {
                Util.Priority.HIGH -> color = Color.parseColor("#D50000")
                Util.Priority.NORMAL -> color = Color.parseColor("#f57c00")
                Util.Priority.LOW -> color = Color.parseColor("#1B5E20")
            }
            events.add(Event(color, task.dueDate!!.time))
        }


        for (reminder in allReminders) {
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

    fun onTaskClick(taskShownPair: TaskShownPair) {
        viewState.showActionsDialog(taskShownPair.task)
    }

    fun onEditTask(task: Task) {
        val intent = Intent(context, EditTaskActivity::class.java)
        intent.putExtra("task_id", task.id)
        viewState.startEditing(intent)
    }

    fun onDeleteTask(task: Task) {
        realm.executeTransaction {
            task.reminder?.let {
                val notifications = it.notifications.split(" ")
                for(notification in notifications) {
                    if (notification.isNotEmpty()) {
                        NotificationReceiver.Companion.removeNotification(context, notification)
                    }
                }
                it.deleteFromRealm()
            }
            task.deleteFromRealm()
        }
    }

    fun onDoneTask(task: Task) {
        realm.executeTransaction {
            task.doneDate = Calendar.getInstance().time
            viewState.reloadAdapter()
        }
    }

    fun onUndoTask(task: Task) {
        realm.executeTransaction {
            task.doneDate = null
            viewState.reloadAdapter()
        }
    }

    fun saveLastDatePicked(dateClicked: Date) {
        lastDatePicked = dateClicked
    }

}