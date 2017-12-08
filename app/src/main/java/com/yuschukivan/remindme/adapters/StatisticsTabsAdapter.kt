package com.yuschukivan.remindme.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.yuschukivan.remindme.features.statistics.StatsEstimatedFragment
import com.yuschukivan.remindme.features.statistics.StatsSpentFragment
import com.yuschukivan.remindme.features.statistics.StatsTasksFragment
import com.yuschukivan.remindme.features.statistics.StatsTotalFragment

/**
 * Created by yusch on 27.11.2v017.
 */
class StatisticsTabsAdapter(val fm: FragmentManager): FragmentStatePagerAdapter(fm) {

    val tabs = listOf("Total", "Estimated", "Spent", "Tasks")

    override fun getCount() = tabs.size

    override fun getPageTitle(position: Int) =  tabs[position]

    override fun getItem(position: Int): Fragment? = when(position) {
        0 -> StatsTotalFragment.newInstance()
        1 -> StatsEstimatedFragment.newInstance()
        2 -> StatsSpentFragment.newInstance()
        3 -> StatsTasksFragment.newInstance()
        else -> StatsTasksFragment.newInstance()
    }

}