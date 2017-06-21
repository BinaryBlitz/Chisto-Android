package ru.binaryblitz.Chisto.network

import ru.binaryblitz.Chisto.BuildConfig

object ServerConfig {
    val baseUrl = if (BuildConfig.DEBUG) "https://chisto.xyz" else  "https://chis.to"
    val apiURL = baseUrl + "/api/"

    val imageUrl: String
        get() {
            return ""
        }

    val prefsName = "ChistoPrefs"
    val tokenEntity = "auth_token"
    val cityEntity = "city_token"
    val showDialogEntity = "show_dialog"
    val userEntity = "auth_info"
}
