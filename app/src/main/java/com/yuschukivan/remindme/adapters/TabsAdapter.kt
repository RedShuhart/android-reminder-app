package com.yuschukivan.remindme.adapters


import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.yuschukivan.remindme.fragments.FragmentReminders
import com.yuschukivan.remindme.models.Categoty

/**
 * Created by Ivan on 5/9/2017.
 */

class TabsAdapter(val fm: FragmentManager, val tabs: MutableList<Categoty>): FragmentStatePagerAdapter(fm), MutableList<Categoty> by tabs {

    constructor(fm: FragmentManager): this(fm, mutableListOf())

    override fun getCount() = tabs.size + 1

    override fun getPageTitle(position: Int) =  when(position) {
        0 -> "All"
        else -> tabs[position - 1].title
    }

    override fun getItem(position: Int) = when(position) {
        0 -> FragmentReminders.newInstance(position)
        else -> FragmentReminders.newInstance(tabs[position - 1].title, tabs[position - 1].id, position)
    }
}