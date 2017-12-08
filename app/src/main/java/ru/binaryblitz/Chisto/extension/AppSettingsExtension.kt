package ru.binaryblitz.Chisto.extension

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View
import ru.binaryblitz.Chisto.BuildConfig
import ru.binaryblitz.Chisto.R


fun Activity.showSettingsRequest(@StringRes messageId: Int) {
    window.decorView.findViewById<View>(android.R.id.content)?.let {
        Snackbar.make(it, messageId, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings, { openSettings() })
                .show()
    }
}

fun Activity.openSettings() {
    startActivity(getSettingsIntent())
}

private fun getSettingsIntent(): Intent = Intent().apply {
    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}
