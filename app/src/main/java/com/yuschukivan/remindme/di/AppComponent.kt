package com.yuschukivan.remindme.di

import com.yuschukivan.remindme.di.modules.ContextModule
import com.yuschukivan.remindme.di.modules.RealmModule
import com.yuschukivan.remindme.mvp.presenters.*
import dagger.Component
import javax.inject.Singleton

/**
 * Created by Ivan on 5/9/2017.
 */
@Singleton
@Component(modules = arrayOf(ContextModule::class, RealmModule::class))
interface AppComponent {

    fun inject(presenter: MainPresenter)
    fun inject(presenter: FragmentRemindersPresenter)
    fun inject(presenter: SplashPresenter)
    fun inject(presenter: AddReminderPresenter)
    fun inject(presenter: CalendarPresenter)
    fun inject(presenter: RemindDialogPresenter)

}