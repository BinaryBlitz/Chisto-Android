package ru.binaryblitz.Chisto.Server

object ServerConfig {
    val baseUrl = "https://chisto.xyz"
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
