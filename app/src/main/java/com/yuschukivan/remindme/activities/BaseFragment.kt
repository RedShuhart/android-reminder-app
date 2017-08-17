package com.yuschukivan.remindme.activities

import com.arellomobile.mvp.MvpAppCompatFragment
import com.arellomobile.mvp.MvpFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Ivan on 6/16/2017.
 */
abstract class BaseFragment : MvpAppCompatFragment() {

    private val subscriptions = CompositeDisposable()

    protected fun Disposable.unsubscribeOnDestroy() {
        subscriptions.add(this)
    }

    override fun onPause() {
        super.onPause()

        subscriptions.clear()
    }
}