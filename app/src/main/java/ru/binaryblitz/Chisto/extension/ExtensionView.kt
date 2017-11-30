package ru.binaryblitz.Chisto.extension

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


fun View.visible(show: Boolean, gone: Boolean = true) {
    this.visibility = if (show) {
        View.VISIBLE
    } else if (gone) {
        View.GONE
    } else {
        View.INVISIBLE
    }
}

fun Context.hideKeyboard() {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager?.hideSoftInputFromWindow((this as Activity).window?.currentFocus?.windowToken, 0)
}
