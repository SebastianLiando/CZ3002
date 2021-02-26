package com.guavas.cz3002.extension.android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.res.use

fun Context.hideKeyboard(v: View) {
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

    inputManager?.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

/**
 * Retrieve a color from the current [android.content.res.Resources.Theme].
 */
@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}