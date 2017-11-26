package com.yuschukivan.remindme.models

import com.google.android.gms.maps.model.MarkerOptions

/**
 * Created by yusch on 22.11.2017.
 */
data class TaskLocationPair (
        val task: Task,
        val marker: MarkerOptions
)