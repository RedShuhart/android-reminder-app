package com.yuschukivan.remindme.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v4.view.ViewPager
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.makeramen.roundedimageview.RoundedImageView
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.common.utils.find
import com.yuschukivan.remindme.models.Reminder
import com.yuschukivan.remindme.models.SubTask
import com.yuschukivan.remindme.models.Task
import com.yuschukivan.remindme.models.TaskShownPair
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import rx.Subscription
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by yusch on 05.11.2017.
 */
class TaskItemAdapter(val context: Context, val data: MutableList<TaskShownPair>):
        RecyclerView.Adapter<TaskItemAdapter.Companion.ViewHolder>(), MutableList<TaskShownPair> by data {

    constructor(context: Context): this(context, mutableListOf())

    val viewsSubtasks = PublishSubject.create<Int>()
    val completesSubtask =  PublishSubject.create<Pair<Long, Int>>()
    val showsActions = PublishSubject.create<Pair<Long, Int>>()

    companion object {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val taskCard = itemView.findViewById(R.id.task_card) as CardView
            val titleTextView = itemView.findViewById(R.id.task_title) as TextView
            //val descriptionTextView = itemView.findViewById(R.id.task_description) as TextView
            val dateView = itemView.findViewById(R.id.task_date) as TextView
            val priorityView = itemView.findViewById(R.id.task_priority) as TextView
            val mapView = itemView.findViewById(R.id.map_view) as RoundedImageView
            val separator = itemView.findViewById(R.id.separator) as View
            val estimatedView = itemView.findViewById(R.id.task_estimated) as TextView
            val showSubtasks = itemView.findViewById(R.id.show_subtasks) as TextView
            val subtaskView = itemView.find<RecyclerView>(R.id.subtasks_view)
            val taskStatus = itemView.find<ImageView>(R.id.task_status)
        }
    }




    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val taskPair = data[position]

        holder.titleTextView.text = taskPair.task.name
        //holder.descriptionTextView.text = taskPair.task.description
        holder.priorityView.text = taskPair.task.priority
        holder.estimatedView.text = "────"
        taskPair.task.estimatedTime?.let {
            holder.estimatedView.text = "${it / 60} H ${it % 60} Min"
        }
        val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")
        holder.dateView.text = dateFormat.format(taskPair.task.dueDate)
        holder.showSubtasks.setOnClickListener { viewsSubtasks.onNext(position) }

        when(taskPair.task.priority) {
            Util.Priority.HIGH -> holder.priorityView.setTextColor(Color.parseColor("#D50000"))
            Util.Priority.NORMAL -> holder.priorityView.setTextColor(Color.parseColor("#f57c00"))
            Util.Priority.LOW -> holder.priorityView.setTextColor(Color.parseColor("#1B5E20"))
        }

        when {
            (taskPair.task.dueDate!!.time < Calendar.getInstance().time.time &&  taskPair.task.doneDate == null) -> {
                holder.taskStatus.setBackgroundResource(R.drawable.alert_circle_outline)
                holder.taskStatus.setColorFilter(Color.parseColor("#D50000"))
            }
            (taskPair.task.doneDate != null && taskPair.task.doneDate!!.time > taskPair.task.dueDate!!.time) -> {
                holder.taskStatus.setBackgroundResource(R.drawable.calendar_check_yellow)
            }
            taskPair.task.doneDate != null -> {
                holder.taskStatus.setBackgroundResource(R.drawable.calendar_check)
                holder.taskStatus.setColorFilter(Color.parseColor("#1B5E20"))
            }
            else -> {
                holder.taskStatus.setBackgroundResource(R.color.mdtp_white)
                holder.taskStatus.setColorFilter(Color.parseColor("#000000"))
            }
        }

        holder.subtaskView.visibility = when(taskPair.shown && taskPair.task.subTasks.isNotEmpty()) {
            true -> View.VISIBLE
            else -> View.GONE
        }

        when(taskPair.task.subTasks.isNotEmpty()) {
            true -> {
                if (taskPair.shown) {
                    holder.showSubtasks.setCompoundDrawables(null, null, null, context.getDrawable(R.drawable.menu_up))
                    holder.showSubtasks.text = context.getString(R.string.hide_subtasks)
                } else {
                    holder.showSubtasks.setCompoundDrawables(null, null, null, context.getDrawable(R.drawable.menu_down))
                    holder.showSubtasks.text = context.getString(R.string.show_subtasks)
                }
            }
            else -> {
                holder.showSubtasks.setCompoundDrawables(null, null, null, null)
                holder.showSubtasks.text = context.getString(R.string.no_subtasks)
            }
        }

        val subtaskAdapter = SubtaskItemAdapter({
            subtaskId, subtaskPosition -> completesSubtask.onNext(Pair(subtaskId, position))
        })
        holder.subtaskView.layoutManager = LinearLayoutManager(context)
        subtaskAdapter.init(taskPair.task.subTasks)
        holder.subtaskView.adapter = subtaskAdapter

        if(taskPair.task.mapImage != null ) {
            holder.mapView.visibility = View.VISIBLE
            holder.separator.visibility = View.VISIBLE
            holder.mapView.setImageBitmap(BitmapFactory.decodeByteArray(taskPair.task.mapImage, 0, taskPair.task.mapImage!!.size))
        } else {
            holder.mapView.visibility = View.GONE
            holder.separator.visibility = View.GONE
        }

        listOf<View>(
                holder.titleTextView,
                holder.taskCard,
                //holder.descriptionTextView,
                holder.taskStatus,
                holder.estimatedView,
                holder.dateView,
                holder.priorityView
        ).forEach { view ->
                    view.setOnLongClickListener {
                        showsActions.onNext(Pair(taskPair.task.id, position))
                        true
                    } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
        return TaskItemAdapter.Companion.ViewHolder(view)
    }

    override fun getItemCount() = data.size


}