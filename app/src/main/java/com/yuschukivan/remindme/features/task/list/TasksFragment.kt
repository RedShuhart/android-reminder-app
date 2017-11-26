package com.yuschukivan.remindme.features.task.list

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.activities.BaseFragment
import com.yuschukivan.remindme.activities.MainActivity
import com.yuschukivan.remindme.adapters.ReminderItemAdapter
import com.yuschukivan.remindme.adapters.TaskItemAdapter
import com.yuschukivan.remindme.features.task.edit.EditTaskActivity
import com.yuschukivan.remindme.features.task.view.TaskActivity
import com.yuschukivan.remindme.fragments.FragmentReminders
import com.yuschukivan.remindme.fragments.RemindDialogFragment
import com.yuschukivan.remindme.listeners.RecycleItemClickListener
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.models.TaskShownPair
import com.yuschukivan.remindme.mvp.presenters.FragmentRemindersPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import org.w3c.dom.Text

/**
 * Created by yusch on 08.11.2017.
 */
class TasksFragment: BaseFragment(), TasksView {
    @InjectPresenter(type = PresenterType.LOCAL)
    lateinit var presenter: TasksPresenter

    lateinit var tasksView: RecyclerView
    lateinit var taskAdapter: TaskItemAdapter

    lateinit var fragmentView: View

    lateinit var actionsDialog: AlertDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.tasks_fragment, container, false)
        tasksView = fragmentView.findViewById(R.id.recycle_view) as RecyclerView
        tasksView.layoutManager = LinearLayoutManager(activity)
        taskAdapter = TaskItemAdapter(activity)
        tasksView.adapter = taskAdapter

        return fragmentView
    }

    override fun onResume() {
        val filters = mutableListOf<String>()
        arguments.getString("DONE", null)?.let {
            filters.add(it)
        }
        arguments.getString("OVERDUE", null)?.let {
            filters.add(it)
        }
        if(arguments.getString("category", null) == null) {
            presenter.loadAllTasks(filters)
        } else {
            presenter.loadTasks(arguments.getString("category",null), arguments.getLong("category_id",0L), filters)
        }
        super.onResume()

        taskAdapter.viewsSubtasks
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    query -> presenter.onShowSubtasks(query)
                }.unsubscribeOnDestroy()

        taskAdapter.completesSubtask
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    query -> presenter.onCompletesSubtask(query)
                }.unsubscribeOnDestroy()

        taskAdapter.showsActions
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    query -> presenter.onTaskClick(taskAdapter[query.second])
                }.unsubscribeOnDestroy()
    }

    companion object {
        fun newInstance(category: String, categoryId: Long, position: Int, filters: MutableList<String>): TasksFragment {
            val args = Bundle()
            args.putString("category", category)
            args.putLong("category_id", categoryId)
            args.putInt("position", position)
            args.putString("DONE", filters.find { it == "DONE" })
            args.putString("OVERDUE", filters.find { it == "OVERDUE" })
            val fragment = TasksFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(position: Int, filters: MutableList<String>): TasksFragment {
            val args = Bundle()
            args.putInt("position", position)
            args.putString("DONE", filters.find { it == "DONE" })
            args.putString("OVERDUE", filters.find { it == "OVERDUE" })
            val fragment = TasksFragment()
            fragment.arguments = args
            return fragment
        }

        val REQUEST_TASK_EDIT = 69
    }

    override fun updateItem(position: Int) {
        taskAdapter.notifyItemChanged(position)
    }

    override fun updateAdapter(reminds: List<TaskShownPair>) {
        taskAdapter.clear()
        taskAdapter.addAll(reminds)
        taskAdapter.notifyDataSetChanged()
        tasksView.adapter = taskAdapter
        tasksView.invalidate()
    }

    override fun reloadAdapter() {
        taskAdapter.notifyDataSetChanged()
        tasksView.adapter = taskAdapter
        tasksView.invalidate()
    }

    override fun startEdit(intent: Intent) {
        startActivityForResult(intent, REQUEST_TASK_EDIT)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != RESULT_CANCELED) {
            if (requestCode == REQUEST_TASK_EDIT) {
                if(resultCode == Activity.RESULT_OK) (activity as TaskActivity).initTabLayout(arguments.getInt("position",0))
                onResume()
            }
        }
    }

    override fun showActionsDialog(task: Task) {

        val filters = mutableListOf<String>()
        arguments.getString("DONE", null)?.let {
            filters.add(it)
        }
        arguments.getString("OVERDUE", null)?.let {
            filters.add(it)
        }

        val builder = AlertDialog.Builder(activity)
        val view = activity.layoutInflater.inflate(R.layout.task_actions_dialog, null)
        val taskName = view.findViewById(R.id.task_name) as TextView
        taskName.text = task.name
        val editButton = view.findViewById(R.id.edit_button) as Button
        editButton.setOnClickListener {
            presenter.onEditTask(task)
            actionsDialog.dismiss()
        }
        val deleteButton = view.findViewById(R.id.delete_button) as Button
        deleteButton.setOnClickListener {
            presenter.onDeleteTask(task)
            (activity as TaskActivity).initTabLayout(arguments.getInt("position",0))
            if(arguments.getString("category", null) == null) {
                presenter.loadAllTasks(filters)
            } else {
                presenter.loadTasks(arguments.getString("category",null), arguments.getLong("category_id",0L), filters)
            }
            actionsDialog.dismiss()
        }
        val doneButton = view.findViewById(R.id.done_button) as Button
        if(task.doneDate == null) {
            doneButton.setOnClickListener {
                presenter.onDoneTask(task)
                actionsDialog.dismiss()
            }
        } else {
            doneButton.text = "UNDO"
            doneButton.setCompoundDrawablesWithIntrinsicBounds(null, null, activity.getDrawable(R.drawable.calendar_remove), null)
            doneButton.setOnClickListener {
                presenter.onUndoTask(task)
                actionsDialog.dismiss()
            }
        }
        builder.setView(view)
        actionsDialog = builder.create()
        actionsDialog.show()
    }
}