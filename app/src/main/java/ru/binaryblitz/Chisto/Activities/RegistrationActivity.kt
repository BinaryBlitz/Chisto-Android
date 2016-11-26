package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
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


class RegistrationActivity : BaseActivity() {

    val EXTRA_PHONE = "phone"

    private var code = false

    private var phoneEditText: MaterialEditText? = null
    private var codeEditText: MaterialEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        setOnClickListeners()

        phoneEditText = findViewById(R.id.phone) as MaterialEditText
        codeEditText = findViewById(R.id.code_field) as MaterialEditText
        phoneEditText!!.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        Handler().post {
            phoneEditText!!.requestFocus()
        }
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

        findViewById(R.id.button).setOnClickListener { v ->
            AndroidUtilities.hideKeyboard(v)
            if(code) verifyRequest() else processPhoneInput()
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

        val phone = phoneEditText!!.text.toString()
        savePhone(phone)

        val intent = Intent(this@RegistrationActivity, PersonalInfoActivity::class.java)
        intent.putExtra(EXTRA_PHONE, phone)
        startActivity(intent)
        finish()
    }

    private fun savePhone(phone: String) {
        val user = DeviceInfoStore.getUserObject(this)
        user.phone = phone
        DeviceInfoStore.saveUser(this, user)
    }

    private fun authRequest(animate: Boolean) {
        if(!AndroidUtilities.validatePhone(phoneEditText!!.text.toString())) {
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
                            onInternetConnectionError()
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
    }

    private fun processText(): String {
        var phoneNext = phoneEditText!!.text.toString()
        phoneNext = phoneNext.replace("(", "")
        phoneNext = phoneNext.replace(")", "")
        phoneNext = phoneNext.replace("-", "")
        phoneNext = phoneNext.replace(" ", "")
        return phoneNext
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
        private var phoneFromServer: String? = null
    }
}

