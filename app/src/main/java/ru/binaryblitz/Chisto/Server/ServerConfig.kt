package ru.binaryblitz.Chisto.Server

object ServerConfig {
    val baseUrl = "http://chisto-staging.herokuapp.com"
    val apiURL = baseUrl + "/api/"

    val imageUrl: String
        get() {
            return baseUrl
        }
}
