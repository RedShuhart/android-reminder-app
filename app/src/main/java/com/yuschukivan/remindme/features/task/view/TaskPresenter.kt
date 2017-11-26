package com.yuschukivan.remindme.features.task.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.features.statistics.StatisticsActivity
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.Task
import io.realm.Realm
import javax.inject.Inject

/**
 * Created by yusch on 08.11.2017.
 */
@InjectViewState
class TaskPresenter: MvpPresenter<TaskView>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    var currentTab: Int = 0

    val filters = mutableListOf<String>()

    init {
        RemindApp.appComponent.inject(this)
    }

    fun onCalendar() {
        viewState.goToCalendar()
    }

    fun dispatchLocationIntent() {
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            viewState.askPermissions()
        } else {
            viewState.goToNearBy()
        }
    }

    fun onAddTask() {
        viewState.goToAddTask()
    }

    fun loadCategories() {
        realm.executeTransaction {
            var  category = realm.where(Categoty::class.java).equalTo("id", 0L).findAll()
            if(category.size == 0) {
                val defCategory = realm.createObject(Categoty::class.java, 0L)
                defCategory.title = "Default"
            }
            var categories =  mutableListOf<Categoty>()
            categories.addAll(realm.where(Categoty::class.java).findAll())
            viewState.updateTabsAdapter(categories.sortedBy {
                when(it.title) {
                    "Default" -> 1
                    else -> 2
                }
            }, filters, false)
        }
    }


    fun onAddCategory(name: String) {

        if(realm.where(Categoty::class.java).equalTo("title", name).findAll().size > 0) {
            viewState.showError("Category already exists")
            return
        }

        realm.executeTransaction {
            val newCategory = realm.createObject(Categoty::class.java, System.currentTimeMillis())
            newCategory.title = name
        }
        val categories = mutableListOf<Categoty>()
        categories.addAll(realm.where(Categoty::class.java).findAll())

        viewState.updateTabsAdapter(categories.sortedBy {
            when(it.title) {
                "Default" -> 1
                else -> 2
            }
        }, filters, false)
    }

    fun  onDeleteCategory(categoty: Categoty) {
        realm.executeTransaction {
            val default = realm.where(Categoty::class.java).equalTo("title", "Default").findFirst()
            val  tasks = realm.where(Task::class.java).equalTo("category.title", categoty.title).findAll()
            for(task in tasks) {
                task.category = default
            }
            val  category = realm.where(Categoty::class.java).equalTo("id", categoty.id).findFirst()
            category.deleteFromRealm()

            val categories = mutableListOf<Categoty>()
            categories.addAll(realm.where(Categoty::class.java).findAll())
            viewState.updateTabsAdapter(categories.sortedBy {
                when(it.title) {
                    "Default" -> 1
                    else -> 2
                }
            }, filters, true)
        }
    }

    fun onMainActivity() {
        viewState.goToMain()
    }

    fun saveCurrentTab(position: Int) {
        currentTab = position
    }

    fun  setFilter(id: Int) {
        when(id) {
            R.id.DONE -> {
                if(filters.contains("DONE")) {
                    viewState.highLight(id, false)
                    filters.remove("DONE")
                } else {
                    viewState.highLight(id, true)
                    filters.add("DONE")
                }

            }
            R.id.OVERDUE -> {
                if (filters.contains("OVERDUE")) {
                    viewState.highLight(id, false)
                    filters.remove("OVERDUE")
                } else {
                    viewState.highLight(id, true)
                    filters.add("OVERDUE")
                }
            }
        }
        filterTasks(filters)
    }

    private fun filterTasks(types: List<String>) {
        loadCategories()
    }

    fun onStatistics() {
        val intent = Intent(context, StatisticsActivity::class.java)
        viewState.goToStatistics(intent)
    }

}