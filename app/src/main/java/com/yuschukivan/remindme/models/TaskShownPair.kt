package com.yuschukivan.remindme.models

/**
 * Created by yusch on 15.11.2017.
 */
data class TaskShownPair(
        val task: Task,
        var shown: Boolean = false
)