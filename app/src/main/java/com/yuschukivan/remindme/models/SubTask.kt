package com.yuschukivan.remindme.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by yusch on 31.10.2017.
 */
open class SubTask(
    @PrimaryKey
    var id: Long = 0L,
    var description: String = "",
    var completed: Boolean = false
): RealmObject() {}