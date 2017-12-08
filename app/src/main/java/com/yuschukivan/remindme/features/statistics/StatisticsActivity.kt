package com.yuschukivan.remindme.features.statistics

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.text.SpannableString
import com.arellomobile.mvp.presenter.InjectPresenter
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.activities.BaseActivity
import com.yuschukivan.remindme.adapters.StatisticsTabsAdapter
import com.yuschukivan.remindme.adapters.TaskTabAdapter
import com.yuschukivan.remindme.common.utils.find

/**
 * Created by yusch on 26.11.2017.
 */
class StatisticsActivity: BaseActivity() {


    val toolbar: Toolbar by lazy {
        find<Toolbar>(R.id.toolbar).apply {
            setTitle("Statistics")
        }
    }

    val viewPager by lazy { find<ViewPager>(R.id.view_pager) }
    val tabLayout by lazy { find<TabLayout>(R.id.tab_layout) }

    lateinit var tabsAdapter: StatisticsTabsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics_activity)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        tabsAdapter = StatisticsTabsAdapter(supportFragmentManager)
        viewPager.adapter = tabsAdapter
        tabLayout.setupWithViewPager(viewPager)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}