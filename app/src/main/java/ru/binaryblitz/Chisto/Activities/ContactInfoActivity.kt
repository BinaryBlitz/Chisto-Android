package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonObject
import com.rengwuxian.materialedittext.MaterialEditText
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Model.User
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_profile_contact_info)

        initFields()
        setOnClickListeners()
    }

    override fun onBackPressed() {
        if (!validateFields()) showDialog()
        else setData()
    }

    override fun onResume() {
        super.onResume()
        setInfo()
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener {
            if (!validateFields()) showDialog()
            else setData()
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

    private fun setInfo() {
        user = DeviceInfoStore.getUserObject(this) ?: return
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
        if (user!!.notes!!.isEmpty()) user!!.notes = "null"
        DeviceInfoStore.saveUser(this, user)

        updateUser()
    }

    private fun generateUserJson(): JsonObject {
        val obj = JsonObject()
        obj.addProperty("first_name", name!!.text.toString())
        obj.addProperty("last_name", lastname!!.text.toString())
        obj.addProperty("phone_number", AndroidUtilities.processText(phone!!))
        obj.addProperty("city_id", DeviceInfoStore.getCityObject(this).id)
        obj.addProperty("email", email!!.text.toString())

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
