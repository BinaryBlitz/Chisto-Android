package ru.binaryblitz.Chisto.ui.start

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.google.android.gms.common.api.ResolvableApiException
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_select_city.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.data.LocationProvider
import ru.binaryblitz.Chisto.entities.City
import ru.binaryblitz.Chisto.entities.User
import ru.binaryblitz.Chisto.extension.showSettingsRequest
import ru.binaryblitz.Chisto.network.DeviceInfoStore
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.presentation.SelectCityPresenter
import ru.binaryblitz.Chisto.presentation.SelectLocationView
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.categories.CategoryActivity
import ru.binaryblitz.Chisto.ui.profile.ContactInfoActivity
import ru.binaryblitz.Chisto.ui.start.adapters.CitiesAdapter
import ru.binaryblitz.Chisto.utils.AndroidUtilities
import ru.binaryblitz.Chisto.utils.CustomPhoneNumberTextWatcher
import ru.binaryblitz.Chisto.utils.LogUtil
import java.io.IOException
import java.util.*


@RuntimePermissions
class SelectCityActivity : BaseActivity(), SwipeRefreshLayout.OnRefreshListener,
        SelectLocationView {

    private lateinit var phoneEditText: EditText
    private lateinit var cityEditText: EditText
    private val adapter by lazy {
        CitiesAdapter(
                context = this,
                listener = this::finishSelectCity
        )
    }

    private val presenter by lazy { SelectCityPresenter(this, LocationProvider(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_city)
        Fabric.with(this, Crashlytics())

        back_btn.setOnClickListener { finish() }
        my_loc_btn.setOnClickListener { grantLocationPermissionsWithPermissionCheck() }
        city_not_found_btn.setOnClickListener { showDialog() }

        initList()

        Handler().post {
            refresh.isEnabled = true
            load()
        }
    }

    override fun onPause() {
        presenter.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    private fun initDialog(dialog: MaterialDialog) {
        dialog.customView?.run {
            this@SelectCityActivity.phoneEditText = findViewById(R.id.phoneEditText)
            this@SelectCityActivity.cityEditText = findViewById(R.id.cityEditText)
            this@SelectCityActivity.phoneEditText.addTextChangedListener(CustomPhoneNumberTextWatcher())
        } ?: return

    }

    override fun onRefresh() {
        load()
    }

    override fun showUserPosition(location: Location) {
        location.let { load(it.latitude, it.longitude) }
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun grantLocationPermissions() {
        presenter.onLocationPermissionsGrant()
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationDenied() {
        showSettingsRequest(R.string.location_error)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun showLocationSettings(e: ResolvableApiException) {
        e.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) presenter.startLocationUpdates()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun load(latitude: Double, longitude: Double) {
        val gcd = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1)
            if (addresses.isNotEmpty()) {
                if (adapter.itemCount == 0) {
                    cityError()
                } else {
                    adapter.selectCity(addresses[0].latitude, addresses[0].longitude)
                }
            } else {
                onLocationError()
            }
        } catch (e: IOException) {
            LogUtil.logException(e)
        }
    }

    private fun cityError() {
        Snackbar.make(main, ru.binaryblitz.Chisto.R.string.city_error, Snackbar.LENGTH_SHORT).show()
    }

    private fun initList() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SelectCityActivity)
            itemAnimator = DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = this@SelectCityActivity.adapter
        }

        refresh.setOnRefreshListener(this)
        refresh.setColorSchemeResources(R.color.colorAccent)
    }

    private fun load() {
        ServerApi.get(this).api().citiesList.enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                refresh.isRefreshing = false

                if (response.isSuccessful) {
                    parseAnswer(response.body())
                } else {
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                refresh.isRefreshing = false
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(array: JsonArray?) {
        val collection = (0 until array!!.size())
                .map { array.get(it).asJsonObject }
                .map {
                    City(it.get("id").asInt,
                            AndroidUtilities.getStringFieldFromJson(it.get("name")),
                            AndroidUtilities.getDoubleFieldFromJson(it.get("latitude")),
                            AndroidUtilities.getDoubleFieldFromJson(it.get("longitude")))
                }
                .mapTo(ArrayList()) { CitiesAdapter.City(it, false) }

        adapter.setCollection(collection)
        adapter.notifyDataSetChanged()
    }

    private fun showDialog() {
        val dialog = MaterialDialog.Builder(this@SelectCityActivity)
                .title(ru.binaryblitz.Chisto.R.string.app_name)
                .customView(R.layout.city_not_found_dialog, true)
                .positiveText(ru.binaryblitz.Chisto.R.string.send_code)
                .negativeText(ru.binaryblitz.Chisto.R.string.back_code)
                .autoDismiss(false)
                .onPositive { dialog, _ ->
                    if (checkDialogInput()) {
                        sendSubscription(dialog)
                    }
                }
                .onNegative { dialog, _ -> dialog.dismiss() }
                .show()

        initDialog(dialog)
    }

    private fun generateJson(): JsonObject {
        val `object` = JsonObject()
        val toSend = JsonObject()
        `object`.addProperty("phone_number", phoneEditText.text.toString())
        `object`.addProperty("content", cityEditText.text.toString())
        toSend.add("subscription", `object`)

        return toSend
    }

    private fun finishSelectCity(city: City) {
        DeviceInfoStore.saveCity(this, city)

        if (DeviceInfoStore.getToken(this) == "null") {
            saveUser(city)
        }

        if (intent.getIntExtra(ContactInfoActivity.EXTRA_CHECK_RESULT, 0) == 0) {
            val intent = Intent(this, CategoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        finish()
    }

    private fun saveUser(city: ru.binaryblitz.Chisto.entities.City) {
        val user = User.createDefault()
        user.city = city.name
        DeviceInfoStore.saveUser(this, user)
    }

    private fun sendSubscription(dialog: MaterialDialog) {
        val progressDialog = ProgressDialog(this)
        progressDialog.show()

        ServerApi.get(this).api().sendSubscription(generateJson()).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                progressDialog.dismiss()
                dialog.dismiss()
                if (!response.isSuccessful) {
                    onInternetConnectionError()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                progressDialog.dismiss()
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    private fun checkDialogInput(): Boolean {
        var res = true

        if (!AndroidUtilities.validatePhone(phoneEditText.text.toString())) {
            phoneEditText.error = getString(R.string.wrong_phone_code)
            res = false
        }

        if (cityEditText.text.toString().isEmpty()) {
            cityEditText.error = getString(R.string.wrong_city_code)
            res = false
        }

        return res
    }

    private companion object {
        private const val REQUEST_CHECK_SETTINGS = 1
    }
}
