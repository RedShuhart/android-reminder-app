package com.yuschukivan.remindme.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by Ivan on 5/9/2017.
 */
open class Reminder(
        @PrimaryKey
        var id: Long,
        var title: String,
        var priority: String,
        var selectedDays: String,
        var notifications: String,
        var date: Date,
        var type: String,
        var category: Categoty?
): RealmObject(){
        constructor(): this(id = 0L, title = "", priority = "",
                selectedDays = "", notifications = "", date = Calendar.getInstance().time, type = "", category = null)
}