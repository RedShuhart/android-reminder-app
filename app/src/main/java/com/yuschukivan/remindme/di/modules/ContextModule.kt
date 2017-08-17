package com.yuschukivan.remindme.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Ivan on 5/9/2017.
 */
@Module
class ContextModule (private val context: Context) {

    @Provides
    @Singleton
    fun provideContext() = context

}