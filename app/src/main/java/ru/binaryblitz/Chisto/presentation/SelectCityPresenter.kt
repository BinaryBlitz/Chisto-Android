package ru.binaryblitz.Chisto.presentation


import android.location.Location
import com.google.android.gms.common.api.ResolvableApiException
import ru.binaryblitz.Chisto.data.LocationProvider
import timber.log.Timber


class SelectCityPresenter(
        private var view: SelectLocationView? = null,
        private val locationProvider: LocationProvider
) {
    init {
        locationProvider.onUpdated = { location -> onLocationUpdated(location) }
    }

    fun onLocationPermissionsGrant() {
        Timber.d("onLocationPermissionsGrant")
        locationProvider.permissionsGranted = true
        locationProvider.checkLocationSettings(this::startLocationUpdates, this::onResolutionRequired)
    }

    fun onResume() {
        startLocationUpdates()
    }

    fun startLocationUpdates() {
        locationProvider.startLocationUpdates(this::onUpdatingChanged)
    }

    fun onPause() {
        locationProvider.stopLocationUpdates { onUpdatingChanged(false) }
    }

    private fun onUpdatingChanged(updating: Boolean) {
    }

    private fun onLocationUpdated(location: Location) {
        view?.showUserPosition(location)
    }

    private fun onResolutionRequired(e: ResolvableApiException) {
        view?.showLocationSettings(e)
    }

    fun onDestroy() {
        view = null
    }
}
