package com.yuschukivan.remindme.features.task.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.activities.BaseFragment
import com.yuschukivan.remindme.activities.MainActivity
import com.yuschukivan.remindme.adapters.ReminderItemAdapter
import com.yuschukivan.remindme.adapters.TaskItemAdapter
import com.yuschukivan.remindme.fragments.FragmentReminders
import com.yuschukivan.remindme.fragments.RemindDialogFragment
import com.yuschukivan.remindme.listeners.RecycleItemClickListener
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.TaskShownPair
import com.yuschukivan.remindme.mvp.presenters.FragmentRemindersPresenter
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * Created by yusch on 08.11.2017.
 */
class TasksFragment: BaseFragment(), TasksView {


    @InjectPresenter(type = PresenterType.LOCAL)
    lateinit var presenter: TasksPresenter

    lateinit var tasksView: RecyclerView
    lateinit var taskAdapter: TaskItemAdapter

    lateinit var fragmentView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.tasks_fragment, container, false)
        tasksView = fragmentView.findViewById(R.id.recycle_view) as RecyclerView
        tasksView.layoutManager = LinearLayoutManager(activity)
        taskAdapter = TaskItemAdapter()
        tasksView.adapter = taskAdapter

//        tasksView.addOnItemTouchListener(
//                RecycleItemClickListener(activity, tasksView, object: RecycleItemClickListener.OnItemClickListener {
//                    override fun onItemClick(view: View, position: Int) {
//                        presenter.onRemindClick(taskAdapter[position])
//                    }
//
//                    override fun onLongItemClick(view: View, position: Int) {}
//                }))

        return fragmentView
    }

    override fun onResume() {
        if(getArguments().getString("category", null) == null) {
            presenter.loadAllTasks()
        } else {
            presenter.loadTasks(getArguments().getString("category",null), getArguments().getLong("category_id",0L))
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
    }

    companion object {
        fun newInstance(category: String, categoryId: Long, position: Int): FragmentReminders {
            val args = Bundle()
            args.putString("category", category)
            args.putLong("category_id", categoryId)
            args.putInt("position", position)
            val fragment = FragmentReminders()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(position: Int): FragmentReminders {
            val args = Bundle()
            args.putInt("position", position)
            val fragment = FragmentReminders()
            fragment.arguments = args
            return fragment
        }
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
    }
}