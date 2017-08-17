package com.yuschukivan.remindme.services

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wdullaer.materialdatetimepicker.Utils
import com.yuschukivan.remindme.common.utils.Util
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Parcelable


/**
 * Created by Ivan on 5/22/2017.
 */
class NotificationReceiver: BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent.getParcelableExtra<Parcelable>(NOTIFICATION) as Notification
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        Log.d("NotificationReceiver", "id: " + notificationId)
        notificationManager.notify(notificationId, notification)
    }


    companion object {
        val NOTIFICATION_ID = "notification_id"
        val NOTIFICATION = "notification"

        fun removeNotification(context: Context, id:String) {
            Log.d("NotificationRemover", "id: " + id)
            val intent = Intent(context, NotificationReceiver::class.java)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val pendingIntent = PendingIntent.getBroadcast(context, id.toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
            pendingIntent.cancel()
            notificationManager.cancel(id.toInt())
        }
    }

}