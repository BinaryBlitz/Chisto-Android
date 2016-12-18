package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.telephony.PhoneNumberFormattingTextWatcher
import android.widget.EditText
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.rengwuxian.materialedittext.MaterialEditText
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Model.Treatment
import ru.binaryblitz.Chisto.Model.User
import ru.binaryblitz.Chisto.Push.RegistrationIntentService
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import ru.binaryblitz.Chisto.Utils.Animations.Animations
import ru.binaryblitz.Chisto.Utils.LogUtil
import ru.binaryblitz.Chisto.Utils.OrderList
import java.util.*
import java.util.regex.Pattern

class PersonalInfoActivity : BaseActivity() {
    val EXTRA_PHONE = "phone"
    val REQUEST_WEB = 100

    private var name: MaterialEditText? = null
    private var lastname: MaterialEditText? = null
    private var city: MaterialEditText? = null
    private var street: MaterialEditText? = null
    private var house: MaterialEditText? = null
    private var flat: MaterialEditText? = null
    private var comment: MaterialEditText? = null
    private var phone: MaterialEditText? = null
    private var email: MaterialEditText? = null

    private var user: User? = null

    private var dialogOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_contact_info)

        initFields()
        setOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        setInfo()
    }

    override fun onBackPressed() {
        finishActivity()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_WEB) {
            if (data!!.getBooleanExtra("success", false)) {
                showOrderDialog(orderId)
            } else {
                Snackbar.make(findViewById(R.id.main), getString(R.string.payment_error), Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateOrderTreatments(): JsonArray {
        val array = JsonArray()
        val orders = OrderList.get()

        for ((category, treatments, count, color, decoration, decorationCost, size) in orders!!) {
            val isDecoration = checkDecoration(treatments!!)
            for ((id, name1, description, cost, select, laundryTreatmentId) in treatments) {
                if (id == -1) continue
                val local = JsonObject()
                local.addProperty("laundry_treatment_id", laundryTreatmentId)
                local.addProperty("quantity", size ?: count)
                local.addProperty("has_decoration", isDecoration)
                array.add(local)
            }
        }

        return array
    }

    private fun checkDecoration(treatments: ArrayList<Treatment>): Boolean {
        return treatments.any { it.id == -1 }
    }

    private fun generateJson(): JsonObject {
        val obj = JsonObject()

        obj.addProperty("street_name", street!!.text.toString())
        obj.addProperty("house_number", house!!.text.toString())
        obj.addProperty("contact_number", phone!!.text.toString())
        obj.addProperty("apartment_number", flat!!.text.toString())
        obj.addProperty("notes", comment!!.text.toString())
        obj.addProperty("email", "foo@bar.com")

        obj.add("line_items_attributes", generateOrderTreatments())

        val toSend = JsonObject()
        toSend.add("order", obj)

        LogUtil.logError(toSend.toString())

        return toSend
    }

    private fun sendToServer(payWithCreditCard: Boolean) {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().sendOrder(OrderList.getLaundry().id, generateJson(), DeviceInfoStore.getToken(this))
                .enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                if (response.isSuccessful) parseAnswer(response.body(), payWithCreditCard)
                else onServerError(response)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(obj: JsonObject, payWithCreditCard: Boolean) {
        orderId = obj.get("id").asInt
        if (payWithCreditCard) openWebActivity(obj.get("payment").asJsonObject.get("payment_url").asString)
        else showOrderDialog(orderId)
    }

    private fun openWebActivity(url: String) {
        val intent = Intent(this@PersonalInfoActivity, WebActivity::class.java)
        intent.putExtra("url", url)
        startActivityForResult(intent, REQUEST_WEB)
    }

    private fun showOrderDialog(id: Int) {
        Handler().post {
            dialogOpened = true
            (findViewById(R.id.order_name) as TextView).text = "№ " + id.toString()
            Animations.animateRevealShow(findViewById(ru.binaryblitz.Chisto.R.id.dialog), this@PersonalInfoActivity)
        }
    }

    private fun setOnClickListeners() {
        findViewById(R.id.cont_btn).setOnClickListener {
            OrderList.clear()
            goToOrderActivity()
        }

        findViewById(R.id.left_btn).setOnClickListener {
            finishActivity() }

        findViewById(R.id.pay_btn).setOnClickListener {
            process(false)
        }

        findViewById(R.id.credit_card_btn).setOnClickListener {
            process(true)
        }

        findViewById(R.id.address_btn).setOnClickListener {
            startActivity(Intent(this@PersonalInfoActivity, MapActivity::class.java))
        }
    }

    private fun process(payWithCreditCard: Boolean) {
        if (validateFields()) {
            setData()
            val auth = DeviceInfoStore.getToken(this) == null || DeviceInfoStore.getToken(this) == "null"
            if (auth) createUser(payWithCreditCard)
            else updateUser(payWithCreditCard)
        }
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
        phone!!.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        (findViewById(R.id.price) as TextView).text = intent.getIntExtra(EXTRA_PRICE, 0).toString() + " \u20bd"
    }

    private fun setInfo() {
        user = DeviceInfoStore.getUserObject(this)

        setTextToField(city!!, user!!.city)

        if (user!!.name == null || user!!.name == "null") {
            setTextToField(phone!!, intent.getStringExtra(EXTRA_PHONE))
        } else {
            setTextToField(phone!!, user!!.phone)
        }
        setTextToField(email!!, user!!.email)
        setTextToField(name!!, user!!.name)
        setTextToField(lastname!!, user!!.lastname)
        setTextToField(flat!!, user!!.flat)
        setTextToField(phone!!, user!!.phone)
        setTextToField(house!!, user!!.house)
        setTextToField(street!!, user!!.street)
    }

    private fun setData() {
        if (user == null) user = User(1, null, null, null, null, null, null, null, null)

        user!!.name = name!!.text.toString()
        user!!.lastname = lastname!!.text.toString()
        user!!.city = city!!.text.toString()
        user!!.flat = flat!!.text.toString()
        user!!.phone = phone!!.text.toString()
        user!!.street = street!!.text.toString()
        user!!.house = house!!.text.toString()
        user!!.email = email!!.text.toString()

        DeviceInfoStore.saveUser(this, user)
    }

    private fun generateUserJson(): JsonObject {
        val obj = JsonObject()
        obj.addProperty("first_name", name!!.text.toString())
        obj.addProperty("last_name", lastname!!.text.toString())
        obj.addProperty("phone_number", AndroidUtilities.processText(phone!!))
        obj.addProperty("city_id", DeviceInfoStore.getCityObject(this).id)
        obj.addProperty("email", email!!.text.toString())
        if (DeviceInfoStore.getToken(this) == null || DeviceInfoStore.getToken(this) == "null") {
            obj.addProperty("verification_token", intent.getStringExtra(EXTRA_TOKEN))
        }

        val toSend = JsonObject()
        toSend.add("user", obj)

        LogUtil.logError(toSend.toString())

        return toSend
    }

    private fun parseUserAnswer(payWithCreditCard: Boolean, obj: JsonObject) {
        LogUtil.logError(obj.toString())
        DeviceInfoStore.saveToken(this, obj.get("api_token").asString)
        if (AndroidUtilities.checkPlayServices(this)) {
            val intent = Intent(this@PersonalInfoActivity, RegistrationIntentService::class.java)
            startService(intent)
        }
        sendToServer(payWithCreditCard)
    }

    private fun updateUser(payWithCreditCard: Boolean) {
        sendToServer(payWithCreditCard)

        ServerApi.get(this).api().updateUser(generateUserJson(), DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) { }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) { }
        })
    }

    private fun createUser(payWithCreditCard: Boolean) {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().createUser(generateUserJson()).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                if (response.isSuccessful) parseUserAnswer(payWithCreditCard, response.body())
                else onServerError(response)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
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
        if (!AndroidUtilities.validatePhone(phone!!.text.toString())) {
            editText.error = getString(R.string.wrong_data)
            return false
        }

        return true
    }

    private fun validateField(editText: MaterialEditText, numbers: Boolean): Boolean {
        var count = 0

        if (numbers) count = findNumbers(editText)

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

    private fun goToOrderActivity() {
        val intent = Intent(this@PersonalInfoActivity, OrdersActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun finishActivity() {
        finish()
    }

    companion object {
        private val EXTRA_PRICE = "price"
        private val EXTRA_TOKEN = "token"
        var orderId: Int = 0
    }
}
