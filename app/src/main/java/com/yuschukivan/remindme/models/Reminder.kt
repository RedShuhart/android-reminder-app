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
        var description: String,
        var priority: String,
        var latLong: String,
        var selectedDays: String,
        var notifications: String,
        var mapImage: ByteArray?,
        var date: Date,
        var type: String,
        var category: Categoty?,
        var address: String
): RealmObject(){
        constructor(): this(id = 0L, title = "", description = "", priority = "", latLong = "",
                selectedDays = "", notifications = "", mapImage = null, date = Calendar.getInstance().time, type = "", category = null, address = "")
}