package com.yuschukivan.remindme.features.task.list

import android.content.Intent
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.models.TaskShownPair

/**
 * Created by yusch on 08.11.2017.
 */
interface TasksView: MvpView{

    fun updateAdapter(reminds: List<TaskShownPair>)
    fun reloadAdapter()
    fun updateItem(position: Int)
    @StateStrategyType(SkipStrategy::class)
    fun showActionsDialog(task: Task)
    @StateStrategyType(SkipStrategy::class)
    fun startEdit(intent: Intent)
}