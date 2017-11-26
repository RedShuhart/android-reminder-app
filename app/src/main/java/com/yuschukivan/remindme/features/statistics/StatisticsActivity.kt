package com.yuschukivan.remindme.features.statistics

import android.graphics.Color
import android.os.Bundle
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
import com.yuschukivan.remindme.common.utils.find

/**
 * Created by yusch on 26.11.2017.
 */
class StatisticsActivity: BaseActivity(), StatisticsView {


    @InjectPresenter
    lateinit var presenter: StatisticsPresenter

    val toolbar: Toolbar by lazy {
        find<Toolbar>(R.id.toolbar).apply {
            setTitle("Statistics")
        }
    }

    val estimatedChart by lazy { find<PieChart>(R.id.estimated_chart) }
    val spentChart by lazy { find<PieChart>(R.id.done_chart) }
    val totalChart by lazy { find<PieChart>(R.id.tota_chart_chart) }
    val tasksChart by lazy { find<HorizontalBarChart>(R.id.tasks_chart) }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppDefault)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics_activity)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        presenter.loadStats()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun showEstimated(pieSet: PieDataSet) {
        val data = PieData(pieSet)
        estimatedChart.data = data;
        estimatedChart.setEntryLabelColor(Color.parseColor("#FFFFFF"))
        estimatedChart.centerText = SpannableString("Estimated Time")
        estimatedChart.animateXY(2000, 2000)
    }

    override fun showSpent(pieSet: PieDataSet) {
        val data = PieData(pieSet)
        spentChart.data = data;
        spentChart.setEntryLabelColor(Color.parseColor("#FFFFFF"))
        spentChart.centerText = SpannableString("Spent Time")
        spentChart.animateXY(2000, 2000)

    }

    override fun showTotal(pieSet: PieDataSet) {
        val data = PieData(pieSet)
        totalChart.data = data;
        totalChart.setEntryLabelColor(Color.parseColor("#FFFFFF"))
        totalChart.centerText = SpannableString("Total Time")
        totalChart.animateXY(2000, 2000)
    }

    override fun showTasks(dataSet: BarDataSet) {
        val data = BarData(dataSet)
        data.setValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
            entry.data.toString()
        }
        tasksChart.data = data
        tasksChart.xAxis.valueFormatter = LabelFormatter()
        tasksChart.xAxis.granularity = 1f
        tasksChart.xAxis.isGranularityEnabled = true

        tasksChart.animateXY(2000,2000)
    }

    companion object {
        class LabelFormatter : IAxisValueFormatter {
            val labels = listOf("Current", "Overdue", "Done", "Total")
            override fun getFormattedValue(value: Float, axis: AxisBase?): String =
                    labels[value.toInt()]
        }

    }

}