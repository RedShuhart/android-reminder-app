package com.yuschukivan.remindme.adapters

import android.graphics.Paint
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.models.SubTask
import com.yuschukivan.remindme.models.Task
import io.reactivex.subjects.PublishSubject
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG



/**
 * Created by yusch on 05.11.2017.
 */
class SubtaskItemAdapter(val data: MutableList<SubTask>):
        RecyclerView.Adapter<SubtaskItemAdapter.Companion.ViewHolder>(), MutableList<SubTask> by data {

    val checks = PublishSubject.create<Long>()

    constructor(): this(mutableListOf())

    override fun onBindViewHolder(holder: SubtaskItemAdapter.Companion.ViewHolder, position: Int) {
        val subtask = data[position]

        holder.title.text = subtask.description
        when {
            subtask.completed -> holder.title.paintFlags = holder.title.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG
            else -> holder.title.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
        }

        holder.checkBox.isChecked = subtask.completed
        holder.checkBox.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            when {
                isChecked -> checks.onNext(subtask.id)
                else -> checks.onNext(subtask.id)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskItemAdapter.Companion.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.remind_item, parent, false)
        return SubtaskItemAdapter.Companion.ViewHolder(view)
    }

    override fun getItemCount() = data.size

    companion object {

        class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val checkBox = itemView.findViewById(R.id.checkbox) as CheckBox
            val title = itemView.findViewById(R.id.subtask_name) as TextView
        }
    }

    fun init(subTasks: List<SubTask>) {
        clear()
        addAll(subTasks)
        notifyDataSetChanged()
    }

}