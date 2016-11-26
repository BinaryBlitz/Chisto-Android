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
import ru.binaryblitz.Chisto.Model.User
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import ru.binaryblitz.Chisto.Utils.Animations.Animations
import ru.binaryblitz.Chisto.Utils.OrderList
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

        for (order in orders!!.iterator()) {
            val treatments = order.treatments
            for (treatment in treatments!!.iterator()) {
                val local = JsonObject()
                local.addProperty("laundry_treatment_id", treatment.id)
                local.addProperty("quantity", order.count)
                array.add(local)
            }
        }

        return array
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

        return toSend
    }

    private fun sendToServer(bank: Boolean) {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().sendOrder(OrderList.getLaundryId(), generateJson()).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                if (response.isSuccessful) parseAnswer(response.body(), bank)
                else onInternetConnectionError()
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(obj: JsonObject, bank: Boolean) {
        orderId = obj.get("id").asInt
        if (bank) openWebActivity(obj.get("payment").asJsonObject.get("payment_url").asString)
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
            if (validateFields()) {
                setData()
                sendToServer(false)
            }
        }

        findViewById(R.id.bank_btn).setOnClickListener {
            if (validateFields()) {
                setData()
                sendToServer(true)
            }
        }

        findViewById(R.id.address_btn).setOnClickListener {
            startActivity(Intent(this@PersonalInfoActivity, MapActivity::class.java))
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
        phone!!.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    private fun setInfo() {
        user = DeviceInfoStore.getUserObject(this)

        setTextToField(city!!, user!!.city)

        if (user!!.name == null || user!!.name == "null") {
            setTextToField(phone!!, intent.getStringExtra(EXTRA_PHONE))
        } else {
            setTextToField(phone!!, user!!.phone)
        }

        setTextToField(name!!, user!!.name)
        setTextToField(lastname!!, user!!.lastname)
        setTextToField(flat!!, user!!.flat)
        setTextToField(phone!!, user!!.phone)
        setTextToField(house!!, user!!.house)
        setTextToField(street!!, user!!.street)
    }

    private fun setData() {
        if (user == null) user = User(1, null, null, null, null, null, null, null)

        user!!.name = name!!.text.toString()
        user!!.lastname = lastname!!.text.toString()
        user!!.city = city!!.text.toString()
        user!!.flat = flat!!.text.toString()
        user!!.phone = phone!!.text.toString()
        user!!.street = street!!.text.toString()
        user!!.house = house!!.text.toString()

        DeviceInfoStore.saveUser(this, user)
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

        return res
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
        var orderId: Int = 0
    }
}
