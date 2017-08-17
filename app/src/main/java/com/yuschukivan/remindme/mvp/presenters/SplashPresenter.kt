package com.yuschukivan.remindme.mvp.presenters

import android.content.Context
import com.arellomobile.mvp.MvpPresenter
import com.yuschukivan.remindme.RemindApp
import com.yuschukivan.remindme.mvp.views.SplashView
import javax.inject.Inject

/**
 * Created by Ivan on 5/9/2017.
 */
class SplashPresenter @Inject constructor(): MvpPresenter<SplashView>(){

    @Inject
    lateinit var context: Context

    init {
        RemindApp.appComponent.inject(this)
    }

    override fun attachView(view: SplashView?) {
        super.attachView(view)
        view?.goToMain()
    }

}