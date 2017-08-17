package com.yuschukivan.remindme.mvp.presenters

import android.content.Context
import android.os.Handler
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.common.utils.RealmConfig
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.fragments.FragmentReminders
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.mvp.views.FragmentRemindersView
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import javax.inject.Inject

/**
 * Created by Ivan on 5/9/2017.
 */
@InjectViewState
class FragmentRemindersPresenter : MvpPresenter<com.yuschukivan.remindme.mvp.views.FragmentRemindersView>(){

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    init {
        RemindApp.appComponent.inject(this)
    }

    val reminders: MutableList<Reminder> = mutableListOf()

    fun loadReminds(category: String, categoryId: Long) {
        var  reminder: RealmResults<Reminder>
        Log.d("RemindsSize", realm.where(Reminder::class.java).findAll().size.toString())
        when {
            category == "All" && categoryId == 0L -> {reminder = realm.where(Reminder::class.java).findAll()}
            else -> {
                reminder = realm.where(Reminder::class.java).equalTo("type", category).findAll()
            }
        }

        reminders.clear()
        reminders.addAll(reminder)
        val sortedByPriority = reminders.sortedBy {
            when(it.priority) {
                Util.Priority.HIGH -> 1
                Util.Priority.NORMAL -> 2
                Util.Priority.LOW -> 3
                else -> 3
            } }

        val sortedByDate = sortedByPriority.sortedBy {
            when {

                it.date?.time ?: Long.MAX_VALUE > System.currentTimeMillis() -> 1
                else -> 2
            }
        }
        viewState.updateAdapter(sortedByDate)
    }

    fun onRemindClick(reminder : Reminder) {
        viewState.showDialog(reminder)
    }

    fun loadAllReminders() {
        Log.d("RemindsSize", realm.where(Reminder::class.java).findAll().size.toString())
        var  reminder =  realm.where(Reminder::class.java).findAll()
        reminders.clear()
        reminders.addAll(reminder)
        val sortedByPriority = reminders.sortedBy {
            when(it.priority) {
                Util.Priority.HIGH -> 1
                Util.Priority.NORMAL -> 2
                Util.Priority.LOW -> 3
                else -> 3
            } }

        val sortedByDate = sortedByPriority.sortedBy {
            when {

                it.date?.time ?: Long.MAX_VALUE > System.currentTimeMillis() -> 1
                else -> 2
            }
        }
        viewState.updateAdapter(sortedByDate)
    }
}