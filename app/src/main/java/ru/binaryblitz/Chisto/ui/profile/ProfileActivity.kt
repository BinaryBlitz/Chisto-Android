package ru.binaryblitz.Chisto.ui.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonObject
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.User
import ru.binaryblitz.Chisto.network.DeviceInfoStore
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.ui.about.AboutActivity
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.order.MyOrdersActivity
import ru.binaryblitz.Chisto.ui.order.OrdersActivity
import ru.binaryblitz.Chisto.ui.order.WebActivity
import ru.binaryblitz.Chisto.utils.AndroidUtilities
import ru.binaryblitz.Chisto.utils.AppConfig

class ProfileActivity : BaseActivity() {
    val EXTRA_PHONE = "phone"
    val EXTRA_SELECTED = "selected"
    val EXTRA_URL = "url"
    val SELECTED_CONTACT_INFO_ACTIVITY = 2
    val SELECTED_ORDERS_ACTIVITY = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_profile)
        setOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        initElements()
        Handler().post { if (DeviceInfoStore.getToken(this) != "null") { getUser() } }
    }

    private fun initElements() {
        if (DeviceInfoStore.getToken(this) == "null") {
            findViewById(R.id.quit_btn).visibility = View.GONE
        } else {
            findViewById(R.id.quit_btn).visibility = View.VISIBLE
        }
    }

    private fun openActivity(selected: Int, activity: Class<out Activity>) {
        val intent = Intent(this@ProfileActivity, activity)
        intent.putExtra(EXTRA_SELECTED, selected)
        startActivity(intent)
    }

    private fun openActivity(activity: Class<out Activity>) {
        val intent = Intent(this@ProfileActivity, activity)
        startActivity(intent)
    }

    private fun setOnClickListeners() {
        findViewById(R.id.back_btn).setOnClickListener { finish() }

        findViewById(R.id.contact_data_btn).setOnClickListener {
            if (DeviceInfoStore.getToken(this) == "null") {
                openActivity(SELECTED_CONTACT_INFO_ACTIVITY, RegistrationActivity::class.java)
            } else {
                openActivity(ContactInfoActivity::class.java)
            }
        }

        findViewById(R.id.my_orders_btn).setOnClickListener {
            if (DeviceInfoStore.getToken(this) == "null") {
                openActivity(SELECTED_ORDERS_ACTIVITY, RegistrationActivity::class.java)
            } else {
                openActivity(MyOrdersActivity::class.java)
            }
        }

        findViewById(R.id.about_btn).setOnClickListener {
            val intent = Intent(this@ProfileActivity, AboutActivity::class.java)
            startActivity(intent)
        }

        findViewById(R.id.quit_btn).setOnClickListener {
            logOut()
        }

        findViewById(R.id.rules_btn).setOnClickListener {
            val intent = Intent(this@ProfileActivity, WebActivity::class.java)
            intent.putExtra(EXTRA_URL, AppConfig.terms)
            startActivity(intent)
        }
    }

    private fun generateUserJson(): JsonObject {
        val obj = JsonObject()

        obj.addProperty("device_token", "")
        obj.addProperty("platform", "")

        val toSend = JsonObject()
        toSend.add("user", obj)

        return toSend
    }

    private fun updateUser() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().updateUser(generateUserJson(), DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                finish()
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                finish()
            }
        })
    }

    private fun logOut() {
        OrdersActivity.laundryId = 0
        DeviceInfoStore.resetToken(this)
        DeviceInfoStore.resetUser(this)
        updateUser()
    }

    private fun getUser() {
        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this))
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                            parseUserResponse(response.body())
                        } else {
                            onServerError(response)
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        onInternetConnectionError()
                    }
                })
    }

    private fun parseUserResponse(obj: JsonObject) {
        var user = DeviceInfoStore.getUserObject(this)

        if (user == null) user = User.createDefault()

        if (AndroidUtilities.getStringFieldFromJson(obj.get("phone_number")).isEmpty()) {
            user.phone = intent.getStringExtra(EXTRA_PHONE)
        } else {
            user.phone = AndroidUtilities.getStringFieldFromJson(obj.get("phone_number"))
        }

        user.id = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        user.firstName = AndroidUtilities.getStringFieldFromJson(obj.get("first_name"))
        user.lastname = AndroidUtilities.getStringFieldFromJson(obj.get("last_name"))
        user.streetName = AndroidUtilities.getStringFieldFromJson(obj.get("street_name"))
        user.apartmentNumber = AndroidUtilities.getStringFieldFromJson(obj.get("house_number"))
        user.notes = AndroidUtilities.getStringFieldFromJson(obj.get("notes"))
        user.houseNumber = AndroidUtilities.getStringFieldFromJson(obj.get("apartment_number"))
        user.email = AndroidUtilities.getStringFieldFromJson(obj.get("email"))

        val ordersCount = AndroidUtilities.getIntFieldFromJson(obj.get("orders_count"))
        if (ordersCount == 0) {
            (findViewById(R.id.indicator) as TextView).visibility = View.GONE
        } else {
            (findViewById(R.id.indicator) as TextView).visibility = View.VISIBLE
            (findViewById(R.id.indicator) as TextView).text = Integer.toString(ordersCount)
        }

        DeviceInfoStore.saveUser(this, user)
    }
}
