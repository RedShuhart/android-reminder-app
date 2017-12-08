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
class StatsTotalFragment: BaseFragment(), StatisticsView {

    @InjectPresenter
    lateinit var presenter: StatisticsPresenter

    lateinit var fragmentView: View

    lateinit var totalChart: PieChart

    companion object {
        fun newInstance(): StatsTotalFragment {
            val fragment = StatsTotalFragment()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.stats_total_fragment, container, false)
        totalChart = fragmentView.findViewById(R.id.total_chart) as PieChart
        presenter.loadStats()
        return fragmentView
    }

    override fun onResume() {
        presenter.fillTotal()
        super.onResume()
    }

    override fun showData(dataSet: IDataSet<*>) {
        val data = PieData(dataSet as IPieDataSet)
        totalChart.data = data;
        totalChart.description.isEnabled = false
        totalChart.legend.isEnabled = false
        totalChart.setCenterTextSize(15f)
        totalChart.setEntryLabelColor(Color.parseColor("#FFFFFF"))
        totalChart.centerText = SpannableString("Total Time")
        totalChart.animateXY(2000, 2000)
    }
}