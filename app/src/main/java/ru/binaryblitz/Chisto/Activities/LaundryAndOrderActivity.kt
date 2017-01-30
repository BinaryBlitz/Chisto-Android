package ru.binaryblitz.Chisto.Activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Pair
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonObject
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Adapters.OrderContentAdapter
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Custom.RecyclerListView
import ru.binaryblitz.Chisto.Model.Order
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Server.ServerConfig
import ru.binaryblitz.Chisto.Utils.*
import java.util.*

class LaundryAndOrderActivity : BaseActivity() {
    private var adapter: OrderContentAdapter? = null
    private var deliveryFee = 0
    private var dialogOpened = false
    private var promoCodeId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_laundry_and_order)

        initElements()
        setOnClickListeners()

        load()
    }

    private fun showPromoDialog() {
        Handler().post {
            dialogOpened = true
            Animations.animateRevealShow(findViewById(ru.binaryblitz.Chisto.R.id.dialog), this@LaundryAndOrderActivity)
        }
    }

    private fun parsePromo(obj: JsonObject) {
        hidePromoBtn()
        parsePromoInformationFromJson(obj)
        closeDialog()
    }

    private fun hidePromoBtn() {
        findViewById(R.id.add_btn).visibility = View.GONE
        findViewById(R.id.promo_discount).visibility = View.VISIBLE
    }

    private fun parsePromoInformationFromJson(obj: JsonObject) {
        promoCodeId = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        (findViewById(R.id.promo_discount) as TextView).text =
                getString(R.string.minus_sign) +
                AndroidUtilities.getStringFieldFromJson(obj.get("discount")) +
                getString(R.string.ruble_sign)
    }

    private fun showPromoError() {
        (findViewById(R.id.promo_help_text) as TextView).text = getString(R.string.promo_error)
    }

    private fun getPromo() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().getPromoCode((findViewById(R.id.promo_text) as EditText).text.toString(), DeviceInfoStore.getToken(this))
                .enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                AndroidUtilities.hideKeyboard(findViewById(R.id.main))
                if (response.isSuccessful) {
                    parsePromo(response.body())
                } else {
                    showPromoError()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    private fun closeDialog() {
        dialogOpened = false
        Animations.animateRevealHide(findViewById(ru.binaryblitz.Chisto.R.id.dialog))
    }

    override fun onBackPressed() {
        if (dialogOpened) {
            closeDialog()
        } else {
            finish()
        }
    }

    private fun checkPromo(): Boolean {
        if ((findViewById(R.id.promo_text) as EditText).text.toString().isEmpty()) {
            findViewById(R.id.promo_btn)!!.isEnabled = false
            return false
        }

        findViewById(R.id.promo_btn)!!.isEnabled = true
        return true
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener { finish() }

        findViewById(R.id.cont_btn).setOnClickListener {
            if (DeviceInfoStore.getToken(this@LaundryAndOrderActivity) == "null") {
                openActivity(RegistrationActivity::class.java)
            } else {
                openActivity(PersonalInfoActivity::class.java)
            }
        }

        findViewById(R.id.promo_btn).setOnClickListener {
            if (checkPromo()) {
                getPromo()
            }
        }


        findViewById(R.id.add_btn).setOnClickListener {
            showPromoDialog()
        }

        findViewById(R.id.reviews_btn).setOnClickListener {
            val intent = Intent(this@LaundryAndOrderActivity, ReviewsActivity::class.java)
            intent.putExtra(EXTRA_ID, getIntent().getIntExtra(EXTRA_ID, 1))
            startActivity(intent)
        }

        findViewById(R.id.ratingBarBtn).setOnClickListener {
            val intent = Intent(this@LaundryAndOrderActivity, ReviewsActivity::class.java)
            intent.putExtra(EXTRA_ID, getIntent().getIntExtra(EXTRA_ID, 1))
            startActivity(intent)
        }
    }

    private fun initElements() {
        findViewById(R.id.promo_btn)!!.isEnabled = false

        load()
        initList()
        createOrderListView()

        (findViewById(R.id.promo_text) as EditText).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) { checkPromo() }
        })
    }

    private fun initList() {
        val view = findViewById(R.id.recyclerView) as RecyclerListView
        view.layoutManager = LinearLayoutManager(this)
        view.itemAnimator = DefaultItemAnimator()
        view.emptyView = null

        adapter = OrderContentAdapter(this)
        view.adapter = adapter
    }

    private fun createOrderListView() {
        OrderList.setDecorationPrice()
        val orderList = OrderList.get()
        val listToShow = ArrayList<Pair<String, Any>>()

        for (i in orderList!!.indices) {
            val order = orderList[i]
            addHeader(order, listToShow)
            addBasic(order, listToShow)
        }

        adapter!!.setCollection(listToShow)
        adapter!!.notifyDataSetChanged()

        Handler().postDelayed({
            (findViewById(R.id.scroll) as NestedScrollView).fullScroll(NestedScrollView.FOCUS_UP)
        }, 100)

        setSums()
    }

    private fun setSums() {
        (findViewById(R.id.price) as TextView).text = Integer.toString(totalPrice) + getString(R.string.ruble_sign)

        deliveryFee = intent.getIntExtra(EXTRA_DELIVERY_FEE, 0)
        if (deliveryFee != 0) {
            (findViewById(R.id.delivery) as TextView).text = Integer.toString(deliveryFee) + getString(R.string.ruble_sign)
        }
        (findViewById(R.id.cont_btn) as Button).text = getString(R.string.create_order_code) +
                Integer.toString(totalPrice + deliveryFee) + getString(R.string.ruble_sign)
    }

    private fun addHeader(order: Order, listToShow: ArrayList<Pair<String, Any>>) {
        val sum = getFillSum(order)

        val header = OrderContentAdapter.Header(
                order.category.name,
                sum,
                order.count,
                order.category.icon,
                order.color
        )

        listToShow.add(Pair<String, Any>("H", header))
    }

    private fun addBasic(order: Order, listToShow: ArrayList<Pair<String, Any>>) {
        OrderList.pullDecorationToEndOfTreatmentsList()
        (0..order.treatments!!.size - 1)
                .map { order.treatments!![it] }
                .map { OrderContentAdapter.Basic(it.name, it.price) }
                .mapTo(listToShow) { Pair<String, Any>("B", it) }
    }

    private fun getFillSum(order: Order): Int {
        if (order.treatments == null) return 0
        var sum = (0..order.treatments!!.size - 1).sumBy { order.treatments!![it].price }
        sum *= order.count

        return sum
    }

    private val totalPrice: Int
        get() {
            val price = (0..OrderList.get()!!.size - 1).sumBy { getFillSum(OrderList.get(it)!!) }
            return price
        }

    private fun openActivity(activity: Class<out Activity>) {
        val intent = Intent(this@LaundryAndOrderActivity, activity)
        intent.putExtra(EXTRA_PROMO_CODE_ID, promoCodeId)
        intent.putExtra(EXTRA_PRICE, totalPrice + deliveryFee)
        startActivity(intent)
    }

    private fun load() {
        ServerApi.get(this@LaundryAndOrderActivity).api().getLaundry(intent.getIntExtra(EXTRA_ID, 1)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    parseAnswer(response.body())
                } else {
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(obj: JsonObject) {
        (findViewById(R.id.name_text) as TextView).text = obj.get("name").asString
        (findViewById(R.id.desc_text) as TextView).text = obj.get("description").asString

        Image.loadPhoto(this, ServerConfig.imageUrl + obj.get("background_image_url").asString, findViewById(R.id.back_image) as ImageView)
        Image.loadPhoto(this, ServerConfig.imageUrl + obj.get("logo_url").asString, findViewById(R.id.logo_image) as ImageView)

        val count = obj.get("ratings_count").asInt
        val pluralText = resources.getQuantityString(R.plurals.review, count, count)
        (findViewById(R.id.reviews_btn) as TextView).text = pluralText

        (findViewById(R.id.ratingBar) as SimpleRatingBar).rating = obj.get("rating").asFloat
        setDates()
    }

    private fun setDates() {
        (findViewById(R.id.curier_date) as TextView).text = intent.getStringExtra(EXTRA_COLLECTION_DATE)
        (findViewById(R.id.delivery_date) as TextView).text = intent.getStringExtra(EXTRA_DELIVERY_DATE)
    }

    companion object {
        private val EXTRA_ID = "id"
        private val EXTRA_PRICE = "price"
        private val EXTRA_DELIVERY_FEE = "deliveryFee"
        private val EXTRA_COLLECTION_DATE = "collectionDate"
        private val EXTRA_DELIVERY_DATE = "deliveryDate"
        private val EXTRA_PROMO_CODE_ID = "promoCodeId"
    }
}
