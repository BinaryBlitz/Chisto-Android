package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import cn.refactor.library.SmoothCheckBox
import com.crashlytics.android.Crashlytics
import com.google.firebase.iid.FirebaseInstanceId
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
import ru.binaryblitz.Chisto.Push.MyInstanceIDListenerService
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Utils.*
import java.util.*
import java.util.regex.Pattern

class PersonalInfoActivity : BaseActivity() {
    val EXTRA_PHONE = "phone"
    val REQUEST_WEB = 100

    val CASH = "cash"
    val CARD = "card"
    val GREY_COLOR = "#727272"
    val BLACK_COLOR = "#212121"
    private var greyColor: Int = Color.parseColor("#CFCFCF")

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

    private var selectedPaymentType = CASH

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_contact_info)

        initFields()
        setOnClickListeners()

        if (isUserExistOnServer()) Handler().post { getUser() }
    }

    private fun isUserExistOnServer(): Boolean {
        return DeviceInfoStore.getToken(this) != "null"
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
        if (user == null) user = User.createDefault()
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

    override fun onResume() {
        super.onResume()
        setInfo()
    }

    override fun onBackPressed() {
        finishActivity()
    }

    private fun showPaymentError() {
        Snackbar.make(findViewById(R.id.main), getString(R.string.payment_error), Snackbar.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_WEB) {
            if (data!!.getBooleanExtra("success", false)) {
                complete(orderId)
            } else {
                showPaymentError()
            }
        }
    }

    private fun generateOrderTreatments(): JsonArray {
        val array = JsonArray()
        val orders = OrderList.get()

        for ((category, treatments, count, color, decoration, decorationCost, size) in orders!!) {
            val isDecoration = checkDecoration(treatments!!)
            val local = JsonObject()
            local.addProperty("item_id", category.id)
            local.addProperty("quantity", count)
            if (size != null) {
                local.addProperty("area", size)
            }
            local.addProperty("has_decoration", isDecoration)

            val treatmentsArray = JsonArray()
            for ((id, treatmentName, description, cost, select, laundryTreatmentId) in treatments) {
                if (id == AppConfig.decorationId) continue
                val treatmentJson = JsonObject()
                treatmentJson.addProperty("laundry_treatment_id", laundryTreatmentId)
                treatmentsArray.add(treatmentJson)
            }
            local.add("order_treatments_attributes", treatmentsArray)
            array.add(local)
        }

        return array
    }

    private fun checkDecoration(treatments: ArrayList<Treatment>): Boolean {
        return treatments.any { it.id == AppConfig.decorationId }
    }

    private fun generateJson(): JsonObject {
        val obj = JsonObject()

        obj.addProperty("street_name", street!!.text.toString())
        obj.addProperty("house_number", house!!.text.toString())
        obj.addProperty("contact_number", AndroidUtilities.processText(phone!!))
        obj.addProperty("apartment_number", flat!!.text.toString())
        obj.addProperty("notes", comment!!.text.toString())
        obj.addProperty("email", email!!.text.toString())
        obj.addProperty("payment_method", selectedPaymentType)

        val promoId = intent.getIntExtra(EXTRA_PROMO_CODE_ID, 0)
        if (promoId != 0) {
            obj.addProperty("promo_code_id", promoId)
        }

        obj.add("order_items_attributes", generateOrderTreatments())

        val toSend = JsonObject()
        toSend.add("order", obj)

        LogUtil.logError(toSend.toString())

        return toSend
    }

    private fun sendToServer(payWithCard: Boolean) {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().sendOrder(OrderList.getLaundry().id, generateJson(), DeviceInfoStore.getToken(this))
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            parseAnswer(response.body(), payWithCard)
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

    private fun parseAnswer(obj: JsonObject, payWithCard: Boolean) {
        orderId = obj.get("id").asInt
        if (payWithCard) openWebActivity(obj.get("payment").asJsonObject.get("payment_url").asString)
        else complete(orderId)
    }

    private fun openWebActivity(url: String) {
        val intent = Intent(this@PersonalInfoActivity, WebActivity::class.java)
        intent.putExtra("url", url)
        startActivityForResult(intent, REQUEST_WEB)
    }

    private fun complete(orderId: Int) {
        OrdersActivity.newOrderId = orderId
        OrderList.clear()
        goToOrderActivity()
    }

    private fun selectBankCard() {
        (findViewById(R.id.bank_text) as TextView).setTextColor(Color.parseColor(BLACK_COLOR))
        (findViewById(R.id.money_text) as TextView).setTextColor(Color.parseColor(GREY_COLOR))

        (findViewById(R.id.visa) as ImageView).setImageResource(R.drawable.ic_visa)
        (findViewById(R.id.master_card) as ImageView).setImageResource(R.drawable.ic_master_card)

        (findViewById(R.id.cardCheckBox) as SmoothCheckBox).isChecked = true
        (findViewById(R.id.cashCheckBox) as SmoothCheckBox).isChecked = false

        selectedPaymentType = CARD
    }

    private fun selectCash() {
        (findViewById(R.id.money_text) as TextView).setTextColor(Color.parseColor(BLACK_COLOR))
        (findViewById(R.id.bank_text) as TextView).setTextColor(Color.parseColor(GREY_COLOR))

        (findViewById(R.id.visa) as ImageView).setImageResource(R.drawable.ic_visa_no_active)
        (findViewById(R.id.master_card) as ImageView).setImageResource(R.drawable.ic_master_card_no_active)

        (findViewById(R.id.cashCheckBox) as SmoothCheckBox).isChecked = true
        (findViewById(R.id.cardCheckBox) as SmoothCheckBox).isChecked = false

        selectedPaymentType = CASH
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener { finishActivity() }

        findViewById(R.id.bank_btn).setOnClickListener { selectBankCard() }

        findViewById(R.id.money_btn).setOnClickListener { selectCash() }

        findViewById(R.id.continue_btn).setOnClickListener { process(selectedPaymentType == CARD) }

        findViewById(R.id.address_btn).setOnClickListener {
            startActivity(Intent(this@PersonalInfoActivity, MapActivity::class.java))
        }

        findViewById(R.id.street_btn).setOnClickListener {
            startActivity(Intent(this@PersonalInfoActivity, MapActivity::class.java))
        }

        findViewById(R.id.dialog).setOnClickListener {
            ru.binaryblitz.Chisto.Utils.Animations.animateRevealHide(findViewById(R.id.dialog))
        }
    }

    private fun process(payWithCreditCard: Boolean) {
        if (validateFields()) {
            setData()
            if (DeviceInfoStore.getToken(this) == null || DeviceInfoStore.getToken(this) == "null") {
                createUser(payWithCreditCard)
            } else {
                updateUser(payWithCreditCard)
            }
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
        phone!!.addTextChangedListener(CustomPhoneNumberTextWatcher())

        initCheckBoxes()

        (findViewById(R.id.price) as TextView).text = intent.getIntExtra(EXTRA_PRICE, 0).toString() + getString(R.string.ruble_sign)
    }

    private fun initCheckBoxes() {
        initCheckBox((findViewById(R.id.cashCheckBox) as SmoothCheckBox))
        initCheckBox((findViewById(R.id.cardCheckBox) as SmoothCheckBox))
        selectBankCard()
    }

    private fun initCheckBox(checkBox: SmoothCheckBox) {
        checkBox.isEnabled = false
        val unCheckedColor = SmoothCheckBox::class.java.getDeclaredField("mUnCheckedColor")
        unCheckedColor.isAccessible = true
        unCheckedColor.set(checkBox, greyColor)
    }

    private fun setInfo() {
        user = DeviceInfoStore.getUserObject(this)

        if (user == null) {
            setTextToField(phone!!, intent.getStringExtra(EXTRA_PHONE))
            return
        }

        setTextToField(city!!, DeviceInfoStore.getCityObject(this).name)

        if (user!!.firstName == null || user!!.firstName == "null") {
            setTextToField(phone!!, intent.getStringExtra(EXTRA_PHONE))
        }
        else {
            setTextToField(phone!!, user!!.phone)
        }

        setTextToField(email!!, user!!.email)
        setTextToField(name!!, user!!.firstName)
        setTextToField(lastname!!, user!!.lastname)
        setTextToField(flat!!, user!!.apartmentNumber)
        setTextToField(phone!!, user!!.phone)
        setTextToField(house!!, user!!.houseNumber)
        setTextToField(street!!, user!!.streetName)
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

    private fun parseUserAnswer(payWithCreditCard: Boolean, obj: JsonObject) {
        LogUtil.logError(obj.toString())
        DeviceInfoStore.saveToken(this, obj.get("api_token").asString)
        if (AndroidUtilities.checkPlayServices(this)) {
            val intent = Intent(this@PersonalInfoActivity, MyInstanceIDListenerService::class.java)
            startService(intent)
        }
        sendToServer(payWithCreditCard)
    }

    private fun updateUser(payWithCard: Boolean) {
        val dialog  = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().updateUser(generateUserJson(), DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                sendToServer(payWithCard)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                sendToServer(payWithCard)
            }
        })
    }

    private fun createUser(payWithCard: Boolean) {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().createUser(generateUserJson()).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    parseUserAnswer(payWithCard, response.body())
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
        while (matcher.find()) {
            count++
        }

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
        private val EXTRA_PROMO_CODE_ID = "promoCodeId"
        var orderId: Int = 0
    }
}
