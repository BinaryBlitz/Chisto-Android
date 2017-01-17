package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.gson.JsonObject
import com.nineoldandroids.animation.Animator
import com.rengwuxian.materialedittext.MaterialEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import ru.binaryblitz.Chisto.Utils.AnimationStartListener
import ru.binaryblitz.Chisto.Utils.AppConfig
import ru.binaryblitz.Chisto.Utils.CustomPhoneNumberTextWatcher

class RegistrationActivity : BaseActivity() {

    val EXTRA_PHONE = "phone"

    private var code = false
    private var cost = 0
    private var phoneEditText: MaterialEditText? = null
    private var codeEditText: MaterialEditText? = null
    private var continueButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        initElements()
        setOnClickListeners()

        cost = intent.getIntExtra(EXTRA_PRICE, 0)

        Handler().post { phoneEditText!!.requestFocus() }
    }

    override fun onBackPressed() {
        if (!code)
            super.onBackPressed()
        else {
            animateBackBtn()
            resetFields()
        }
    }

    private fun initElements() {
        continueButton = findViewById(R.id.button) as Button
        phoneEditText = findViewById(R.id.phone) as MaterialEditText
        codeEditText = findViewById(R.id.code_field) as MaterialEditText
        phoneEditText!!.addTextChangedListener(CustomPhoneNumberTextWatcher())

        codeEditText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0!!.length == 5) verifyRequest()
            }
        })
    }

    private fun animateBackBtn() {
        YoYo.with(Techniques.SlideOutRight)
                .duration(ANIMATION_DURATION.toLong())
                .withListener(object : AnimationStartListener() {
                    override fun onStart() {
                        findViewById(R.id.l1).visibility = View.VISIBLE

                        YoYo.with(Techniques.SlideInLeft)
                                .duration(ANIMATION_DURATION.toLong())
                                .playOn(findViewById(R.id.l1))

                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(phoneEditText, InputMethodManager.SHOW_IMPLICIT)
                    }
                })
                .playOn(findViewById(R.id.l2))
    }

    private fun resetFields() {
        codeEditText!!.setText("")
        continueButton!!.visibility = View.VISIBLE
        (findViewById(R.id.textView23) as TextView).text = getString(R.string.code_send)
        (findViewById(R.id.title_text) as TextView).text = getString(R.string.type_phone)
        code = false
    }

    private fun processPhoneInput() {
        val phoneText = phoneEditText!!.text.toString()
        if (phoneText.isEmpty()) {
            phoneEditText!!.error = getString(R.string.empty_field)
            return
        }

        authRequest(true)
    }

    private fun setOnClickListeners() {
        continueButton!!.setOnClickListener { v ->
            AndroidUtilities.hideKeyboard(v)
            processPhoneInput()
        }

        findViewById(R.id.browse).setOnClickListener {
            val intent = Intent(this@RegistrationActivity, WebActivity::class.java)
            intent.putExtra("url", AppConfig.terms)
            startActivity(intent)
        }

        findViewById(R.id.left_btn).setOnClickListener {
            if (!code)
                super.onBackPressed()
            else {
                animateBackBtn()
                resetFields()
            }
        }
    }

    private fun checkCodeInput(): Boolean {
        if (codeEditText!!.text.toString().isEmpty() || codeEditText!!.text.toString().length != 5) {
            codeEditText!!.error = getString(R.string.wrong_code)
            return false
        }

        return true
    }

    private fun verifyRequest() {
        if (!checkCodeInput()) return
        executeVerifyRequest()
    }

    private fun saveInfo(obj: JsonObject) {
        val phone = phoneEditText!!.text.toString()
        savePhone(phone)
        saveToken(obj)
        finishActivity(phone)
    }

    private fun saveToken(obj: JsonObject) {
        val token = obj.get("api_token")
        if (!token.isJsonNull) DeviceInfoStore.saveToken(this, token.asString)
    }

    private fun finishActivity(phone: String) {
        if (cost == 0) openProfileScreen()
        else openOrderScreen(phone)
    }

    private fun openOrderScreen(phone: String) {
        val intent = Intent(this@RegistrationActivity, PersonalInfoActivity::class.java)
        intent.putExtra(EXTRA_PHONE, phone)
        intent.putExtra(EXTRA_PRICE, cost)
        intent.putExtra(EXTRA_TOKEN, token)
        startActivity(intent)
        finish()
    }

    private fun openProfileScreen() {
        val intent = Intent(this@RegistrationActivity, ProfileActivity::class.java)
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
                        if (!response.isSuccessful) showCodeError()
                        else parseVerifyAnswer(response.body())
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
        obj.addProperty("code", codeEditText!!.text.toString())

        val toSend = JsonObject()
        toSend.add("verification_token", obj)

        return toSend
    }

    private fun showCodeError() {
        Snackbar.make(findViewById(R.id.main), R.string.wrong_code, Snackbar.LENGTH_SHORT).show()
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
            Snackbar.make(findViewById(R.id.main), getString(R.string.wrong_phone), Snackbar.LENGTH_SHORT).show()
            return
        }

        val dialog = ProgressDialog(this@RegistrationActivity)
        dialog.show()

        ServerApi.get(this@RegistrationActivity).api().authWithPhoneNumber(processText()).enqueue(
                object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        dialog.dismiss()
                        if (response.isSuccessful && response.body() != null) {
                            parseAuthRequestAnswer(response.body())
                            if (animate) {
                                playOutAnimation(findViewById(R.id.l1), findViewById(R.id.textView2))
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
        continueButton!!.visibility = View.GONE
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
                        findViewById(R.id.l2).visibility = View.VISIBLE
                        YoYo.with(Techniques.SlideInRight)
                                .duration(ANIMATION_DURATION.toLong())
                                .playOn(findViewById(R.id.l2))
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        codeEditText!!.requestFocus()
                        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    }
                })
                .playOn(v1)

        YoYo.with(Techniques.SlideOutLeft)
                .duration(ANIMATION_DURATION.toLong())
                .withListener(object : AnimationStartListener() {
                    override fun onStart() {
                        (findViewById(R.id.textView23) as TextView).text =
                                getString(R.string.number_code) + " " + phoneEditText!!.text.toString() + getString(R.string.code_sent)

                        (findViewById(R.id.title_text) as TextView).text = getString(R.string.code_title)
                    }
                })
                .playOn(v2)
    }

    companion object {
        private val ANIMATION_DURATION = 700
        private var token: String? = null
        private val EXTRA_PRICE = "price"
        private val EXTRA_TOKEN = "token"
        private var phoneFromServer: String? = null
    }
}
