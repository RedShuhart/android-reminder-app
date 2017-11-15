package com.yuschukivan.remindme.features.task.list

import android.content.Context
import android.util.Log
import co.metalab.asyncawait.async
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.models.TaskShownPair
import io.realm.Realm
import io.realm.RealmResults
import javax.inject.Inject

/**
 * Created by yusch on 08.11.2017.
 */
@InjectViewState
class TasksPresenter: MvpPresenter<TasksView>() {
    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    init {
        RemindApp.appComponent.inject(this)
    }

    val tasks: MutableList<TaskShownPair> = mutableListOf()

    fun loadTasks(category: String, categoryId: Long) {
        var  tasksList: RealmResults<Task>
        Log.d("TasksSize", realm.where(Task::class.java).findAll().size.toString())
        when {
            category == "All" && categoryId == 0L -> {tasksList = realm.where(Task::class.java).findAll()}
            else -> {
                tasksList = realm.where(Task::class.java).equalTo("category.name", category).findAll()
            }
        }

        tasks.clear()
        tasks.addAll(tasksList.map { task -> TaskShownPair(task, false) })
        val sortedByPriority = tasks.sortedBy {
            when(it.task.priority) {
                Util.Priority.HIGH -> 1
                Util.Priority.NORMAL -> 2
                Util.Priority.LOW -> 3
                else -> 3
            } }

        val sortedByDate = sortedByPriority.sortedBy {
            when {

                it.task.dueDate?.time ?: Long.MAX_VALUE > System.currentTimeMillis() -> 1
                else -> 2
            }
        }
        viewState.updateAdapter(sortedByDate)
    }

    fun loadAllTasks() {
        Log.d("TasksSize", realm.where(Task::class.java).findAll().size.toString())
        var  taskList =  realm.where(Task::class.java).findAll()
        tasks.clear()
        tasks.addAll(taskList.map { task -> TaskShownPair(task, false) })
        val sortedByPriority = tasks.sortedBy {
            when(it.task.priority) {
                Util.Priority.HIGH -> 1
                Util.Priority.NORMAL -> 2
                Util.Priority.LOW -> 3
                else -> 3
            } }

        val sortedByDate = sortedByPriority.sortedBy {
            when {

                it.task.dueDate?.time ?: Long.MAX_VALUE > System.currentTimeMillis() -> 1
                else -> 2
            }
        }
        viewState.updateAdapter(sortedByDate)
    }

    fun onShowSubtasks(id: Long) {
            val position = tasks.indexOfFirst{ it.task.id == id}
            val shown = tasks[position].shown
            tasks[position].shown = !shown
            viewState.updateItem(position)
    }

    fun onCompletesSubtask(query: Pair<Long, Long>) {
        val subtaskId = query.first
        val taskId = query.second
    }
}
