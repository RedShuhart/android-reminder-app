package com.yuschukivan.remindme.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.yuschukivan.remindme.R
import io.reactivex.subjects.PublishSubject

/**
 * Created by yusch on 12.11.2017.
 */
class SubtaskListItemAdapter(val data: MutableList<String>):
        RecyclerView.Adapter<SubtaskListItemAdapter.Companion.ViewHolder>(), MutableList<String> by data {

    constructor(): this(mutableListOf())

    val removes = PublishSubject.create<String>()

    companion object {
        class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val removeButton = itemView.findViewById(R.id.remove_button) as ImageButton
            val title = itemView.findViewById(R.id.subtask_name) as TextView
        }

    }

    override fun onBindViewHolder(holder: SubtaskListItemAdapter.Companion.ViewHolder, position: Int) {
        holder.title.text = data[position]
        holder.removeButton.setOnClickListener { removes.onNext(data[position]) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskListItemAdapter.Companion.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.subtask_list_item, parent, false)
        return SubtaskListItemAdapter.Companion.ViewHolder(view)
    }

    override fun getItemCount() = data.size
}