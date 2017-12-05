package ru.binaryblitz.Chisto.ui.laundries

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
import android.view.View
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonObject
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_laundry_and_order.*
import kotlinx.android.synthetic.main.dialog_promocode.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Order
import ru.binaryblitz.Chisto.network.DeviceInfoStore
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.network.ServerConfig
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.order.ReviewsActivity
import ru.binaryblitz.Chisto.ui.order.adapters.OrderSimpleContentAdapter
import ru.binaryblitz.Chisto.ui.profile.PersonalInfoActivity
import ru.binaryblitz.Chisto.ui.profile.RegistrationActivity
import ru.binaryblitz.Chisto.utils.AndroidUtilities
import ru.binaryblitz.Chisto.utils.Animations
import ru.binaryblitz.Chisto.utils.Image
import ru.binaryblitz.Chisto.utils.OrderList
import java.util.*

class LaundryAndOrderActivity : BaseActivity() {
    private val adapter by lazy { OrderSimpleContentAdapter() }
    private var deliveryFee = 0
    private var dialogOpened = false
    private var promoCodeId = 0
    private var discount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_laundry_and_order)

        initElements()
        setOnClickListeners()

        load()
    }

    private fun showPromoCodeDialog() {
        Handler().post {
            dialogOpened = true
            Animations.animateRevealShow(findViewById(R.id.dialog), this@LaundryAndOrderActivity)
        }
    }

    private fun parsePromoCode(obj: JsonObject) {
        hidePromoCodeButton()
        parsePromoInformationFromJson(obj)
        closeDialog()
    }

    private fun hidePromoCodeButton() {
        add_btn.visibility = View.GONE
        promo_discount.visibility = View.VISIBLE
    }

    private fun parsePromoInformationFromJson(obj: JsonObject) {
        promoCodeId = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        discount = calculateDiscount(AndroidUtilities.getIntFieldFromJson(obj.get("discount")))

        promo_discount.text = getString(R.string.minus_sign) + discount + getString(R.string.ruble_sign)
        cont_btn.text = getString(R.string.create_order_code) +
                Integer.toString(totalPrice + deliveryFee - discount) + getString(R.string.ruble_sign)
    }

    private fun calculateDiscount(percent: Int): Int {
        return ((totalPrice + deliveryFee).toDouble() * (percent.toDouble() / 100.0)).toInt()
    }

    private fun showPromoError() {
        promo_help_text.text = getString(R.string.promo_error)
    }

    private fun getPromoCode() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().getPromoCode(promo_text.text.toString(), DeviceInfoStore.getToken(this))
                .enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                AndroidUtilities.hideKeyboard(findViewById(R.id.main))

                if (response.isSuccessful) {
                    parsePromoCode(response.body()!!)
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
        Animations.animateRevealHide(findViewById(R.id.dialog))
    }

    override fun onBackPressed() {
        if (dialogOpened) {
            closeDialog()
        } else {
            finish()
        }
    }

    private fun checkPromoCode(): Boolean {
        if (promo_text.text.toString().isEmpty()) {
            promo_btn.isEnabled = false
            return false
        }

        promo_btn.isEnabled = true
        return true
    }

    private fun setOnClickListeners() {
        left_btn.setOnClickListener { finish() }

        cont_btn.setOnClickListener {
            if (DeviceInfoStore.getToken(this@LaundryAndOrderActivity) == "null") {
                openActivity(RegistrationActivity::class.java)
            } else {
                openActivity(PersonalInfoActivity::class.java)
            }
        }

        promo_btn.setOnClickListener {
            if (checkPromoCode()) {
                getPromoCode()
            }
        }

        add_btn.setOnClickListener {
            showPromoCodeDialog()
        }

        reviews_btn.setOnClickListener {
            val intent = Intent(this@LaundryAndOrderActivity, ReviewsActivity::class.java)
            intent.putExtra(EXTRA_ID, getIntent().getIntExtra(EXTRA_ID, 1))
            startActivity(intent)
        }

        ratingBarBtn.setOnClickListener {
            val intent = Intent(this@LaundryAndOrderActivity, ReviewsActivity::class.java)
            intent.putExtra(EXTRA_ID, getIntent().getIntExtra(EXTRA_ID, 1))
            startActivity(intent)
        }
    }

    private fun initElements() {
        promo_btn.isEnabled = false

        load()
        initList()
        createOrderListView()

        promo_text.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) { checkPromoCode() }
        })
    }

    private fun initList() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LaundryAndOrderActivity)
            itemAnimator = DefaultItemAnimator()
            emptyView = null
            adapter = this@LaundryAndOrderActivity.adapter
        }
    }

    private fun createOrderListView() {
        OrderList.setDecorationPrice()
        val orderList = OrderList.get()
        val listToShow = ArrayList<OrderSimpleContentAdapter.Item>()

        orderList!!.indices
                .map { orderList[it] }
                .forEach { addItem(it, listToShow) }

        adapter.setCollection(listToShow.toList())

        Handler().postDelayed({
            scroll.fullScroll(NestedScrollView.FOCUS_UP)
        }, 100)

        setSums()
    }

    private fun setSums() {
        price.text = Integer.toString(totalPrice) + getString(R.string.ruble_sign)
        deliveryFee = intent.getIntExtra(EXTRA_DELIVERY_FEE, 0)

        if (deliveryFee != 0) {
            delivery.text = Integer.toString(deliveryFee) + getString(R.string.ruble_sign)
        }

        cont_btn.text = getString(R.string.create_order_code) +
                Integer.toString(totalPrice + deliveryFee) + getString(R.string.ruble_sign)
    }

    private fun addItem(order: Order, listToShow: ArrayList<OrderSimpleContentAdapter.Item>) {
        val sum = getFillSum(order)

        val item = OrderSimpleContentAdapter.Item(
                order.category.name,
                sum,
                order.count,
                order.category.icon,
                order.color
        )

        listToShow.add(item)
    }

    private fun getFillSum(order: Order): Int {
        if (order.treatments == null) return 0
        var sum = (0 until order.treatments!!.size).sumBy { order.treatments!![it].price }
        sum *= order.count

        return sum
    }

    private val totalPrice: Int
        get() = (0 until OrderList.get()!!.size).sumBy { getFillSum(OrderList.get(it)!!) }

    private fun openActivity(activity: Class<out Activity>) {
        val intent = Intent(this@LaundryAndOrderActivity, activity)
        intent.putExtra(EXTRA_PROMO_CODE_ID, promoCodeId)
        intent.putExtra(EXTRA_PRICE, totalPrice + deliveryFee - discount)
        startActivity(intent)
    }

    private fun load() {
        ServerApi.get(this@LaundryAndOrderActivity).api().getLaundry(
                intent.getIntExtra(EXTRA_ID, 1)
        ).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    parseAnswer(response.body()!!)
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
        name_text.text = obj.get("name").asString
        desc_text.text = obj.get("description").asString

        Image.loadPhoto(this, ServerConfig.imageUrl + obj.get("background_image_url").asString, back_image)
        Image.loadPhoto(this, ServerConfig.imageUrl + obj.get("logo_url").asString, logo_image)

        val count = obj.get("ratings_count").asInt
        val pluralText = resources.getQuantityString(R.plurals.review, count, count)
        reviews_btn.text = pluralText

        ratingBar.rating = obj.get("rating").asFloat
        setDates()
    }

    private fun setDates() {
        curier_date.text = intent.getStringExtra(EXTRA_COLLECTION_DATE)
        delivery_date.text = intent.getStringExtra(EXTRA_DELIVERY_DATE)

        curier_time.text = intent.getStringExtra(EXTRA_COLLECTION_BOUNDS)
        delivery_time.text = intent.getStringExtra(EXTRA_DELIVERY_BOUNDS)
    }

    companion object {
        private val EXTRA_ID = "id"
        private val EXTRA_PRICE = "price"
        private val EXTRA_DELIVERY_FEE = "deliveryFee"
        private val EXTRA_COLLECTION_DATE = "collectionDate"
        private val EXTRA_DELIVERY_DATE = "deliveryDate"
        private val EXTRA_PROMO_CODE_ID = "promoCodeId"
        private const val EXTRA_DELIVERY_BOUNDS = "deliveryBounds"
        private const val EXTRA_COLLECTION_BOUNDS = "extra_collection_bounds"
    }
}
