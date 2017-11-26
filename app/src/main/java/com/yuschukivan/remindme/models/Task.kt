package com.yuschukivan.remindme.models

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by yusch on 31.10.2017.
 */
open class Task(
        @PrimaryKey
        var id: Long = 0L,
        var name: String = "",
        var description: String ="",
        var dueDate: Date? = null,
        var doneDate: Date? = null,
        var priority: String = "",
        var estimatedTime: Int? = null,
        var reminder: Reminder? = null,
        var subTasks: RealmList<SubTask> = RealmList(),
        var mapImage: ByteArray? = null,
        var category: Categoty? = null,
        var latLong: String? = null,
        var address: String? = null
): RealmObject() {}