package com.yuschukivan.remindme.di

import com.yuschukivan.remindme.di.modules.ContextModule
import com.yuschukivan.remindme.di.modules.RealmModule
import com.yuschukivan.remindme.features.calendar.CalendarPresenter
import com.yuschukivan.remindme.features.nearby.NearByPresenter
import com.yuschukivan.remindme.features.statistics.StatisticsPresenter
import com.yuschukivan.remindme.features.task.create.CreateTaskPresenter
import com.yuschukivan.remindme.features.task.edit.EditTaskPresenter
import com.yuschukivan.remindme.features.task.list.TasksPresenter
import com.yuschukivan.remindme.features.task.view.TaskPresenter
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
    fun inject(presenter: NearByPresenter)
    fun inject(presenter: TaskPresenter)
    fun inject(presenter: TasksPresenter)
    fun inject(presenter: CreateTaskPresenter)
    fun inject(presenter: EditTaskPresenter)
    fun inject(presenter: StatisticsPresenter)

}