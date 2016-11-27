package ru.binaryblitz.Chisto.Base

import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import retrofit2.Response

import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Utils.ServerErrorHandler

open class BaseActivity : AppCompatActivity() {

    protected fun onInternetConnectionError() {
        Snackbar.make(findViewById(R.id.main), R.string.lost_connection, Snackbar.LENGTH_SHORT).show()
    }

    protected fun onServerError(response: Response<*>) {
        val error = ServerErrorHandler.parseError(response)
        if (error.status() == 500) Snackbar.make(findViewById(R.id.main), getString(R.string.server_error), Snackbar.LENGTH_SHORT).show()
        else Snackbar.make(findViewById(R.id.main), error.message(), Snackbar.LENGTH_SHORT).show()
    }

    protected fun onLocationError() {
        Snackbar.make(findViewById(R.id.main), R.string.location_error, Snackbar.LENGTH_SHORT).show()
    }
}
