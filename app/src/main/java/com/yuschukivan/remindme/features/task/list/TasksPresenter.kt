package com.yuschukivan.remindme.features.task.list

import android.content.Context
import android.content.Intent
import android.util.Log
import co.metalab.asyncawait.async
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.features.task.edit.EditTaskActivity
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.SubTask
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.models.TaskShownPair
import com.yuschukivan.remindme.services.NotificationReceiver
import io.realm.Realm
import io.realm.RealmResults
import java.util.*
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

    fun loadTasks(category: String, categoryId: Long, filters: List<String>) {
        var  tasksList: RealmResults<Task>
        Log.d("TasksSize", realm.where(Task::class.java).findAll().size.toString())
        when {
            category == "All" && categoryId == 0L -> {tasksList = realm.where(Task::class.java).findAll()}
            else -> {
                tasksList = realm.where(Task::class.java).equalTo("category.title", category).findAll()
            }
        }

        tasks.clear()
        tasks.addAll(tasksList.map { task -> TaskShownPair(task, false) })
        val currentTime = Calendar.getInstance().time.time

        if (!filters.contains("DONE") && !filters.contains("OVERDUE")) {
            tasks.removeAll { it.task.doneDate != null || (it.task.dueDate!!.time < currentTime ) }
        }else if(!filters.contains("DONE") && filters.contains("OVERDUE") ) {
            tasks.removeAll { it.task.doneDate != null || (it.task.dueDate!!.time > currentTime )}
        } else if (!filters.contains("OVERDUE") && filters.contains("DONE")) {
            tasks.removeAll { it.task.dueDate!!.time < currentTime || it.task.doneDate == null }
        } else if (filters.contains("DONE") && filters.contains("OVERDUE")) {
            tasks.removeAll { !(it.task.doneDate != null && it.task.doneDate!!.time > it.task.dueDate!!.time)}
        }

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

    fun loadAllTasks(filters: List<String>) {
        Log.d("TasksSize", realm.where(Task::class.java).findAll().size.toString())
        var  taskList =  realm.where(Task::class.java).findAll()

        tasks.clear()
        tasks.addAll(taskList.map { task -> TaskShownPair(task, false) })

        val currentTime = Calendar.getInstance().time.time

        if (!filters.contains("DONE") && !filters.contains("OVERDUE")) {
            tasks.removeAll { it.task.doneDate != null || (it.task.dueDate!!.time < currentTime ) }
        }else if(!filters.contains("DONE") && filters.contains("OVERDUE") ) {
            tasks.removeAll { it.task.doneDate != null || (it.task.dueDate!!.time > currentTime )}
        } else if (!filters.contains("OVERDUE") && filters.contains("DONE")) {
            tasks.removeAll { it.task.dueDate!!.time < currentTime || it.task.doneDate == null }
        } else if (filters.contains("DONE") && filters.contains("OVERDUE")) {
            tasks.removeAll { !(it.task.doneDate != null && it.task.doneDate!!.time > it.task.dueDate!!.time)}
        }

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
        tasks.clear()
        tasks.addAll(sortedByDate)
        viewState.updateAdapter(sortedByDate)
    }

    fun onShowSubtasks(position: Int) {
            Log.d("Tasks", "Show Subtasks")
            Log.d("Tasks Fragment", "position on show: $position")
            val shown = tasks[position].shown
            tasks[position].shown = !shown
            viewState.updateItem(position)
    }

    fun onCompletesSubtask(query: Pair<Long, Int>) {
        val subtaskId = query.first
        val taskPosition = query.second
        Log.d("Tasks Fragment", "position on complete: $taskPosition")
        var subTask =  realm.where(SubTask::class.java).equalTo("id", subtaskId).findFirst()
        realm.executeTransaction {
            subTask.completed = !subTask.completed
        }
        viewState.updateItem(taskPosition)
    }

    fun onTaskClick(taskShownPair: TaskShownPair) {
        viewState.showActionsDialog(taskShownPair.task)
    }

    fun onEditTask(task: Task) {
        val intent = Intent(context, EditTaskActivity::class.java)
        intent.putExtra("task_id", task.id)
        viewState.startEdit(intent)
    }

    fun onDeleteTask(task: Task) {
        realm.executeTransaction {
            task.reminder?.let {
                val notifications = it.notifications.split(" ")
                for(notification in notifications) {
                    if (notification.isNotEmpty()) {
                        NotificationReceiver.Companion.removeNotification(context, notification)
                    }
                }
                it.deleteFromRealm()
            }
            task.deleteFromRealm()
        }
    }

    fun onDoneTask(task: Task) {
        realm.executeTransaction {
            task.doneDate = Calendar.getInstance().time
            task.subTasks.forEach { it.completed = true }
            viewState.reloadAdapter()
        }
    }

    fun onUndoTask(task: Task) {
        realm.executeTransaction {
            task.doneDate = null
            task.subTasks.forEach { it.completed = false }
            viewState.reloadAdapter()
        }
    }
}
