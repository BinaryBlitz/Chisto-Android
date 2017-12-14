package ru.binaryblitz.Chisto.ui.profile

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.JsonObject
import com.jakewharton.rxbinding2.widget.textChanges
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.rengwuxian.materialedittext.MaterialEditText
import io.fabric.sdk.android.Fabric
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function6
import kotlinx.android.synthetic.main.activity_profile_contact_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.User
import ru.binaryblitz.Chisto.extension.setCheckEditText
import ru.binaryblitz.Chisto.extension.toast
import ru.binaryblitz.Chisto.extension.visible
import ru.binaryblitz.Chisto.network.DeviceInfoStore
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.push.MyInstanceIDListenerService
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.map.MapActivity
import ru.binaryblitz.Chisto.ui.profile.PersonalInfoActivity.Companion.BLACK_COLOR
import ru.binaryblitz.Chisto.ui.profile.PersonalInfoActivity.Companion.CARD
import ru.binaryblitz.Chisto.ui.profile.PersonalInfoActivity.Companion.CASH
import ru.binaryblitz.Chisto.ui.profile.PersonalInfoActivity.Companion.GREY_COLOR
import ru.binaryblitz.Chisto.ui.start.SelectCityActivity
import ru.binaryblitz.Chisto.utils.AndroidUtilities
import java.util.regex.Pattern

typealias AllContactFields = Function6<Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean>

class ContactInfoActivity : BaseActivity() {

    private val phoneObservable by lazy {
        phoneEditText.textChanges()
                .map { numberPhoneMaskFilled }
                .doOnNext { phoneEditText.setCheckEditText(it) }
    }

    private val nameObservable by lazy {
        nameEditText.textChanges()
                .map { it.isNotBlank() }
                .doOnNext { nameEditText.setCheckEditText(it) }
    }

    private val cityObservable by lazy {
        cityEditText.textChanges()
                .map { it.isNotBlank() }
                .doOnNext { cityEditText.setCheckEditText(it) }
    }

    private val streetObservable by lazy {
        streetEditText.textChanges()
                .map { it.isNotBlank() }
                .doOnNext { streetEditText.setCheckEditText(it) }
    }

    private val houseObservable by lazy {
        houseEditText.textChanges()
                .map { it.isNotBlank() }
                .doOnNext { houseEditText.setCheckEditText(it) }
    }

    private val flatObservable by lazy {
        flatEditText.textChanges()
                .map { it.isNotBlank() }
                .doOnNext { flatEditText.setCheckEditText(it) }
    }

    private var disposable: Disposable? = null
    private var user: User? = null
    private var selectedPaymentType = CASH
    private var numberPhoneMaskFilled = false

    private val phoneMaskedTextChangedListener by lazy {
        MaskedTextChangedListener(
                format = RegistrationActivity.PHONE_MASK,
                autocomplete = true,
                field = phoneEditText,
                listener = null,
                valueListener = object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                        numberPhoneMaskFilled = maskFilled
                    }
                }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_profile_contact_info)

        setOnClickListeners()

        phoneEditText.addTextChangedListener(phoneMaskedTextChangedListener)

        disposable = Observable.combineLatest(
                phoneObservable,
                nameObservable,
                cityObservable,
                streetObservable,
                houseObservable,
                flatObservable,
                AllContactFields { phone, name, city, street, house, flat ->
                    phone && name && city && street && house && flat
                }
        ).subscribe({ saveButton.isEnabled = it })


        Handler().post { getUser() }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    private fun getUser() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this))
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        dialog.dismiss()
                        if (response.isSuccessful) {
                            parseUserResponse(response.body()!!)
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

        user.apply {
            id = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
            firstName = AndroidUtilities.getStringFieldFromJson(obj.get("first_name"))
            lastname = AndroidUtilities.getStringFieldFromJson(obj.get("last_name"))
            streetName = AndroidUtilities.getStringFieldFromJson(obj.get("street_name"))
            apartmentNumber = AndroidUtilities.getStringFieldFromJson(obj.get("house_number"))
            notes = AndroidUtilities.getStringFieldFromJson(obj.get("notes"))
            houseNumber = AndroidUtilities.getStringFieldFromJson(obj.get("apartment_number"))
            email = AndroidUtilities.getStringFieldFromJson(obj.get("email"))
        }

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
            finish()
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
        left_btn.setOnClickListener { onBackPressed() }
        bankLayout.setOnClickListener(payClickListener)
        moneyLayout.setOnClickListener(payClickListener)
        addressLayout.setOnClickListener {
            val intent = Intent(this@ContactInfoActivity, SelectCityActivity::class.java)
            intent.putExtra(EXTRA_CHECK_RESULT, GET_CITY_REQUEST)
            startActivityForResult(intent, GET_CITY_REQUEST)
        }
        street_btn.setOnClickListener {
            startActivity(Intent(this@ContactInfoActivity, MapActivity::class.java))
        }
        saveButton.setOnClickListener { setData() }
    }

    private fun showDialogIfNotLoggedIn() {
        MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(getString(R.string.registration_not_completed_error))
                .positiveText(R.string.yes_code)
                .negativeText(R.string.no_code)
                .onPositive { _, _ -> run { finish() } }
                .onNegative { dialog, _ -> run { dialog.dismiss() } }
                .show()
    }

    private fun selectBankCard() {
        bankTextView.setTextColor(Color.parseColor(BLACK_COLOR))
        moneyTextView.setTextColor(Color.parseColor(GREY_COLOR))

        visaImageView.setImageResource(R.drawable.ic_visa)
        masterCardImageView.setImageResource(R.drawable.ic_master_card)

        cardCheckBox.isChecked = true
        cashCheckBox.isChecked = false

        selectedPaymentType = CARD
        contactLayout.visible(true)
    }

    private val payClickListener: (View) -> Unit = { view ->
        if (contactLayout.visibility == View.GONE) {
            contactLayout.apply {
                visible(true)
                alpha = 0f
                animate().alpha(1.0f).duration = 350
            }
        }

        when (view.id) {
            bankLayout.id -> selectBankCard()
            moneyLayout.id -> selectCash()
        }
    }

    private fun selectCash() {
        moneyTextView.setTextColor(Color.parseColor(BLACK_COLOR))
        bankTextView.setTextColor(Color.parseColor(GREY_COLOR))

        visaImageView.setImageResource(R.drawable.ic_visa_no_active)
        masterCardImageView.setImageResource(R.drawable.ic_master_card_no_active)

        cashCheckBox.isChecked = true
        cardCheckBox.isChecked = false

        selectedPaymentType = CASH
    }

    private fun generateUserJson(): JsonObject {
        val obj = JsonObject().apply {
            addProperty("first_name", nameEditText.text.toString())
            addProperty("payment_method", selectedPaymentType)
            addProperty("apartment_number", flatEditText.text.toString())
            addProperty("house_number", houseEditText.text.toString())
            addProperty("street_name", streetEditText.text.toString())
            addProperty("notes", commentEditText.text.toString())
            addProperty("phone_number", AndroidUtilities.processText(phoneEditText))
            addProperty("city_id", DeviceInfoStore.getCityObject(this@ContactInfoActivity).id)
            addProperty("device_token", FirebaseInstanceId.getInstance().token)
            addProperty("platform", "android")
        }

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
                    toast(getString(R.string.changes_saved))
                    parseUserAnswer(response.body()!!)
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
        DeviceInfoStore.getUserObject(this)?.run {
            setTextToField(cityEditText, DeviceInfoStore.getCityObject(this@ContactInfoActivity).name)
            setTextToField(nameEditText, firstName)
            setTextToField(flatEditText, apartmentNumber)
            setTextToField(phoneEditText, phone)
            setTextToField(houseEditText, houseNumber)
            setTextToField(streetEditText, streetName)
            setTextToField(phoneEditText, phone)
            setTextToField(commentEditText, notes)
        } ?: setTextToField(phoneEditText, intent.getStringExtra(EXTRA_PHONE))
    }

    private fun setData() {
        if (user == null) user = User.createDefault()

        user?.apply {
            firstName = nameTextView.text.toString()
            city = cityEditText.text.toString()
            apartmentNumber = flatEditText.text.toString()
            phone = phoneEditText.text.toString()
            streetName = streetEditText.text.toString()
            houseNumber = houseEditText.toString()
            notes = commentEditText.text.toString()
        }

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
                toast(getString(R.string.changes_saved))
                finish()
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                toast(getString(R.string.changes_saved))
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
        var res = validateField(nameEditText, true)
        res = res and validateField(cityEditText, true)
        res = res and validateField(streetEditText, false)
        res = res and validateField(houseEditText, false)
        res = res and validateField(flatEditText, false)
        res = res and validatePhoneField(phoneEditText)

        return res
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

        while (matcher.find()) {
            count++
        }

        return count
    }

    companion object {
        private const val EXTRA_TOKEN = "token"
        private const val EXTRA_PHONE = "phone"
        const val GET_CITY_REQUEST = 1
        const val EXTRA_CHECK_RESULT = "extra_check_result"
    }
}
