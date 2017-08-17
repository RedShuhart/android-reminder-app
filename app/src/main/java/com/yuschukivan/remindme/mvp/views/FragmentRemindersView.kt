package com.yuschukivan.remindme.mvp.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.yuschukivan.remindme.models.Reminder

/**
 * Created by Ivan on 5/9/2017.
 */
@StateStrategyType(SkipStrategy::class)
interface FragmentRemindersView : MvpView{

    fun updateAdapter(reminds: List<Reminder>)
    fun reloadAdapter()
    @StateStrategyType(SkipStrategy::class)
    fun showDialog(reminder: Reminder)
}