package ru.binaryblitz.Chisto.Utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ImageSpan
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.JsonElement
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.io.ByteArrayOutputStream
import java.io.File

@SuppressWarnings("unused")
object AndroidUtilities {

    private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

    fun hideKeyboard(v: View) {
        val imm = v.context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    fun dpToPx(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun colorAndroidBar(context: Activity, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = context.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = color
        }
    }

    fun validatePhone(phNumber: String): Boolean {
        if (phNumber.isEmpty()) return false

        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        val isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt("+7"))
        var phoneNumber: Phonenumber.PhoneNumber? = null
        try {
            phoneNumber = phoneNumberUtil.parse(phNumber, isoCode)
        } catch (e: NumberParseException) {
            LogUtil.logException(e)
        }

        return phoneNumberUtil.isValidNumber(phoneNumber)
    }

    fun validateEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    fun call(context: Context, phone: String) {
        val call = Uri.parse("tel:" + phone)
        val intent = Intent(Intent.ACTION_DIAL, call)
        context.startActivity(intent)
    }

    fun getStringFieldFromJson(element: JsonElement?): String {
        if (element == null || element.isJsonNull) return ""
        else return element.asString
    }

    fun getIntFieldFromJson(element: JsonElement?): Int {
        if (element == null || element.isJsonNull) return 0
        else return element.asInt
    }

    fun getDoubleFieldFromJson(element: JsonElement?): Double {
        if (element == null || element.isJsonNull) return 0.0
        else return element.asDouble
    }

    fun getBooleanFieldFromJson(element: JsonElement?): Boolean {
        if (element == null || element.isJsonNull) return false
        else return element.asBoolean
    }

    fun encodeToBase64(image: Bitmap): String {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    fun checkPlayServices(context: Activity): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(context)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(context, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show()
            } else {
                context.finish()
            }
            return false
        }
        return true
    }

    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null)
            return null
        else if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight

        if (!(intrinsicWidth > 0 && intrinsicHeight > 0))
            return null

        try {
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } catch (e: OutOfMemoryError) {
            return null
        }

    }

    fun convertDpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / 160f)
    }

    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    fun getTabTitleFromDrawable(context: Context, resId: Int): CharSequence {

        val image = ContextCompat.getDrawable(context, resId)

        image.setBounds(0, 0, convertDpToPixel(24f, context).toInt(), convertDpToPixel(24f, context).toInt())
        val sb = SpannableString("  ")
        val imageSpan = ImageSpan(image, ImageSpan.ALIGN_BOTTOM)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return sb
    }

    fun setupUIForAutoKeyBoardHide(view: View) {
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                hideKeyboard(v)
                false
            }
        }

        if (view is ViewGroup) {
            (0..view.childCount - 1)
                    .map { view.getChildAt(it) }
                    .forEach { setupUIForAutoKeyBoardHide(it) }
        }
    }

    fun deleteDirectoryTree(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            for (child in fileOrDirectory.listFiles()) {
                deleteDirectoryTree(child)
            }
        }

        fileOrDirectory.delete()
    }
}
