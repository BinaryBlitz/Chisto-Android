package ru.binaryblitz.Chisto.Base

import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity

import ru.binaryblitz.Chisto.R

open class BaseActivity : AppCompatActivity() {

    protected fun onInternetConnectionError() {
        Snackbar.make(findViewById(R.id.main), R.string.lost_connection, Snackbar.LENGTH_SHORT).show()
    }

    protected fun onLocationError() {
        Snackbar.make(findViewById(R.id.main), R.string.location_error, Snackbar.LENGTH_SHORT).show()
    }
}
