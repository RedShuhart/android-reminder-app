package com.yuschukivan.remindme.adapters

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.yuschukivan.remindme.features.task.list.TasksFragment
import com.yuschukivan.remindme.fragments.FragmentReminders
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by yusch on 15.11.2017.
 */
class TaskTabAdapter(val fm: FragmentManager, val tabs: MutableList<Categoty>, val filters: MutableList<String>): FragmentStatePagerAdapter(fm), MutableList<Categoty> by tabs {

    constructor(fm: FragmentManager): this(fm, mutableListOf(), mutableListOf())
    constructor(fm: FragmentManager, filters: MutableList<String>): this(fm, mutableListOf(), filters)


    override fun getCount() = tabs.size + 1

    override fun getPageTitle(position: Int) =  when(position) {
        0 -> "All"
        else -> tabs[position - 1].title
    }

    override fun getItem(position: Int) = when(position) {
        0 -> TasksFragment.newInstance(position, filters)
        else -> TasksFragment.newInstance(tabs[position - 1].title, tabs[position - 1].id, position, filters)
    }
}