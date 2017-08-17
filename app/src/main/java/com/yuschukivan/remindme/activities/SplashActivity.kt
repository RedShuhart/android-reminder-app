package com.yuschukivan.remindme.activities

import android.content.Intent
import android.os.Bundle
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.yuschukivan.remindme.common.utils.RealmConfig
import com.yuschukivan.remindme.mvp.presenters.SplashPresenter
import com.yuschukivan.remindme.mvp.views.SplashView
import io.realm.Realm
import io.realm.RealmConfiguration

/**
 * Created by Ivan on 5/9/2017.
 */class SplashActivity: MvpAppCompatActivity(), SplashView{


    @InjectPresenter
    lateinit var presenter: SplashPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mvpDelegate.onAttach()
    }

    override fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}