package com.yuschukivan.remindme.features.statistics

import com.arellomobile.mvp.MvpView
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.interfaces.datasets.IDataSet

/**
 * Created by yusch on 26.11.2017.
 */
interface StatisticsView: MvpView {
    fun showData(dataSet: IDataSet<*>)
}