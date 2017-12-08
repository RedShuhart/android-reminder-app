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
            "#0091EA",
            "#00C853",
            "#FFAB00",
            "#D50000")

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

    val categoriesStats = mutableListOf<StatisticsData>()

    fun loadStats() {
        categoriesStats.clear()
        val categories = realm.where(Categoty::class.java).findAll()
        val tasks = realm.where(Task::class.java).findAll()
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
    }

    fun fillEstimated() {
        val entries = categoriesStats.filter { it.timeEstimated > 0 }.map { PieEntry(it.timeEstimated.toFloat(), it.typeName) }
        val pieSet = PieDataSet(entries, "Estimated Time")
        pieSet.colors = chartColors.map { Color.parseColor(it) }
        pieSet.valueTextSize = 12f
        pieSet.valueTextColor = (Color.parseColor("#FFFFFF"))
        pieSet.setValueFormatter { value, _, _, _ ->
            if(value > 0) "${"%.1f".format(value / 60)} H" else ""
        }
        viewState.showData(pieSet)
    }

    fun fillSpent() {
        val entries = categoriesStats.filter { it.timeSpent > 0 }.map { PieEntry(it.timeSpent.toFloat(), it.typeName) }
        val pieSet = PieDataSet(entries, "Spent Time")
        pieSet.colors = chartColors.map { Color.parseColor(it) }
        pieSet.valueTextSize = 12f
        pieSet.valueTextColor = (Color.parseColor("#FFFFFF"))
        pieSet.setValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
            if(value > 0) "${"%.1f".format(value / 60)} H" else ""
        }
        viewState.showData(pieSet)
    }

    fun fillTotal() {
        val entries = categoriesStats.filter { it.totalTime > 0 }.map { PieEntry(it.totalTime.toFloat(), it.typeName) }
        val pieSet = PieDataSet(entries, "Total Time")
        pieSet.colors = chartColors.map { Color.parseColor(it) }
        pieSet.valueTextSize = 12f
        pieSet.valueTextColor = (Color.parseColor("#FFFFFF"))
        pieSet.setValueFormatter { value, _, _, _ ->
            if(value > 0) "${"%.1f".format(value / 60)} H" else ""
        }
        viewState.showData(pieSet)
    }

    fun fillTasks2() {

        val tasks = listOf<Pair<Int, String>>(
                Pair(categoriesStats.map { it.doneTasks }.sum(), "Done"),
                Pair(categoriesStats.map{ it.overdueTasks }.sum(), "Overdue"),
                Pair(categoriesStats.map { it.totalTasks }.sum() - categoriesStats.map{ it.overdueTasks }.sum() - categoriesStats.map { it.doneTasks }.sum(), "Current")
                /*Pair(stats.map { it.totalTasks }.sum(), "All")*/
        ).mapIndexed { index, pair ->  BarEntry(index.toFloat(), pair.first.toFloat(), pair.second)}

        val dataSet = BarDataSet(tasks, "Tasks status")
        dataSet.colors = otherColors.map { Color.parseColor(it) }
        viewState.showData(dataSet)
    }

    fun fillTasks() {
        val tasks = realm.where(Task::class.java).findAll()
        var current = 0
        var done = 0
        var overdue = 0
        var overdueDone = 0;

        val currentTime = Calendar.getInstance().time.time

        tasks.forEach { when {
            it.doneDate != null && it.doneDate!!.time <= it.dueDate!!.time ->  done ++
            it.doneDate != null && it.doneDate!!.time > it.dueDate!!.time -> overdueDone ++
            it.doneDate == null && it.dueDate!!.time < currentTime -> overdue++
            else -> current++
        } }

        val stats = listOf(
                "Current" to current,
                "Done" to done,
                "Overdue done" to overdueDone,
                "Overdue" to overdue
        ).mapIndexed { index, pair -> BarEntry(index.toFloat(), pair.second.toFloat(), pair.first)  }
        val dataSet = BarDataSet(stats, "Tasks status")
        dataSet.colors = otherColors.map { Color.parseColor(it) }
        dataSet.valueTextSize = 15f
        Log.d("statistics", "current $current done $done overdue $overdue overdueDone $overdueDone total ${tasks}")

        viewState.showData(dataSet)
    }

}