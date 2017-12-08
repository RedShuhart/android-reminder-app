package com.yuschukivan.remindme.features.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.activities.BaseFragment
import com.yuschukivan.remindme.common.utils.find

/**
 * Created by yusch on 27.11.2017.
 */
class StatsTasksFragment: BaseFragment(), StatisticsView {

    @InjectPresenter
    lateinit var presenter: StatisticsPresenter

    lateinit var fragmentView: View

    lateinit var tasksChart: HorizontalBarChart

    companion object {
        fun newInstance(): StatsTasksFragment {
            val fragment = StatsTasksFragment()
            return fragment
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.stats_tasks_fragment, container, false)
        tasksChart = fragmentView.findViewById(R.id.tasks_chart) as HorizontalBarChart
        presenter.loadStats()
        return fragmentView
    }

    override fun onResume() {
        presenter.fillTasks()
        super.onResume()
    }

    override fun showData(dataSet: IDataSet<*>) {
        val data = BarData(dataSet as IBarDataSet)
        data.setValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
            entry.data.toString()
        }
        tasksChart.data = data
        tasksChart.xAxis.setValueFormatter { value, axis -> "" }
        tasksChart.xAxis.granularity = 1f
        tasksChart.xAxis.isGranularityEnabled = true
        tasksChart.axisRight.granularity = 1f
        tasksChart.axisLeft.granularity = 1f
        tasksChart.axisLeft.axisMaximum = data.yMax + 2
        tasksChart.axisLeft.axisMinimum = 0f
        tasksChart.axisRight.axisMaximum = data.yMax + 2
        tasksChart.axisRight.axisMinimum = 0f
//        tasksChart.axisLeft.setValueFormatter { value, axis -> value.toInt().toString()  }

        tasksChart.animateXY(2000,2000)
    }


}