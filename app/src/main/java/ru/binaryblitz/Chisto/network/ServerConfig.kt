package ru.binaryblitz.Chisto.network

object ServerConfig {
    //TODO https://chisto.xyz don't work. Revert later
//    val baseUrl = if (BuildConfig.DEBUG) "https://chisto.xyz" else  "https://chis.to"
    val baseUrl = "https://chis.to"
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
