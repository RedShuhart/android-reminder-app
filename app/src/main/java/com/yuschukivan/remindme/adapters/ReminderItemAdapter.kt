package com.yuschukivan.remindme.adapters

import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.ActionProvider
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.makeramen.roundedimageview.RoundedImageView
import com.yuschukivan.remindme.R
import com.yuschukivan.remindme.common.utils.Util
import com.yuschukivan.remindme.models.Reminder
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import java.text.SimpleDateFormat

/**
 * Created by Ivan on 5/9/2017.
 */
class ReminderItemAdapter(val data: MutableList<Reminder>):
        RecyclerView.Adapter<ReminderItemAdapter.Companion.ViewHolder>(), MutableList<Reminder> by data {

    constructor(): this(mutableListOf())


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleTextView.text = data[position].title
        val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")
        holder.dateView.text = dateFormat.format(data[position].date)
        holder.priorityView.text = data[position].priority
        when(data[position].priority) {
            Util.Priority.HIGH -> holder.priorityView.setTextColor(Color.parseColor("#D50000"))
            Util.Priority.NORMAL -> holder.priorityView.setTextColor(Color.parseColor("#f57c00"))
            Util.Priority.LOW -> holder.priorityView.setTextColor(Color.parseColor("#1B5E20"))
        }
        if(data[position].date.time  < System.currentTimeMillis()) holder.cardView.setCardBackgroundColor(Color.parseColor("#E0E0E0"))
        else holder.cardView.setCardBackgroundColor(Color.WHITE)
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.remind_item, parent, false)
        return ViewHolder(view)
    }

    companion object {
        class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

                val cardView = itemView.findViewById(R.id.card_view) as CardView
                val titleTextView = itemView.findViewById(R.id.title_text) as TextView
                val dateView = itemView.findViewById(R.id.date_text) as TextView
                val priorityView = itemView.findViewById(R.id.priority_view) as TextView
        }
    }
}