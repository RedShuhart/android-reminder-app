package com.yuschukivan.remindme.features.statistics

import com.arellomobile.mvp.MvpView
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.PieDataSet

/**
 * Created by yusch on 26.11.2017.
 */
interface StatisticsView: MvpView {
    fun showEstimated(pieSet: PieDataSet)
    fun showSpent(pieSet: PieDataSet)
    fun showTotal(pieSet: PieDataSet)
    fun showTasks(dataSet: BarDataSet)

}