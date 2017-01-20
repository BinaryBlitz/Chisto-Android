package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.JsonObject
import com.rengwuxian.materialedittext.MaterialEditText
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Model.User
import ru.binaryblitz.Chisto.Push.MyInstanceIDListenerService
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import ru.binaryblitz.Chisto.Utils.CustomPhoneNumberTextWatcher
import java.util.regex.Pattern

class ContactInfoActivity : BaseActivity() {
    private var name: MaterialEditText? = null
    private var lastname: MaterialEditText? = null
    private var city: MaterialEditText? = null
    private var street: MaterialEditText? = null
    private var house: MaterialEditText? = null
    private var flat: MaterialEditText? = null
    private var comment: MaterialEditText? = null
    private var email: MaterialEditText? = null
    private var phone: MaterialEditText? = null

    private var user: User? = null

    private val EXTRA_TOKEN = "token"
    private val EXTRA_PHONE = "phone"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_profile_contact_info)

        initFields()
        setOnClickListeners()

        Handler().post { getUser() }
    }

    private fun getUser() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this))
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            parseUserResponse(response.body())
                        } else {
                            onServerError(response)
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        dialog.dismiss()
                        onInternetConnectionError()
                    }
                })
    }

    private fun parseUserResponse(obj: JsonObject) {
        var user = DeviceInfoStore.getUserObject(this)

        if (user == null) {
            user = User.createDefault()
        }

        user.id = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        user.firstName = AndroidUtilities.getStringFieldFromJson(obj.get("first_name"))
        user.lastname = AndroidUtilities.getStringFieldFromJson(obj.get("last_name"))
        user.streetName = AndroidUtilities.getStringFieldFromJson(obj.get("street_name"))
        user.apartmentNumber = AndroidUtilities.getStringFieldFromJson(obj.get("house_number"))
        user.notes = AndroidUtilities.getStringFieldFromJson(obj.get("notes"))
        user.houseNumber = AndroidUtilities.getStringFieldFromJson(obj.get("apartment_number"))
        user.email = AndroidUtilities.getStringFieldFromJson(obj.get("email"))
        DeviceInfoStore.saveUser(this, user)
        setInfo()
    }

    override fun onBackPressed() {
        finishActivity()
    }

    private fun finishActivity() {
        if (DeviceInfoStore.getToken(this) == "null") {
            finishIfNotLoggedIn()
        } else {
            finishIfLoggedIn()
        }
    }

    private fun finishIfLoggedIn() {
        if (!validateFields()) {
            showDialog()
        } else {
            setData()
        }
    }

    private fun finishIfNotLoggedIn() {
        showDialogIfNotLoggedIn()
    }

    override fun onResume() {
        super.onResume()
        setInfo()
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener {
            if (!validateFields()) {
                showDialog()
            } else {
                setData()
            }
        }

        findViewById(R.id.address_btn).setOnClickListener {
            startActivity(Intent(this@ContactInfoActivity, MapActivity::class.java))
        }

        findViewById(R.id.street_btn).setOnClickListener {
            startActivity(Intent(this@ContactInfoActivity, MapActivity::class.java))
        }
    }

    private fun showDialog() {
        MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(getString(R.string.profile_wrong_fields))
                .positiveText(R.string.yes_code)
                .negativeText(R.string.no_code)
                .onPositive { dialog, action -> run { finish() } }
                .onNegative { dialog, action -> run { dialog.dismiss() } }
                .show()
    }

    private fun showDialogIfNotLoggedIn() {
        MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(getString(R.string.registration_not_completed_error))
                .positiveText(R.string.yes_code)
                .negativeText(R.string.no_code)
                .onPositive { dialog, action -> run { finish() } }
                .onNegative { dialog, action -> run { dialog.dismiss() } }
                .show()
    }

    private fun initFields() {
        name = findViewById(R.id.name_text) as MaterialEditText
        lastname = findViewById(R.id.lastname_text) as MaterialEditText
        city = findViewById(R.id.city_text) as MaterialEditText
        street = findViewById(R.id.street_text) as MaterialEditText
        house = findViewById(R.id.house_text) as MaterialEditText
        flat = findViewById(R.id.flat_text) as MaterialEditText
        phone = findViewById(R.id.phone) as MaterialEditText
        comment = findViewById(R.id.comment_text) as MaterialEditText
        email = findViewById(R.id.email) as MaterialEditText
        phone!!.addTextChangedListener(CustomPhoneNumberTextWatcher())
    }

    private fun generateUserJson(): JsonObject {
        val obj = JsonObject()
        obj.addProperty("first_name", name!!.text.toString())
        obj.addProperty("last_name", lastname!!.text.toString())
        obj.addProperty("apartment_number", flat!!.text.toString())
        obj.addProperty("house_number", house!!.text.toString())
        obj.addProperty("street_name", street!!.text.toString())
        obj.addProperty("notes", comment!!.text.toString())
        obj.addProperty("phone_number", AndroidUtilities.processText(phone!!))
        obj.addProperty("city_id", DeviceInfoStore.getCityObject(this).id)
        obj.addProperty("email", email!!.text.toString())
        obj.addProperty("device_token", FirebaseInstanceId.getInstance().token)
        obj.addProperty("platform", "android")
        if (DeviceInfoStore.getToken(this) == null || DeviceInfoStore.getToken(this) == "null") {
            obj.addProperty("verification_token", intent.getStringExtra(EXTRA_TOKEN))
        }

        val toSend = JsonObject()
        toSend.add("user", obj)

        return toSend
    }

    private fun parseUserAnswer(obj: JsonObject) {
        DeviceInfoStore.saveToken(this, obj.get("api_token").asString)
        if (AndroidUtilities.checkPlayServices(this)) {
            val intent = Intent(this@ContactInfoActivity, MyInstanceIDListenerService::class.java)
            startService(intent)
        }

        finish()
    }

    private fun createUser() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().createUser(generateUserJson()).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    parseUserAnswer(response.body())
                } else {
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    private fun setInfo() {
        user = DeviceInfoStore.getUserObject(this)

        if (user == null) {
            setTextToField(phone!!, intent.getStringExtra(EXTRA_PHONE))
            return
        }

        setTextToField(email!!, user!!.email)
        setTextToField(city!!, DeviceInfoStore.getCityObject(this).name)
        setTextToField(name!!, user!!.firstName)
        setTextToField(lastname!!, user!!.lastname)
        setTextToField(flat!!, user!!.apartmentNumber)
        setTextToField(phone!!, user!!.phone)
        setTextToField(house!!, user!!.houseNumber)
        setTextToField(street!!, user!!.streetName)
        setTextToField(phone!!, user!!.phone)
        setTextToField(comment!!, user!!.notes)
    }

    private fun setData() {
        if (user == null) user = User.createDefault()

        user!!.firstName = name!!.text.toString()
        user!!.lastname = lastname!!.text.toString()
        user!!.city = city!!.text.toString()
        user!!.apartmentNumber = flat!!.text.toString()
        user!!.phone = phone!!.text.toString()
        user!!.streetName = street!!.text.toString()
        user!!.houseNumber = house!!.text.toString()
        user!!.email = email!!.text.toString()
        user!!.notes = comment!!.text.toString()
        DeviceInfoStore.saveUser(this, user)

        if (DeviceInfoStore.getToken(this) == "null") {
            createUser()
        } else {
            updateUser()
        }
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

    private fun setTextToField(editText: EditText, text: String?) {
        if (text != null && !text.isEmpty() && text != "null") {
            editText.setText(text)
        }
    }

    private fun validateFields(): Boolean {
        var res = validateField(name!!, true)
        res = res and validateField(lastname!!, true)
        res = res and validateField(city!!, true)
        res = res and validateField(street!!, false)
        res = res and validateField(house!!, false)
        res = res and validateField(flat!!, false)
        res = res and validatePhoneField(phone!!)
        res = res and validateEmailField(email!!)

        return res
    }

    private fun validateEmailField(editText: MaterialEditText): Boolean {
        if (!AndroidUtilities.validateEmail(editText.text.toString())) {
            editText.error = getString(R.string.wrong_data)
            return false
        }

        return true
    }

    private fun validatePhoneField(editText: MaterialEditText): Boolean {
        if (!AndroidUtilities.validatePhone(editText.text.toString())) {
            editText.error = getString(R.string.wrong_data)
            return false
        }

        return true
    }

    private fun validateField(editText: MaterialEditText, numbers: Boolean): Boolean {
        var count = 0

        if (numbers) {
            count = findNumbers(editText)
        }

        if (editText.text.toString().isEmpty() || count != 0) {
            editText.error = getString(R.string.wrong_data)
            return false
        }

        return true
    }

    private fun findNumbers(editText: MaterialEditText): Int {
        val pattern = Pattern.compile("-?\\d+")
        val matcher = pattern.matcher(editText.text.toString())

        var count = 0
        while (matcher.find())
            count++

        return count
    }
}
