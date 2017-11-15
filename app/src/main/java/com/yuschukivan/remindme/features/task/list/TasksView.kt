package com.yuschukivan.remindme.features.task.list

import com.arellomobile.mvp.MvpView
import com.yuschukivan.remindme.models.TaskShownPair

/**
 * Created by yusch on 08.11.2017.
 */
interface TasksView: MvpView{

    fun updateAdapter(reminds: List<TaskShownPair>)
    fun reloadAdapter()
    fun updateItem(position: Int)
}