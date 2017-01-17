package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonObject
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Utils.AppConfig
import ru.binaryblitz.Chisto.Model.User
import ru.binaryblitz.Chisto.Utils.AndroidUtilities

class ProfileActivity : BaseActivity() {
    val EXTRA_PHONE = "phone"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_profile)

        setOnClickListeners()
        Handler().post { getUser() }
    }

    private fun setOnClickListeners() {
        findViewById(R.id.back_btn).setOnClickListener { finish() }

        findViewById(R.id.contact_data_btn).setOnClickListener {
            val intent = Intent(this@ProfileActivity, ContactInfoActivity::class.java)
            startActivity(intent)
        }

        findViewById(R.id.my_orders_btn).setOnClickListener {
            val intent = Intent(this@ProfileActivity, MyOrdersActivity::class.java)
            startActivity(intent)
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
            intent.putExtra("url", AppConfig.terms)
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
        DeviceInfoStore.resetToken(this)
        DeviceInfoStore.resetUser(this)
        updateUser()
    }

    private fun getUser() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this))
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        dialog.dismiss()
                        if (response.isSuccessful) parseUserResponse(response.body())
                        else onServerError(response)
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        dialog.dismiss()
                        onInternetConnectionError()
                    }
                })
    }

    private fun parseUserResponse(obj: JsonObject) {
        var user = DeviceInfoStore.getUserObject(this)

        if (user == null) user = User.createDefault()

        if (AndroidUtilities.getStringFieldFromJson(obj.get("phone_number")).isEmpty())
            user.phone = intent.getStringExtra(EXTRA_PHONE)
        else
            user.phone = AndroidUtilities.getStringFieldFromJson(obj.get("phone_number"))

        user.id = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        user.firstName = AndroidUtilities.getStringFieldFromJson(obj.get("first_name"))
        user.lastname = AndroidUtilities.getStringFieldFromJson(obj.get("last_name"))
        user.streetName = AndroidUtilities.getStringFieldFromJson(obj.get("street_name"))
        user.apartmentNumber = AndroidUtilities.getStringFieldFromJson(obj.get("house_number"))
        user.notes = AndroidUtilities.getStringFieldFromJson(obj.get("notes"))
        user.houseNumber = AndroidUtilities.getStringFieldFromJson(obj.get("apartment_number"))
        user.email = AndroidUtilities.getStringFieldFromJson(obj.get("email"))

        if (user.notes!!.isEmpty()) user.notes = "null"

        DeviceInfoStore.saveUser(this, user)
    }
}
