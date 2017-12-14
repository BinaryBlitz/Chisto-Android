package ru.binaryblitz.Chisto.ui.profile

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.gson.JsonObject
import com.nineoldandroids.animation.Animator
import com.redmadrobot.inputmask.MaskedTextChangedListener
import com.redmadrobot.inputmask.MaskedTextChangedListener.ValueListener
import kotlinx.android.synthetic.main.activity_registration.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.extension.toast
import ru.binaryblitz.Chisto.extension.visible
import ru.binaryblitz.Chisto.network.DeviceInfoStore
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.order.MyOrdersActivity
import ru.binaryblitz.Chisto.ui.order.WebActivity
import ru.binaryblitz.Chisto.utils.AndroidUtilities
import ru.binaryblitz.Chisto.utils.AnimationStartListener
import ru.binaryblitz.Chisto.utils.AppConfig

class RegistrationActivity : BaseActivity() {

    private val phoneMaskedTextChangedListener by lazy {
        MaskedTextChangedListener(
                format = PHONE_MASK,
                autocomplete = true,
                field = phoneEditText,
                listener = null,
                valueListener = object : ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                        continueButton.isEnabled = maskFilled
                    }
                }
        )
    }

    private val codeMaskedTextChangedListener by lazy {
        MaskedTextChangedListener(
                format = CODE_MASK,
                autocomplete = true,
                field = codeEditText,
                listener = null,
                valueListener = object : ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                        if (maskFilled) {
                            verifyRequest()
                        }
                    }
                }
        )
    }

    private var code = false
    private var price = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        phoneEditText.addTextChangedListener(phoneMaskedTextChangedListener)
        codeEditText.addTextChangedListener(codeMaskedTextChangedListener)

        setOnClickListeners()

        price = intent.getIntExtra(EXTRA_PRICE, 0)

        Handler().post { phoneEditText.requestFocus() }
    }

    override fun onBackPressed() {
        if (!code)
            super.onBackPressed()
        else {
            animateBackBtn()
            resetFields()
        }
    }

    private fun animateBackBtn() {
        YoYo.with(Techniques.SlideOutRight)
                .duration(ANIMATION_DURATION.toLong())
                .withListener(object : AnimationStartListener() {
                    override fun onStart() {
                        phoneEditText.visible(true)

                        YoYo.with(Techniques.SlideInLeft)
                                .duration(ANIMATION_DURATION.toLong())
                                .playOn(phoneEditText)

                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(phoneEditText, InputMethodManager.SHOW_IMPLICIT)
                    }
                })
                .playOn(codeEditText)
    }

    private fun resetFields() {
        codeEditText.setText("")
        continueButton.visible(true)
        labelSendCodeTextView.text = getString(R.string.code_send)
        titleTextView.text = getString(R.string.type_phone)
        code = false
    }

    private fun processPhoneInput() {
        val phoneText = phoneEditText.text.toString()

        if (phoneText.isEmpty()) {
            phoneEditText.error = getString(R.string.empty_field)
            return
        }

        authRequest(true)
    }

    private fun setOnClickListeners() {
        continueButton.setOnClickListener { processPhoneInput() }

        browse.setOnClickListener {
            val intent = Intent(this@RegistrationActivity, WebActivity::class.java)
            intent.putExtra("url", AppConfig.terms)
            startActivity(intent)
        }

        left_btn.setOnClickListener {
            if (!code)
                super.onBackPressed()
            else {
                animateBackBtn()
                resetFields()
            }
        }
    }

    private fun checkCodeInput(): Boolean {
        if (codeEditText.text.toString().isEmpty() || codeEditText.text.toString().length != 5) {
            codeEditText.error = getString(R.string.wrong_code)
            return false
        }

        return true
    }

    private fun verifyRequest() {
        if (!checkCodeInput()) {
            return
        }
        executeVerifyRequest()
    }

    private fun saveInfo(obj: JsonObject) {
        val phone = phoneEditText.text.toString()
        savePhone(phone)
        saveToken(obj)
        finishActivity(phone)
    }

    private fun saveToken(obj: JsonObject) {
        val token = obj.get("api_token")

        if (!token.isJsonNull) {
            DeviceInfoStore.saveToken(this, token.asString)
        }
    }

    private fun continueRegistration(phone: String) {
        if (DeviceInfoStore.getToken(this) == "null") {
            openContactInfo(phone)
        } else {
            openSelectedActivity(phone)
        }
    }

    private fun openSelectedActivity(phone: String) {
        val choice = intent.getIntExtra(EXTRA_SELECTED, 0)

        if (choice == SELECTED_CONTACT_INFO_ACTIVITY) {
            openContactInfo(phone)
        } else {
            openMyOrders()
        }
    }

    private fun finishActivity(phone: String) {
        if (price == 0) {
            continueRegistration(phone)
        } else {
            openOrderScreen(phone)
        }
    }

    private fun openOrderScreen(phone: String) {
        val intent = Intent(this@RegistrationActivity, PersonalInfoActivity::class.java)
        intent.putExtra(EXTRA_PHONE, phone)
        intent.putExtra(EXTRA_PRICE, price)
        intent.putExtra(EXTRA_TOKEN, token)
        intent.putExtra(EXTRA_PROMO_CODE_ID, intent.getIntExtra(EXTRA_PROMO_CODE_ID, 0))
        startActivity(intent)
        finish()
    }

    private fun openMyOrders() {
        val intent = Intent(this@RegistrationActivity, MyOrdersActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openContactInfo(phone: String) {
        val intent = Intent(this@RegistrationActivity, ContactInfoActivity::class.java)
        intent.putExtra(EXTRA_PHONE, phone)
        intent.putExtra(EXTRA_TOKEN, token)
        startActivity(intent)
        finish()
    }

    private fun executeVerifyRequest() {
        val dialog = ProgressDialog(this@RegistrationActivity)
        dialog.show()

        ServerApi.get(this@RegistrationActivity).api().verifyPhoneNumber(generateVerifyJson()).enqueue(
                object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        dialog.dismiss()

                        if (response.isSuccessful) {
                            parseVerifyAnswer(response.body()!!)
                        } else {
                            toast(getString(R.string.wrong_code))
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        dialog.dismiss()
                        onInternetConnectionError()
                    }
                })
    }

    private fun generateVerifyJson(): JsonObject {
        val obj = JsonObject()
        obj.addProperty("token", token)
        obj.addProperty("code", codeEditText.text.toString())

        val toSend = JsonObject()
        toSend.add("verification_token", obj)

        return toSend
    }

    private fun parseVerifyAnswer(obj: JsonObject) {
        saveInfo(obj)
    }

    private fun savePhone(phone: String) {
        val user = DeviceInfoStore.getUserObject(this) ?: return
        user.phone = phone
        DeviceInfoStore.saveUser(this, user)
    }

    private fun authRequest(animate: Boolean) {
        if (!AndroidUtilities.validatePhone(phoneEditText!!.text.toString())) {
            toast(getString(R.string.wrong_phone))
            return
        }

        val dialog = ProgressDialog(this@RegistrationActivity)
        dialog.show()

        ServerApi.get(this@RegistrationActivity).api().authWithPhoneNumber(processText()).enqueue(
                object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        dialog.dismiss()
                        if (response.isSuccessful && response.body() != null) {
                            parseAuthRequestAnswer(response.body()!!)

                            if (animate) {
                                playOutAnimation(phoneEditText, textView2)
                            }
                        } else {
                            onServerError(response)
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        dialog.dismiss()
                        onInternetConnectionError()
                    }
                }
        )
    }

    private fun parseAuthRequestAnswer(obj: JsonObject) {
        code = true
        token = obj.get("token").asString
        phoneFromServer = obj.get("phone_number").asString
        continueButton.visible(false)
    }

    private fun processText(): JsonObject {
        var phoneNext = phoneEditText!!.text.toString()
        phoneNext = phoneNext.replace("(", "")
        phoneNext = phoneNext.replace(")", "")
        phoneNext = phoneNext.replace("-", "")
        phoneNext = phoneNext.replace(" ", "")

        val obj = JsonObject()
        obj.addProperty("phone_number", phoneNext)

        val toSend = JsonObject()
        toSend.add("verification_token", obj)

        return toSend
    }

    private fun playOutAnimation(v1: View, v2: View) {
        YoYo.with(Techniques.SlideOutLeft)
                .duration(ANIMATION_DURATION.toLong())
                .withListener(object : AnimationStartListener() {
                    override fun onStart() {
                        codeEditText.visible(true)
                        YoYo.with(Techniques.SlideInRight)
                                .duration(ANIMATION_DURATION.toLong())
                                .playOn(codeEditText)
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        codeEditText.requestFocus()
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    }
                })
                .playOn(v1)

        YoYo.with(Techniques.SlideOutLeft)
                .duration(ANIMATION_DURATION.toLong())
                .withListener(object : AnimationStartListener() {
                    override fun onStart() {
                        labelSendCodeTextView.text = getString(R.string.number_code) + " " +
                                phoneEditText.text.toString() + getString(R.string.code_sent)
                        titleTextView.text = getString(R.string.code_title)
                    }
                })
                .playOn(v2)
    }

    companion object {
        private val ANIMATION_DURATION = 700
        private var token: String? = ""
        private val EXTRA_PRICE = "price"
        private val EXTRA_TOKEN = "token"
        private val EXTRA_PROMO_CODE_ID = "promoCodeId"
        private var phoneFromServer: String? = null
        const val PHONE_MASK = "+[0] [000] [000]-[00]-[00]"
        private const val CODE_MASK = "[00000]"
        const val EXTRA_PHONE = "phone"
        const val EXTRA_SELECTED = "selected"
        const val SELECTED_CONTACT_INFO_ACTIVITY = 2
    }
}
