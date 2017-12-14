package ru.binaryblitz.Chisto.extension

import android.app.Activity
import android.content.Context
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import ru.binaryblitz.Chisto.R


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

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = false): View
        = LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)

fun Context.toast(msg: String, toastLength: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, msg, toastLength).show()
}

fun EditText.setCheckEditText(check: Boolean) {
    if (check) {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0)
    } else {
        setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
    }
}
