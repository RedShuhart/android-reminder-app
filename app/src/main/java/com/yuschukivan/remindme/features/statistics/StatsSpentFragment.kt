package com.yuschukivan.remindme.features.statistics

import android.graphics.Color
import android.os.Bundle
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
import com.yuschukivan.remindme.common.utils.find

/**
 * Created by yusch on 27.11.2017.
 */
class StatsSpentFragment: BaseFragment(), StatisticsView {

    @InjectPresenter
    lateinit var presenter: StatisticsPresenter

    lateinit var fragmentView: View

    lateinit var spentChart: PieChart

    companion object {
        fun newInstance(): StatsSpentFragment {
            val fragment = StatsSpentFragment()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.stats_spent_fragment, container, false)
        spentChart = fragmentView.findViewById(R.id.spent_chart) as PieChart
        presenter.loadStats()
        return fragmentView
    }

    override fun onResume() {
        presenter.fillSpent()
        super.onResume()
    }

    override fun showData(dataSet: IDataSet<*>) {
        val data = PieData(dataSet as IPieDataSet)
        spentChart.data = data;
        spentChart.setEntryLabelColor(Color.parseColor("#FFFFFF"))
        spentChart.centerText = SpannableString("Spent Time")
        spentChart.description.isEnabled = false
        spentChart.legend.isEnabled = false
        spentChart.setCenterTextSize(15f)
        spentChart.animateXY(2000, 2000)

    }

}