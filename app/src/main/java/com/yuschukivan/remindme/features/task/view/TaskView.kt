package com.yuschukivan.remindme.features.task.view

import com.arellomobile.mvp.MvpView
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by yusch on 08.11.2017.
 */
interface TaskView : MvpView {
    fun goToCalendar()
    fun goToAddTask()
    fun updateTabsAdapter(categories: List<Categoty>)
    fun showError(s: String)
    fun goToNearBy()
    fun askPermissions()
    fun goToMain()

}
