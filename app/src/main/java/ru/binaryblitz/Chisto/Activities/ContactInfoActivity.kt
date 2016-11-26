package ru.binaryblitz.Chisto.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import com.crashlytics.android.Crashlytics
import com.rengwuxian.materialedittext.MaterialEditText
import io.fabric.sdk.android.Fabric
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Model.User
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import java.util.regex.Pattern
import ru.binaryblitz.Chisto.R.id.editText
import android.telephony.PhoneNumberFormattingTextWatcher
import com.afollestad.materialdialogs.MaterialDialog
import ru.binaryblitz.Chisto.Model.CategoryItem
import ru.binaryblitz.Chisto.Utils.AndroidUtilities


class ContactInfoActivity : BaseActivity() {
    private var name: MaterialEditText? = null
    private var lastname: MaterialEditText? = null
    private var city: MaterialEditText? = null
    private var street: MaterialEditText? = null
    private var house: MaterialEditText? = null
    private var flat: MaterialEditText? = null
    private var comment: MaterialEditText? = null
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
        if(!validateFields()) showDialog()
        else setData()
    }

    override fun onResume() {
        super.onResume()
        setInfo()
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener {
            if(!validateFields()) showDialog()
            else setData()
        }

        findViewById(R.id.address_btn).setOnClickListener {
            startActivity(Intent(this@ContactInfoActivity, MapActivity::class.java))
        }
    }

    private fun showDialog() {
        MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(getString(R.string.profile_wrong_fields))
                .positiveText(R.string.yes_code)
                .negativeText(R.string.no_code)
                .onPositive { dialog, action ->
                    run { finish() }
                }
                .onNegative { dialog, action ->
                    run { dialog.dismiss() }
                }
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

        phone!!.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    private fun setInfo() {
        user = DeviceInfoStore.getUserObject(this)

        setTextToField(city!!, user!!.city)
        setTextToField(name!!, user!!.name)
        setTextToField(lastname!!, user!!.lastname)
        setTextToField(flat!!, user!!.flat)
        setTextToField(phone!!, user!!.phone)
        setTextToField(house!!, user!!.house)
        setTextToField(street!!, user!!.street)
        setTextToField(phone!!, user!!.phone)
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

        finish()
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
}
