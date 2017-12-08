package com.yuschukivan.remindme.features.calendar

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.yuschukivan.remindme.R
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.yuschukivan.remindme.activities.BaseActivity
import com.yuschukivan.remindme.adapters.RemindCalendarItemAdapter
import com.yuschukivan.remindme.adapters.TaskItemAdapter
import com.yuschukivan.remindme.common.utils.find
import com.yuschukivan.remindme.features.task.list.TasksFragment.Companion.REQUEST_TASK_EDIT
import com.yuschukivan.remindme.features.task.view.TaskActivity
import com.yuschukivan.remindme.fragments.RemindDialogFragment
import com.yuschukivan.remindme.listeners.RecycleItemClickListener
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.models.TaskShownPair
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*


/**
 * Created by Ivan on 5/9/2017.
 */
class CalendarActivity: BaseActivity(), CalendarView {


    @InjectPresenter
    lateinit var presenter: CalendarPresenter


    lateinit var remindAdapter: RemindCalendarItemAdapter
    lateinit var tasksAdapter: TaskItemAdapter
    lateinit var compactCalendarView: CompactCalendarView
    lateinit var actionsDialog: AlertDialog

    val pickedDate by lazy { find<TextView>(R.id.picked_date) }
    val placeholder by lazy { find<TextView>(R.id.placeholder_text) }
    val remindersView by lazy { find<RecyclerView>(R.id.reminde_recycle_view)}
    val tasksView by lazy { find<RecyclerView>(R.id.task_recycle_view)}

    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_layout)
        initToolbar()
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        remindersView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        remindAdapter = RemindCalendarItemAdapter()
        remindersView.adapter = remindAdapter

        tasksView.layoutManager = LinearLayoutManager(this)
        tasksAdapter = TaskItemAdapter(this)
        tasksView.adapter = tasksAdapter

        compactCalendarView = findViewById(R.id.compactcalendar_view) as CompactCalendarView
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY)

        presenter.onDatePick(Calendar.getInstance().time)
        presenter.loadEvents()
        presenter.onMonthChange(compactCalendarView.firstDayOfCurrentMonth)

        compactCalendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                presenter.saveLastDatePicked(dateClicked)
                presenter.onDatePick(dateClicked)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                presenter.onMonthChange(firstDayOfNewMonth)
            }
        })

        remindersView.addOnItemTouchListener(
                RecycleItemClickListener(applicationContext, remindersView, object: RecycleItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                    }

                    override fun onLongItemClick(view: View, position: Int) {
                        presenter.onRemindClick(remindAdapter[position])
                    }
                }))

    }

    override fun onResume() {
        super.onResume()

        tasksAdapter.showsActions
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    query -> presenter.onTaskClick(tasksAdapter[query.second])
                }.unsubscribeOnDestroy()

        tasksAdapter.viewsSubtasks
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    query -> presenter.onShowSubtasks(query)
                }.unsubscribeOnDestroy()

        tasksAdapter.completesSubtask
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    query -> presenter.onCompletesSubtask(query)
                }.unsubscribeOnDestroy()
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.calendar_toolbar) as Toolbar
        toolbar!!.setTitle(R.string.callendat_activity)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun updateTasks(tasks: MutableList<TaskShownPair>) {
        tasksAdapter.clear()
        tasksAdapter.addAll(tasks)
        tasksAdapter.notifyDataSetChanged()
        tasksView.visibility = View.VISIBLE
        if(tasks.isEmpty()) tasksView.visibility = View.GONE

    }

    override fun updateItem(position: Int) {
        tasksAdapter.notifyItemChanged(position)
    }

    override fun updateReminders(reminders: List<Reminder>) {
        remindAdapter.clear()
        remindAdapter.addAll(reminders)
        remindAdapter.notifyDataSetChanged()
        remindersView.visibility = View.VISIBLE
        if(reminders.isEmpty()) remindersView.visibility = View.GONE

    }

    override fun reloadAdapter() {
        tasksAdapter.notifyDataSetChanged()
        tasksView.adapter = tasksAdapter
        tasksView.invalidate()
    }


        override fun updateMonth(month: String) {
        toolbar?.title =  month
    }

    override fun showEvents(events: List<Event>) {
        compactCalendarView.removeAllEvents()
        compactCalendarView.addEvents(events)
    }

    override fun showDialog(reminder: Reminder) {
        val dialog = RemindDialogFragment.newInstance(reminder)
        dialog.deletes.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    presenter.loadEvents()
                    presenter.onDatePick(presenter.lastDatePicked)
                }.unsubscribeOnDestroy()
        dialog.show(fragmentManager,"Tag")
    }

    override fun setDateName(format: String) {
        pickedDate.text = format
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == REQUEST_TASK_EDIT) {
                if(resultCode == Activity.RESULT_OK) {
                    presenter.loadEvents()
                    presenter.onDatePick(presenter.lastDatePicked)
                }
                onResume()
            }
        }
    }

    override fun showPlaceholder(show: Boolean) {
        when(show) {
            true -> placeholder.visibility = View.VISIBLE
            else -> placeholder.visibility = View.GONE
        }
    }

    override fun showActionsDialog(task: Task) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(task.name)
        val view = layoutInflater.inflate(R.layout.task_actions_dialog, null)

        val layout = view.findViewById(R.id.actions_layout) as LinearLayout

        task.latLong?.let { latLong ->
            val navigationButton = view.findViewById(R.id.routes_button).apply {
                visibility = View.VISIBLE
                setOnClickListener{
                    val coords = latLong.split(",")
                    val uri = "http://maps.google.com/maps?daddr=${coords[0]},${coords[1]} ${task.name}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    intent.`package` = "com.google.android.apps.maps"
                    startActivity(intent)
                }
            }
        }

        val editButton = view.findViewById(R.id.edit_button) as Button
        editButton.setOnClickListener {
            presenter.onEditTask(task)
            actionsDialog.dismiss()
        }
        val deleteButton = view.findViewById(R.id.delete_button) as Button
        deleteButton.setOnClickListener {
            presenter.onDeleteTask(task)
            presenter.onDatePick(presenter.lastDatePicked)
            actionsDialog.dismiss()
        }
        val doneButton = view.findViewById(R.id.done_button) as Button

        if ( task.doneDate == null) {
            doneButton.setOnClickListener {
                presenter.onDoneTask(task)
                presenter.onDatePick(presenter.lastDatePicked)
                actionsDialog.dismiss()
            }
        } else {
            doneButton.text = "UNDO"
            doneButton.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.calendar_remove), null)
            doneButton.setOnClickListener {
                presenter.onUndoTask(task)
                presenter.onDatePick(presenter.lastDatePicked)
                actionsDialog.dismiss()
            }
        }

        builder.setView(view)
        actionsDialog = builder.create()
        actionsDialog.show()
    }

    override fun startEditing(intent: Intent) {
        startActivityForResult(intent, REQUEST_TASK_EDIT)
    }


}