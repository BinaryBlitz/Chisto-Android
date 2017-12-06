package ru.binaryblitz.Chisto.presentation


import android.location.Location
import ru.binaryblitz.Chisto.data.LocationProvider


class SelectCityPresenter(
        private var view: SelectLocationView? = null,
        private val locationProvider: LocationProvider
) {
    init {
        locationProvider.onUpdated = { location -> onLocationUpdated(location) }
    }

    fun onLocationPermissionsGrant() {
        locationProvider.permissionsGranted = true
        startLocationUpdates()
    }

    fun onResume() {
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        locationProvider.startLocationUpdates { onUpdatingChanged(it) }
    }

    fun onPause() {
        locationProvider.stopLocationUpdates { onUpdatingChanged(false) }
    }

    private fun onUpdatingChanged(updating: Boolean) {
    }

    private fun onLocationUpdated(location: Location) {
        view?.showUserPosition(location)
    }

    fun onDestroy() {
        view = null
    }
}
