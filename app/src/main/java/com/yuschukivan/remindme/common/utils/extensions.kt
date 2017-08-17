package com.yuschukivan.remindme.common.utils

import android.view.View
import com.arellomobile.mvp.MvpAppCompatActivity

/**
 * Created by Ivan on 5/9/2017.
 */
internal fun <T: View> MvpAppCompatActivity.find(id: Int): T = findViewById(id) as T
internal fun <T: View> View.find(id: Int): T = findViewById(id) as T