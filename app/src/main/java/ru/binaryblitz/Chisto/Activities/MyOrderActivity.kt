package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Adapters.OrderContentAdapter
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Custom.RecyclerListView
import ru.binaryblitz.Chisto.Model.CategoryItem
import ru.binaryblitz.Chisto.Model.Order
import ru.binaryblitz.Chisto.Model.Treatment
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Utils.*
import ru.binaryblitz.Chisto.Utils.Animations
import java.text.SimpleDateFormat
import java.util.*

class MyOrderActivity : BaseActivity() {
    private var layout: SwipeRefreshLayout? = null
    private var adapter: OrderContentAdapter? = null
    private var dialogOpened = false
    private var price: Int = 0
    private var deliveryFee: Int = 0
    private var freeDeliveryFrom: Int = 0
    private var isRated = false
    private var orderId = 0

    val CASH = "cash"
    val CARD = "card"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_my_order)

        initList()
        initSwipeRefresh()
        setOnClickListeners()

        Handler().post {
            layout!!.isRefreshing = true
            load()
        }
    }

    override fun onBackPressed() {
        if (dialogOpened) return
        super.onBackPressed()
    }

    private fun initList() {
        val view = findViewById(R.id.recyclerView) as RecyclerListView
        view.layoutManager = LinearLayoutManager(this)
        view.itemAnimator = DefaultItemAnimator()
        view.emptyView = null

        adapter = OrderContentAdapter(this)
        view.adapter = adapter
    }

    private fun initSwipeRefresh() {
        layout = findViewById(R.id.refresh) as SwipeRefreshLayout
        layout!!.setOnRefreshListener(null)
        layout!!.isEnabled = false
        layout!!.setColorSchemeResources(R.color.colorAccent)
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener { finish() }

        findViewById(R.id.phone_call).setOnClickListener { AndroidUtilities.call(this@MyOrderActivity, AppConfig.phone) }

        findViewById(R.id.cont_btn).setOnClickListener {
            if (!checkRating()) {
                showErrorDialog()
            } else {
                executeRatingRequest()
            }
        }

        findViewById(R.id.review_btn).setOnClickListener {
            showReviewDialog()
        }
    }

    private fun executeRatingRequest() {
        if (isRated) {
            updateRating()
        } else {
            sendRating()
        }
    }

    private fun load() {
        ServerApi.get(this).api().getOrder(intent.getIntExtra(EXTRA_ID, 1), DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                layout!!.isRefreshing = false
                parseAnswer(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                layout!!.isRefreshing = false
                onInternetConnectionError()
            }
        })
    }

    private fun parsePromo(obj: JsonObject) {
        findViewById(R.id.promocode_discount_view).visibility = View.VISIBLE
        findViewById(R.id.promocode_name_view).visibility = View.VISIBLE
        (findViewById(R.id.promo_name) as TextView).text = AndroidUtilities.getStringFieldFromJson(obj.get("code"))
        (findViewById(R.id.promo_price_difference) as TextView).text =
                getString(R.string.minus_sign) +
                        calculateDiscount(AndroidUtilities.getIntFieldFromJson(obj.get("discount"))) +
                        getString(R.string.ruble_sign)
    }

    private fun calculateDiscount(percent: Int): Int {
        if (price < freeDeliveryFrom) {
            return ((price + deliveryFee).toDouble() * (percent.toDouble() / 100.0)).toInt()
        } else {
            return ((price + deliveryFee).toDouble() * (percent.toDouble() / 100.0)).toInt()
        }
    }

    private fun parseAnswer(obj: JsonObject) {
        val status = AndroidUtilities.getStringFieldFromJson(obj.get("status"))
        processStatus(status)
        processPaymentMethod(AndroidUtilities.getStringFieldFromJson(obj.get("payment_method")))
        setLaundryInfo(obj.get("laundry").asJsonObject)

        (findViewById(R.id.date_text_view) as TextView).text = getString(R.string.my_order_code) + AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        (findViewById(R.id.date) as TextView).text = getDateFromJson(obj)

        if (obj.get("order_items") != null && !obj.get("order_items").isJsonNull) {
            createOrderListView(obj.get("order_items").asJsonArray)
        }

        if (obj.get("promo_code") != null && !obj.get("promo_code").isJsonNull) {
            parsePromo(obj.get("promo_code").asJsonObject)
        } else {
            hidePromoCodeInformation()
        }

        orderId = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        isRated = obj.get("rating") != null && !obj.get("rating").isJsonNull
        if (!isRated && isOpenedFromPush) {
            showReviewDialog()
        }
    }

    private fun hidePromoCodeInformation() {
        findViewById(R.id.promocode_discount_view).visibility = View.GONE
        findViewById(R.id.promocode_name_view).visibility = View.GONE
    }

    private fun generateJson(): JsonObject {
        val obj = JsonObject()

        obj.addProperty("value", (findViewById(R.id.ratingBar) as SimpleRatingBar).rating)
        obj.addProperty("content", (findViewById(R.id.review_text) as EditText).text.toString())

        val toSend = JsonObject()
        toSend.add("rating", obj)

        return toSend
    }

    private fun parseReviewResponse() {
        isRated = true
        Animations.animateRevealHide(findViewById(R.id.dialog))
    }

    private fun showReviewDialog() {
        Handler().post {
            dialogOpened = true
            (findViewById(R.id.order_name_completed) as TextView).text =
                    getString(R.string.order) + " № " + orderId.toString() + getString(R.string.completed)
            Animations.animateRevealShow(findViewById(R.id.dialog), this@MyOrderActivity)
        }
    }

    private fun showErrorDialog() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.error))
                .content(getString(R.string.wrong_review_code))
                .positiveText(R.string.ok_code)
                .onPositive { dialog, which -> dialog.dismiss() }
                .show()
    }

    private fun checkRating(): Boolean {
        return (findViewById(R.id.ratingBar) as SimpleRatingBar).rating.toInt() != 0
    }

    private fun sendRating() {
        val dialog = ProgressDialog(this)
        dialog.show()
        ServerApi.get(this).api().sendReview(OrdersActivity.laundryId, generateJson(), DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>) {
                dialog.dismiss()
                isOpenedFromPush = false
                Animations.animateRevealHide(findViewById(R.id.dialog))
                if (response.isSuccessful) {
                    parseReviewResponse()
                } else {
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                dialog.dismiss()
                isOpenedFromPush = false
                Animations.animateRevealHide(findViewById(R.id.dialog))
                onInternetConnectionError()
            }
        })
    }

    private fun updateRating() {
        val dialog = ProgressDialog(this)
        dialog.show()
        ServerApi.get(this).api().updateReview(OrdersActivity.laundryId, generateJson(), DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>) {
                dialog.dismiss()
                ru.binaryblitz.Chisto.Utils.Animations.animateRevealHide(findViewById(R.id.dialog))
                if (response.isSuccessful) {
                    parseReviewResponse()
                } else {
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                dialog.dismiss()
                ru.binaryblitz.Chisto.Utils.Animations.animateRevealHide(findViewById(R.id.dialog))
                onInternetConnectionError()
            }
        })
    }

    private fun createOrderListView(array: JsonArray) {
        ColorsList.load(this)
        val listToShow = ArrayList<Pair<String, Any>>()

        for (obj in array) {
            val order = getOrderFromJson(obj.asJsonObject)
            addHeader(order, listToShow)
            addBasic(order, listToShow)
        }

        adapter!!.setCollection(listToShow)
        adapter!!.notifyDataSetChanged()

        Handler().postDelayed({
            (findViewById(R.id.scroll) as NestedScrollView).fullScroll(NestedScrollView.FOCUS_UP)
        }, 100)

        setPrices()
    }

    private fun getOrderFromJson(obj: JsonObject): Order {
        val categoryJson = obj.get("order_treatments").asJsonArray.get(0).asJsonObject.get("treatment").asJsonObject.get("item").asJsonObject

        val category = getCategoryFromJson(categoryJson)

        val order = Order(
                category,
                getTreatments(obj.get("order_treatments").asJsonArray),
                AndroidUtilities.getIntFieldFromJson(obj.get("quantity")),
                ColorsList.findColor(AndroidUtilities.getIntFieldFromJson(categoryJson.get("category_id"))),
                AndroidUtilities.getBooleanFieldFromJson(obj.get("has_decoration")), 0, null)

        calculatePrices(obj, order)

        return order
    }

    private fun getCategoryFromJson(obj: JsonObject): CategoryItem {
        return CategoryItem(
                AndroidUtilities.getIntFieldFromJson(obj.get("id")),
                AndroidUtilities.getStringFieldFromJson(obj.get("icon_url")),
                AndroidUtilities.getStringFieldFromJson(obj.get("name")),
                "", false)
    }

    private fun calculatePrices(obj: JsonObject, order: Order) {
        var treatmentsPrice = getPrice(order.treatments!!)

        if (order.decoration) {
            order.decorationPrice = processDecoration(AndroidUtilities.getDoubleFieldFromJson(obj.get("multiplier")), treatmentsPrice)
            order.treatments!!.add(Treatment(0, getString(R.string.decoration), "", order.decorationPrice, false, 0))
        }

        treatmentsPrice = getPrice(order.treatments!!)
        price += treatmentsPrice * order.count
    }

    private fun getPrice(array: ArrayList<Treatment>): Int {
        return array.sumBy { it.price }
    }

    private fun getTreatments(array: JsonArray): ArrayList<Treatment> {
        val treatments: ArrayList<Treatment> = (0..array.size() - 1)
                .map { array.get(it).asJsonObject }
                .mapTo(ArrayList()) {
                    Treatment(AndroidUtilities.getIntFieldFromJson(it.get("treatment").asJsonObject.get("id")),
                            AndroidUtilities.getStringFieldFromJson(it.get("treatment").asJsonObject.get("name")),
                            "",
                            AndroidUtilities.getIntFieldFromJson(it.get("price")),
                            false, 0)
                }

        return treatments
    }

    private fun setPrices() {
        (findViewById(R.id.price) as TextView).text = Integer.toString(price) + getString(R.string.ruble_sign)

        if (price < freeDeliveryFrom) {
            setPricesWithDeliveryFee()
        } else {
            setPricesWithoutDeliveryFee()
        }
    }

    private fun setPricesWithDeliveryFee() {
        (findViewById(R.id.final_price) as TextView).text = Integer.toString(price + deliveryFee) + getString(R.string.ruble_sign)
        (findViewById(R.id.delivery) as TextView).text = Integer.toString(deliveryFee) + getString(R.string.ruble_sign)
    }

    private fun setPricesWithoutDeliveryFee() {
        (findViewById(R.id.final_price) as TextView).text = Integer.toString(price) + getString(R.string.ruble_sign)
        (findViewById(R.id.delivery) as TextView).text = getString(R.string.free)
    }

    private fun processDecoration(multiplier: Double, price: Int): Int {
        return (price * multiplier).toInt() - price
    }

    private fun addHeader(order: Order, listToShow: ArrayList<Pair<String, Any>>) {
        val sum = getFillSum(order)

        val header = OrderContentAdapter.Header(
                order.category.name,
                sum,
                order.count,
                order.category.icon,
                order.color)

        listToShow.add(Pair<String, Any>("H", header))
    }

    private fun addBasic(order: Order, listToShow: ArrayList<Pair<String, Any>>) {
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

    private fun setLaundryInfo(obj: JsonObject) {
        (findViewById(R.id.name) as TextView).text = AndroidUtilities.getStringFieldFromJson(obj.get("name"))
        (findViewById(R.id.description) as TextView).text = AndroidUtilities.getStringFieldFromJson(obj.get("description"))
        Image.loadPhoto(
                this,
                AndroidUtilities.getStringFieldFromJson(obj.get("logo_url")),
                findViewById(R.id.category_icon) as ImageView
        )

        freeDeliveryFrom = AndroidUtilities.getIntFieldFromJson(obj.get("free_delivery_from"))
        deliveryFee = AndroidUtilities.getIntFieldFromJson(obj.get("delivery_fee"))
    }

    private fun processPaymentMethod(paymentType: String) {
        val icon: Int
        val text: Int
        when (paymentType) {
            CASH -> {
                icon = R.drawable.ic_cash
                text = R.string.cash
            }
            CARD -> {
                icon = R.drawable.ic_card
                text = R.string.card
            }
            else -> {
                icon = R.drawable.ic_cash
                text = R.string.cash
            }
        }

        (findViewById(R.id.payment_type_icon) as ImageView).setImageResource(icon)
        (findViewById(R.id.payment_type_text) as TextView).setText(text)
    }

    private fun processStatus(status: String) {
        val icon: Int
        val text: Int
        val textColor: Int

        when (status) {
            "processing" -> {
                icon = R.drawable.ic_process_indicator
                textColor = ContextCompat.getColor(this, R.color.processColor)
                text = R.string.processing_code
            }
            "completed" -> {
                icon = R.drawable.ic_completed_indicator
                textColor = ContextCompat.getColor(this, R.color.completedColor)
                text = R.string.completed_code
            }
            "cleaning" -> {
                icon = R.drawable.ic_cleaning_indicator
                textColor = ContextCompat.getColor(this, R.color.cleaningColor)
                text = R.string.cleaning_code
            }
            "dispatched" -> {
                icon = R.drawable.ic_dispatched_indicator
                textColor = ContextCompat.getColor(this, R.color.dispatchedColor)
                text = R.string.dispatched_code
            }
            "confirmed" -> {
                icon = R.drawable.ic_confirmed_indicator
                textColor = ContextCompat.getColor(this, R.color.confirmedColor)
                text = R.string.confirmed_code
            }
            else -> {
                icon = R.drawable.ic_canceled_indicator
                textColor = ContextCompat.getColor(this, R.color.canceledColor)
                text = R.string.canceled_code
            }
        }

        (findViewById(R.id.order_icon) as ImageView).setImageResource(icon)
        (findViewById(R.id.status_text) as TextView).setTextColor(textColor)
        (findViewById(R.id.status_text) as TextView).setText(text)
    }

    private fun getDateFromJson(obj: JsonObject): String {
        var date: Date? = null
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            date = format.parse(obj.get("created_at").asString)
        } catch (e: Exception) {
            LogUtil.logException(e)
        }

        val format = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(date)
    }

    companion object {
        private val EXTRA_ID = "id"
        var isOpenedFromPush = false
    }
}
