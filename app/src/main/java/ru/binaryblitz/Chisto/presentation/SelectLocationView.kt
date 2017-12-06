package ru.binaryblitz.Chisto.presentation

import android.location.Location


interface SelectLocationView {
    fun showUserPosition(location: Location)
}
