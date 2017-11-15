package com.yuschukivan.remindme.adapters

import android.content.Context
import android.support.v4.view.ViewPager
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.common.utils.find
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.SubTask
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.models.TaskShownPair
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by yusch on 05.11.2017.
 */
class TaskItemAdapter(val data: MutableList<TaskShownPair>):
        RecyclerView.Adapter<TaskItemAdapter.Companion.ViewHolder>(), MutableList<TaskShownPair> by data {

    constructor(): this(mutableListOf())

    val viewsSubtasks = PublishSubject.create<Long>()
    lateinit var  completesSubtask: Observable<Pair<Long, Long>>


    companion object {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val cardView = itemView.findViewById(R.id.card_view) as CardView
            val titleTextView = itemView.findViewById(R.id.title_text) as TextView
            val descriptionTextView = itemView.findViewById(R.id.description_text) as TextView
            val dateView = itemView.findViewById(R.id.date_text) as TextView
            val priorityView = itemView.findViewById(R.id.priority_text) as TextView
            val mapView = itemView.findViewById(R.id.map_view) as ImageView
            val separator = itemView.findViewById(R.id.separator) as CardView
            val showSubtasks = itemView.findViewById(R.id.show_subtasks) as TextView
            val subtaskAdapter = SubtaskItemAdapter()
            val subtaskView = itemView.find<RecyclerView>(R.id.subtasks_view).apply {
                adapter = subtaskAdapter
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val taskPair = data[position]

        holder.titleTextView.text = taskPair.task.name
        holder.descriptionTextView.text = taskPair.task.description
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
        holder.dateView.text = dateFormat.format(taskPair.task.dueDate)

        holder.subtaskView.visibility = when(taskPair.shown) {
            true -> View.GONE
            else -> View.VISIBLE
        }

        holder.subtaskView.visibility = when (taskPair.task.subTasks.isEmpty()) {
            true -> View.GONE
            else -> View.VISIBLE
        }
        val adapter = SubtaskItemAdapter()
        adapter.init(taskPair.task.subTasks)
        holder.subtaskView.adapter = adapter

        completesSubtask = adapter.checks.map { it -> Pair(it, taskPair.task.id) }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskItemAdapter.Companion.ViewHolder(view)
    }

    override fun getItemCount() = data.size


}