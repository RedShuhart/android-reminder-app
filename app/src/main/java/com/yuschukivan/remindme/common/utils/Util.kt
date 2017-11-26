package com.yuschukivan.remindme.common.utils

import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import com.yuschukivan.remindme.activities.MainActivity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.support.v4.app.TaskStackBuilder
import com.yuschukivan.remindme.R
import android.media.RingtoneManager
import android.graphics.drawable.BitmapDrawable
import android.support.v4.app.NotificationCompat
import android.app.AlarmManager
import android.R.attr.delay
import android.app.Notification
import android.graphics.Color
import android.os.SystemClock
import android.util.Log
import com.yuschukivan.remindme.services.NotificationReceiver
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Ivan on 5/16/2017.
 */
class Util {

    object NotificationUtil {

        fun generateNotification(context: Context,name: String, description: String, delay: Long, notificationId: Int): Int {
            val calendar = Calendar.getInstance()
            Log.d("NotificationGeneratr", "argid: " + notificationId)
            if(calendar.timeInMillis > delay) {
                return -1
            } else {
                val builder = NotificationCompat.Builder(context)
                        .setContentTitle(name)
                        .setContentText(description)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                        .setLights(Color.parseColor("#FF0000"), 3000, 3000)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setPriority(Notification.PRIORITY_HIGH)

//                val intent = Intent(context, MainActivity::class.java)
//                val activity = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
//                builder.setContentIntent(activity)

                val notification = builder.build()

                val notificationIntent = Intent(context, NotificationReceiver::class.java)
                notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId)
                notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification)
                val pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

                val futureInMillis = delay
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.set(AlarmManager.RTC_WAKEUP, futureInMillis, pendingIntent)
                return notificationId
            }
        }



        fun generateNotificationRepeat(context: Context, name: String, description: String, dayOfWeek: Int, notificationId: Int): Int {

            val builder = NotificationCompat.Builder(context)
                    .setContentTitle(name)
                    .setContentText(description)
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setLights(Color.parseColor("#FF0000"), 3000, 3000)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setPriority(Notification.PRIORITY_HIGH)

            val intent = Intent(context, MainActivity::class.java)
            val activity = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            builder.setContentIntent(activity)

            val notification = builder.build()

            val notificationIntent = Intent(context, NotificationReceiver::class.java)
            notificationIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId)
            notificationIntent.putExtra(NotificationReceiver.NOTIFICATION, notification)
            val pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DATE, (calendar.get(Calendar.DAY_OF_WEEK) - dayOfWeek));
            calendar.set(Calendar.HOUR_OF_DAY, 11);
            calendar.set(Calendar.MINUTE, 45);

            if(Calendar.getInstance().after(calendar)) calendar.add(Calendar.DATE, 7)

            val alarmTime = calendar.timeInMillis
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, AlarmManager.INTERVAL_DAY * 7, pendingIntent)
            return notificationId
        }

    }

    object Days {
        val WEEKDAYS = arrayOf("M", "T", "W", "T", "F", "S", "S")
        val DAY_INTERVAL = 24L*60L*60L*1000L
    }

    object Category {
        val TODO: String = "ToDo"
        val BIRTHDAY: String ="Birthday"
        val IDEA: String = "Idea"
        val HISTORY: String = "HISTORY"
    }

    object Filters {
        val TYPE = arrayOf("OVERDUE", "DONE")
    }

    object Priority {
        val LOW: String = "Low"
        val NORMAL: String ="Normal"
        val HIGH: String = "High"
    }
}