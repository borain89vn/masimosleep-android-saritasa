package com.mymasimo.masimosleep.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator

fun Context.dpFromPx(px: Float): Float = px / resources.displayMetrics.density

fun Context.pxFromDp(dp: Float): Float = dp * resources.displayMetrics.density

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * Check destination to avoid "cannot be found from the current destination" exception
 */
fun NavController.navigateSafe(
        @IdRes resId: Int,
        args: Bundle? = null,
        navOptions: NavOptions? = null,
        navExtras: Navigator.Extras? = null
) {
    val action = currentDestination?.getAction(resId) ?: graph.getAction(resId)
    if (action != null && currentDestination?.id != action.destinationId) {
        navigate(resId, args, navOptions, navExtras)
    }
}

fun calculateXZoomScale(visibleTimeSpan: Long, startTime: Long, endTime: Long): Float {
    val duration = endTime - startTime
    return if (duration <= 0 || visibleTimeSpan <= 0) {
        1F
    } else {
        val scale = duration.toFloat() / visibleTimeSpan.toFloat()
        return if (scale < 1) {
            //No need to zoom out
            1F
        } else {
            scale
        }
    }
}