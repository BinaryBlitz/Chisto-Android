package ru.binaryblitz.Chisto.data

import android.app.Activity
import android.location.Location
import android.os.Looper
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import timber.log.Timber


class LocationProvider(activity: Activity) {

    companion object {
        private const val TAG = "LocationProvider"
        private const val MILLS_PER_SEC: Long = 1000
    }

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val interval = 10

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private val fastestInterval
        get() = interval / 2

    /**
     * Provides access to the Fused Location Provider API.
     */
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(activity) }

    /**
     * Provides access to the Location Settings API.
     */
    private val settingsClient by lazy { LocationServices.getSettingsClient(activity) }

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private val locationRequest by lazy { createLocationRequest() }

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private val locationSettingsRequest by lazy { buildLocationSettingsRequest() }

    /**
     * Callback for Location events.
     */
    private val locationCallback by lazy { createLocationCallback() }

    /**
     * External callback for Location events.
     */
    var onUpdated: (location: Location) -> Unit = { }

    /**
     * Tracks the status of the location updates request.
     */
    private var requestingLocationUpdates: Boolean = false

    var permissionsGranted: Boolean = false

    private fun createLocationRequest(): LocationRequest = LocationRequest().also {
        it.interval = interval * MILLS_PER_SEC
        it.fastestInterval = fastestInterval * MILLS_PER_SEC
        it.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    /**
     * Uses a [LocationSettingsRequest.Builder] to build a [LocationSettingsRequest] that is used
     * for checking if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest(): LocationSettingsRequest {
        return LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .build()
    }

    /**
     * Creates a callback for receiving location events.
     */
    private fun createLocationCallback() = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            Timber.d("onLocationResult")
            onUpdated(result.lastLocation)
        }
    }

    fun checkLocationSettings(onSuccess: () -> Unit, onException: (ResolvableApiException) -> Unit) {
        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener { onSuccess.invoke() }
                .addOnFailureListener {
                    val statusCode = (it as ApiException).statusCode
                    Timber.d(statusCode.toString())

                    when (statusCode) {
                        CommonStatusCodes.RESOLUTION_REQUIRED -> onException(it as ResolvableApiException)
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        }
                    }
                }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    fun startLocationUpdates(onComplete: (Boolean) -> Unit) {
        if (!permissionsGranted) return
        Timber.d("startLocationUpdates")
        if (requestingLocationUpdates) return

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                .addOnSuccessListener { requestingLocationUpdates = true }
                .addOnFailureListener { requestingLocationUpdates = false }
                .addOnCompleteListener { onComplete(requestingLocationUpdates) }
    }

    fun stopLocationUpdates(onComplete: () -> Unit) {
        if (!requestingLocationUpdates) return

        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener {
                    requestingLocationUpdates = false
                    onComplete()
                }
    }
}
