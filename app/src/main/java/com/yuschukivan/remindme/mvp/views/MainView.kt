package com.yuschukivan.remindme.mvp.views

import android.content.Intent
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by Ivan on 5/9/2017.
 */
interface MainView: MvpView{

    fun goToCalendar()
    fun goToAddReminder()
    fun updateTabsAdapter(categories: List<Categoty>)
    fun showError(s: String)
    fun goToNearBy()
    fun askPermissions()
    fun goToTasks()
    fun goToStatistics(intent: Intent)

}