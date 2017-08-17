package com.yuschukivan.remindme

import android.app.Application
import com.yuschukivan.remindme.di.AppComponent
import com.yuschukivan.remindme.di.DaggerAppComponent
import com.yuschukivan.remindme.di.modules.ContextModule
import com.yuschukivan.remindme.di.modules.RealmModule

/**
 * Created by Ivan on 5/9/2017.
 */
class RemindApp: Application() {

    companion object {
        lateinit var appComponent: AppComponent
            get
            private set
    }

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .contextModule(ContextModule(this))
                .realmModule(RealmModule(this))
                .build()
    }

}