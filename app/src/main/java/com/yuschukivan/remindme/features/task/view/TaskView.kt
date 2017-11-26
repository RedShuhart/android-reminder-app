package com.yuschukivan.remindme.features.task.view

import android.content.Intent
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by yusch on 08.11.2017.
 */
interface TaskView : MvpView {
    @StateStrategyType(SkipStrategy::class)
    fun goToCalendar()
    @StateStrategyType(SkipStrategy::class)
    fun goToAddTask()
    fun updateTabsAdapter(categories: List<Categoty>, filters: MutableList<String>, deleted: Boolean)
    fun showError(s: String)
    @StateStrategyType(SkipStrategy::class)
    fun goToNearBy()
    fun askPermissions()
    @StateStrategyType(SkipStrategy::class)
    fun goToMain()
    fun highLight(id: Int, enable: Boolean)

    fun goToStatistics(intent: Intent)
}
