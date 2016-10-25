package com.chisto.Base

import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import com.chisto.R

open class BaseActivity : AppCompatActivity() {

    protected fun onInternetConnectionError() {
        Snackbar.make(findViewById(R.id.main), R.string.lost_connection_str, Snackbar.LENGTH_SHORT).show()
    }

    protected fun onLocationError() {
        Snackbar.make(findViewById(R.id.main), R.string.location_error, Snackbar.LENGTH_SHORT).show()
    }
}
