package com.yuschukivan.remindme.mvp.presenters

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.common.utils.RealmConfig
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.mvp.views.MainView
import io.realm.Realm
import javax.inject.Inject

/**
 * Created by Ivan on 5/9/2017.
 */
@InjectViewState
class MainPresenter: MvpPresenter<MainView>(){

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    init {
        RemindApp.appComponent.inject(this)
    }

    fun onCalendar() {
        viewState.goToCalendar()
    }

    fun onAddReminder() {
        viewState.goToAddReminder()
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
            })
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
        })
    }

    fun  onDeleteCategory(categoty: Categoty) {
        realm.executeTransaction {
            val default = realm.where(Categoty::class.java).equalTo("title", "Default").findFirst()
            val  reminders = realm.where(Reminder::class.java).equalTo("type", categoty.title).findAll()
            for(reminder in reminders) {
                reminder.category = default
                reminder.type = default.title
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
            })
        }
    }
}