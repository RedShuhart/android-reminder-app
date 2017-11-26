package com.yuschukivan.remindme.features.statistics

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ViewPortHandler
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.models.Categoty
import com.yuschukivan.remindme.models.Task
import io.realm.Realm
import java.util.*
import javax.inject.Inject

/**
 * Created by yusch on 26.11.2017.
 */
@InjectViewState
class StatisticsPresenter: MvpPresenter<StatisticsView>() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var realm: Realm

    var currentTab: Int = 0


    val chartColors = listOf<String>(
            "#D50000",
            "#AA00FF",
            "#006064",
            "#6200EA",
            "#0091EA",
            "#2962FF",
            "#E65100",
            "#004D40",
            "#C51162",
            "#DD2C00",
            "#1B5E20",
            "#78909C")

    val otherColors = listOf<String>(
            "#6200EA",
            "#FF3D00",
            "#0091EA",
            "#AA00FF")

    init {
        RemindApp.appComponent.inject(this)
    }
    companion object {
        data class StatisticsData(
                val typeName: String,
                var timeSpent: Int = 0,
                var timeEstimated: Int = 0,
                var totalTime: Int = 0,
                var totalTasks: Int = 0,
                var doneTasks: Int = 0,
                var overdueTasks: Int = 0)

        class CategoryFormatter: IValueFormatter {
            override fun getFormattedValue(value: Float, entry: Entry?, dataSetIndex: Int, viewPortHandler: ViewPortHandler?): String =
                    entry!!.data.toString()

        }
    }



    fun loadStats() {

        val categories = realm.where(Categoty::class.java).findAll()
        val tasks = realm.where(Task::class.java).findAll()
        val categoriesStats = mutableListOf<StatisticsData>()
        val currentTime = Calendar.getInstance().time.time

        categories.forEach { category ->
            val stats = StatisticsData(category.title)
            tasks.forEach { task ->
                if (task.category!!.title == category.title) {
                    stats.totalTasks ++
                    if ( task.doneDate != null) stats.doneTasks ++
                        else if(task.dueDate!!.time < currentTime) stats.overdueTasks ++
                    task.estimatedTime?.let { estimated ->
                        stats.totalTime += estimated
                        if(task.doneDate != null) {
                            stats.timeSpent += estimated
                        } else stats.timeEstimated += estimated
                    }
                }
            }
            Log.d("stats", stats.toString())
            categoriesStats.add(stats)
        }

        fillEstimated(categoriesStats)
        fillSpent(categoriesStats)
        fillTotal(categoriesStats)
        fillTasks(categoriesStats)
    }

    fun fillEstimated(stats: List<StatisticsData>) {
        val entries = stats.map { PieEntry(it.timeEstimated.toFloat(), it.typeName) }
        val pieSet = PieDataSet(entries, "Estimated Time")
        pieSet.colors = chartColors.map { Color.parseColor(it) }
        pieSet.valueTextSize = 12f
        pieSet.valueTextColor = (Color.parseColor("#FFFFFF"))
        pieSet.setValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
            "${"%.1f".format(value / 60)} H"
        }
        viewState.showEstimated(pieSet)
    }

    fun fillSpent(stats: List<StatisticsData>) {
        val entries = stats.map { PieEntry(it.timeSpent.toFloat(), it.typeName) }
        val pieSet = PieDataSet(entries, "Spent Time")
        pieSet.colors = chartColors.map { Color.parseColor(it) }
        pieSet.valueTextSize = 12f
        pieSet.valueTextColor = (Color.parseColor("#FFFFFF"))
        pieSet.setValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
            "${"%.1f".format(value / 60)} H"
        }
        viewState.showSpent(pieSet)
    }

    fun fillTotal(stats: List<StatisticsData>) {
        val entries = stats.map { PieEntry(it.totalTime.toFloat(), it.typeName) }
        val pieSet = PieDataSet(entries, "Total Time")
        pieSet.colors = chartColors.map { Color.parseColor(it) }
        pieSet.valueTextSize = 12f
        pieSet.valueTextColor = (Color.parseColor("#FFFFFF"))
        pieSet.setValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
            "${"%.1f".format(value / 60)} H"
        }
        viewState.showTotal(pieSet)
    }

    fun fillTasks(stats: List<StatisticsData>) {
        val tasks = listOf<Pair<Int, String>>(
                Pair(stats.map { it.doneTasks }.sum(), "Done"),
                Pair(stats.map{ it.overdueTasks }.sum(), "Overdue"),
                Pair(stats.map { it.totalTasks }.sum() - stats.map{ it.overdueTasks }.sum() - stats.map { it.doneTasks }.sum(), "Current"),
                Pair(stats.map { it.totalTasks }.sum(), "All")
        ).mapIndexed { index, pair ->  BarEntry(index.toFloat(), pair.first.toFloat(), pair.second)}

        val dataSet = BarDataSet(tasks, "Tasks status")
        dataSet.colors = otherColors.map { Color.parseColor(it) }

        viewState.showTasks(dataSet)
    }
}