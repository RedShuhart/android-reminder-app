package com.yuschukivan.remindme.activities

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.support.transition.Visibility
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.yuschukivan.remindme.R
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.yuschukivan.remindme.adapters.ReminderItemAdapter
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.common.utils.find
import com.yuschukivan.remindme.fragments.RemindDialogFragment
import com.yuschukivan.remindme.listeners.RecycleItemClickListener
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.mvp.presenters.CalendarPresenter
import com.yuschukivan.remindme.mvp.views.CalendarView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Ivan on 5/9/2017.
 */
class CalendarActivity: BaseActivity(), CalendarView{

    @InjectPresenter
    lateinit var presenter: CalendarPresenter

    lateinit var recyclerView: RecyclerView
    lateinit var remindAdapter: ReminderItemAdapter
    lateinit var compactCalendarView: CompactCalendarView

    val pickedDate by lazy { find<TextView>(R.id.picked_date) }

    private var toolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_layout)
        initToolbar()
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        recyclerView = findViewById(R.id.recycle_view) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        remindAdapter = ReminderItemAdapter()
        recyclerView.adapter = remindAdapter

        compactCalendarView = findViewById(R.id.compactcalendar_view) as CompactCalendarView
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY)

        presenter.onDatePick(Calendar.getInstance().time)
        presenter.loadEvents()
        presenter.onMonthChange(compactCalendarView.firstDayOfCurrentMonth)

        compactCalendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                presenter.onDatePick(dateClicked)
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                presenter.onMonthChange(firstDayOfNewMonth)
            }
        })

        recyclerView.addOnItemTouchListener(
                RecycleItemClickListener(applicationContext, recyclerView, object: RecycleItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        presenter.onRemindClick(remindAdapter[position])
                    }

                    override fun onLongItemClick(view: View, position: Int) {}
                }))
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.calendar_toolbar) as Toolbar
        toolbar!!.setTitle(R.string.callendat_activity)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun updateReminders(reminders: List<Reminder>) {
        remindAdapter.clear()
        remindAdapter.addAll(reminders)
        remindAdapter.notifyDataSetChanged()
        recyclerView.visibility = View.VISIBLE
        if(reminders.isEmpty()) recyclerView.visibility = View.GONE

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
                    presenter.onDatePick(Calendar.getInstance().time)
                }.unsubscribeOnDestroy()
        dialog.show(fragmentManager,"Tag")
    }
    override fun setDateName(format: String) {
        pickedDate.text = format
    }


}