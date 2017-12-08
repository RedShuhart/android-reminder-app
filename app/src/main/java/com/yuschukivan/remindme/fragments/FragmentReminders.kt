package com.yuschukivan.remindme.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.PresenterType
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.activities.BaseFragment
import com.yuschukivan.remindme.activities.MainActivity
import com.yuschukivan.remindme.adapters.ReminderItemAdapter
import com.yuschukivan.remindme.listeners.RecycleItemClickListener
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.mvp.presenters.FragmentRemindersPresenter
import com.yuschukivan.remindme.mvp.views.FragmentRemindersView
import io.reactivex.android.schedulers.AndroidSchedulers
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Ivan on 5/9/2017.
 */
class FragmentReminders : BaseFragment(), FragmentRemindersView {


    @InjectPresenter(type = PresenterType.LOCAL)
    lateinit var presenter: FragmentRemindersPresenter

    lateinit var recyclerView: RecyclerView
    lateinit var remindAdapter: ReminderItemAdapter

    lateinit var fragmentView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.fragment_all, container, false)
        recyclerView = fragmentView.findViewById(R.id.recycle_view) as RecyclerView
        recyclerView.layoutManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
        remindAdapter = ReminderItemAdapter()
        recyclerView.adapter = remindAdapter

        recyclerView.addOnItemTouchListener(
                RecycleItemClickListener(activity, recyclerView, object: RecycleItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                    }

                    override fun onLongItemClick(view: View, position: Int) {
                        presenter.onRemindClick(remindAdapter[position])
                    }
                }))

        return fragmentView
    }

    override fun onResume() {
        if(getArguments().getString("category", null) == null) {
            presenter.loadAllReminders()
        } else {
            presenter.loadReminds(getArguments().getString("category",null), getArguments().getLong("category_id",0L))
        }
        super.onResume()

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

    override fun updateAdapter(reminds: List<Reminder>) {
        remindAdapter.clear()
        remindAdapter.addAll(reminds)
        remindAdapter.notifyDataSetChanged()
        recyclerView.adapter = remindAdapter
        recyclerView.invalidate()
    }

    override fun reloadAdapter() {
        remindAdapter.notifyDataSetChanged()
        recyclerView.adapter = remindAdapter
    }

    override fun showDialog(reminder: Reminder) {
        val dialog =  RemindDialogFragment.newInstance(reminder)
        dialog.deletes.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    (activity as MainActivity).initTabLayout(arguments.getInt("position",0))
                    onResume()
                }.unsubscribeOnDestroy()
        dialog.show(activity.fragmentManager, "TAG")
    }
}
