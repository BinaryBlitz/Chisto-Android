package ru.binaryblitz.Chisto.presentation

import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException


interface SelectLocationView {
    fun showUserPosition(location: Location)
    fun showLocationSettings(e: ResolvableApiException)
}
