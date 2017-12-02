package ru.binaryblitz.Chisto.extension

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText


fun View.visible(show: Boolean, gone: Boolean = true) {
    this.visibility = if (show) {
        View.VISIBLE
    } else if (gone) {
        View.GONE
    } else {
        View.INVISIBLE
    }
}

fun EditText.clear() {
    setText("")
}

fun Activity.hideKeyboard() {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
    inputMethodManager?.hideSoftInputFromWindow(window?.currentFocus?.windowToken, 0)
}