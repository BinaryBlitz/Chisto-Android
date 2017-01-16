package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle

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

class ProfileActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_profile)

        setOnClickListeners()
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
            quit()
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

    private fun quit() {
        DeviceInfoStore.resetToken(this)
        DeviceInfoStore.resetUser(this)
        updateUser()
    }
}
