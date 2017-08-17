package com.yuschukivan.remindme.behaviors

import android.content.Context
import android.support.v4.view.ViewParentCompat.onNestedScroll
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.CoordinatorLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View


/**
 * Created by Ivan on 5/16/2017.
 */
class ScrollAwareFABBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior() {


    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout?,
                                     child: FloatingActionButton?, directTargetChild: View?, target: View?, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target,
                nestedScrollAxes)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout?, child: FloatingActionButton?,
                                target: View?, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed)

        if (dyConsumed > 0 && child!!.visibility == View.VISIBLE) {
            child.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                override fun onHidden(fab: FloatingActionButton?) {
                    super.onShown(fab)
                    fab!!.visibility = View.INVISIBLE
                }
            })
        } else if (dyConsumed < 0 && child!!.visibility != View.VISIBLE) {
            child.show()
        }
    }

}