package com.yuschukivan.remindme.activities

import com.arellomobile.mvp.MvpAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Ivan on 6/16/2017.
 */
abstract class BaseActivity : MvpAppCompatActivity() {

    private val subscriptions = CompositeDisposable()

    protected fun Disposable.unsubscribeOnDestroy() {
        subscriptions.add(this)
    }

    override fun onPause() {
        super.onPause()

        subscriptions.clear()
    }
}