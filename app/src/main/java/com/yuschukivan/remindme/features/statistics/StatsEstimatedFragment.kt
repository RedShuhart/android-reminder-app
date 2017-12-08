package com.yuschukivan.remindme.features.statistics

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.activities.BaseFragment
import com.yuschukivan.remindme.adapters.TaskItemAdapter
import com.yuschukivan.remindme.common.utils.find

/**
 * Created by yusch on 27.11.2017.
 */
class StatsEstimatedFragment: BaseFragment(), StatisticsView {

    @InjectPresenter
    lateinit var presenter: StatisticsPresenter

    lateinit var fragmentView: View

    lateinit var estimatedChart: PieChart

    companion object {
        fun newInstance(): StatsEstimatedFragment {
            val fragment = StatsEstimatedFragment()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.stats_estimated_fragment, container, false)
        estimatedChart = fragmentView.findViewById(R.id.estimated_chart) as PieChart
        presenter.loadStats()
        return fragmentView
    }

    override fun onResume() {
        presenter.fillEstimated()
        super.onResume()
    }

    override fun showData(dataSet: IDataSet<*>) {
        val data = PieData(dataSet as IPieDataSet)
        estimatedChart.data = data;
        estimatedChart.description.isEnabled = false
        estimatedChart.legend.isEnabled = false
        estimatedChart.setCenterTextSize(15f)
        estimatedChart.setEntryLabelColor(Color.parseColor("#FFFFFF"))
        estimatedChart.centerText = SpannableString("Estimated Time")

        estimatedChart.animateXY(2000, 2000)
    }

}