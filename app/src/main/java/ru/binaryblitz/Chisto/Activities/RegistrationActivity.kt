package ru.binaryblitz.Chisto.Activities

import com.google.gson.JsonObject

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView

import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
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
import ru.binaryblitz.Chisto.Utils.CodeTimer
import ru.binaryblitz.Chisto.Utils.LogUtil

class RegistrationActivity : BaseActivity() {

    private var code = false

    private var messageTextView: TextView? = null
    private var phoneEditText: MaterialEditText? = null
    private var codeEditText: MaterialEditText? = null
    private var countyCodeEditText: MaterialEditText? = null

    private val myRunnable = Runnable {
        messageForUser = getString(R.string.send_code_after_str) + (milis.toDouble() / SECOND.toDouble()).toInt()
        if (milis < 2 * SECOND) messageForUser = REPEAT_STR
    }

    private val watcher = object : PhoneNumberFormattingTextWatcher() {
        private var backspacingFlag = false
        private var editedFlag = false
        private var cursorComplement: Int = 0

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            cursorComplement = s.length - phoneEditText!!.selectionStart
            backspacingFlag = count > after
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable) {
            val string = s.toString()
            val phone = string.replace("[^\\d]".toRegex(), "")

            if (!editedFlag) {
                if (phone.length >= 8 && !backspacingFlag) {
                    editedFlag = true
                    val ans = "(" + phone.substring(0, 3) + ") " + phone.substring(3, 6) + "-" +
                            phone.substring(6, 8) + "-" + phone.substring(8)
                    this@RegistrationActivity.phoneEditText!!.setText(ans)
                    this@RegistrationActivity.phoneEditText!!.setSelection(
                            this@RegistrationActivity.phoneEditText!!.text.length - cursorComplement)
                } else if (phone.length >= 3 && !backspacingFlag) {
                    editedFlag = true
                    val ans = "(" + phone.substring(0, 3) + ") " + phone.substring(3)
                    this@RegistrationActivity.phoneEditText!!.setText(ans)
                    this@RegistrationActivity.phoneEditText!!.setSelection(
                            this@RegistrationActivity.phoneEditText!!.text.length - cursorComplement)
                }
            } else {
                editedFlag = false
            }
        }
    }

    private fun UpdateGUI() {
        Handler().post(myRunnable)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        setOnClickListeners()

        messageTextView = findViewById(R.id.textView37) as TextView
        messageTextView!!.visibility = View.GONE

        initTimer()

        phoneEditText = findViewById(R.id.phone) as MaterialEditText
        codeEditText = findViewById(R.id.phon2e) as MaterialEditText
        countyCodeEditText = findViewById(R.id.code) as MaterialEditText
        phoneEditText!!.addTextChangedListener(watcher)
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
                    }
                })
                .playOn(findViewById(R.id.l2))

        YoYo.with(Techniques.SlideOutRight)
                .duration(ANIMATION_DURATION.toLong())
                .withListener(object : AnimationStartListener() {
                    override fun onStart() {
                        findViewById(R.id.textView2).visibility = View.VISIBLE

                        YoYo.with(Techniques.SlideInLeft)
                                .duration(ANIMATION_DURATION.toLong())
                                .playOn(findViewById(R.id.textView2))
                    }
                })
                .playOn(findViewById(R.id.textView23))
    }

    private fun resetFields() {
        codeEditText!!.setText("")
        messageTextView!!.visibility = View.GONE
        code = false
    }

    private fun processPhoneInput() {
        val phoneText = phoneEditText!!.text.toString()
        if (phoneText.isEmpty()) {
            phoneEditText!!.error = getString(R.string.empty_field_str)
            return
        }

        authRequest(true)
    }

    private fun setOnClickListeners() {

        findViewById(R.id.button).setOnClickListener { v ->
            AndroidUtilities.hideKeyboard(v)
            if (!code)
                processPhoneInput()
            else
                verifyRequest()
        }

        findViewById(R.id.textView37).setOnClickListener(View.OnClickListener {
            if (!AndroidUtilities.isConnected(this@RegistrationActivity)) {
                onInternetConnectionError()
                return@OnClickListener
            }
            Handler().postDelayed({
                authRequest(false)
                startTimer()
            }, 50)
        })
    }

    private fun checkCodeInput(): Boolean {
        if (codeEditText!!.text.toString().isEmpty()) {
            codeEditText!!.error = getString(R.string.empty_field_str)
            return false
        }

        return true
    }

    private fun verifyRequest() {
        if (!checkCodeInput()) return

        val dialog = ProgressDialog(this@RegistrationActivity)
        dialog.show()

        ServerApi.get(this@RegistrationActivity).api().verifyPhoneNumber(token, codeEditText!!.text.toString(), phoneFromServer)
                .enqueue(object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        dialog.dismiss()
                        if (!response.isSuccessful || response.body() == null) {
                            Snackbar.make(findViewById(R.id.main), R.string.wrong_code_str, Snackbar.LENGTH_SHORT).show()
                        } else {
                            val `object` = response.body()
                            DeviceInfoStore.saveToken(this@RegistrationActivity, `object`.get("api_token").asString)
                        }
                    }

                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        dialog.dismiss()
                        onInternetConnectionError()
                    }
                })
    }

    private fun authRequest(animate: Boolean) {
        val dialog = ProgressDialog(this@RegistrationActivity)
        dialog.show()

        ServerApi.get(this@RegistrationActivity).api().authWithPhoneNumber(processText()).enqueue(
                object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        dialog.dismiss()
                        LogUtil.logError(response.body().toString())
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

    private fun parseAuthRequestAnswer(`object`: JsonObject) {
        code = true
        messageTextView!!.visibility = View.VISIBLE
        token = `object`.get("token").asString
        phoneFromServer = `object`.get("phone_number").asString
    }

    private fun processText(): String {
        var phone = countyCodeEditText!!.text.toString()
        var phoneNext = phoneEditText!!.text.toString()
        phoneNext = phoneNext.replace("(", "")
        phoneNext = phoneNext.replace(")", "")
        phoneNext = phoneNext.replace("-", "")
        phoneNext = phoneNext.replace(" ", "")
        phone += phoneNext
        LogUtil.logError(phone)
        return phone
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
                })
                .playOn(v1)

        YoYo.with(Techniques.SlideOutLeft)
                .duration(ANIMATION_DURATION.toLong())
                .withListener(object : AnimationStartListener() {
                    override fun onStart() {
                        findViewById(R.id.textView23).visibility = View.VISIBLE

                        YoYo.with(Techniques.SlideInRight)
                                .duration(ANIMATION_DURATION.toLong())
                                .playOn(findViewById(R.id.textView23))
                    }
                })
                .playOn(v2)
    }

    private fun startTimer() {
        CodeTimer.reset()
        CodeTimer.with(object : CodeTimer.OnTimer {
            override fun onTick(millisUntilFinished: Long) {
                milis = millisUntilFinished
                UpdateGUI()
            }

            override fun onFinish() {
            }
        })

        CodeTimer.start()
    }

    private fun initTimer() {
        val t = object : Thread() {
            override fun run() {
                try {
                    while (!isInterrupted) {
                        Thread.sleep(1000)

                        runOnUiThread {
                            updateMessageText()
                            if (milis < 2000)
                                messageTextView!!.isClickable = true
                            else
                                messageTextView!!.isClickable = false
                        }
                    }
                } catch (ignored: InterruptedException) {
                }

            }
        }

        t.start()
    }

    private fun updateMessageText() {
        if (messageForUser == REPEAT_STR) {
            val content = SpannableString(getString(R.string.send_again_str))
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            messageTextView!!.text = content
        } else {
            messageTextView!!.text = messageForUser
        }
    }

    companion object {

        private val REPEAT_STR = "repeat"
        private val ANIMATION_DURATION = 700
        private val SECOND = 1000
        private var token: String? = null
        private var phoneFromServer: String? = null

        private var milis: Long = 0
        private var messageForUser: String? = null
    }
}

