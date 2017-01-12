package ru.binaryblitz.Chisto.Activities

import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.widget.ImageView
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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
import java.text.SimpleDateFormat
import java.util.*

class MyOrderActivity : BaseActivity() {
    private var layout: SwipeRefreshLayout? = null
    private var adapter: OrderContentAdapter? = null

    private var cost: Int = 0
    private var deliveryCost: Int = 0
    private var deliveryBound: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_my_order)
        Image.init(this)

        initList()
        initSwipeRefresh()
        setOnClickListeners()

        Handler().post {
            layout!!.isRefreshing = true
            load()
        }
    }

    private fun initList() {
        val view = findViewById(R.id.recyclerView) as RecyclerListView
        view.layoutManager = LinearLayoutManager(this)
        view.itemAnimator = DefaultItemAnimator()
        view.setHasFixedSize(true)
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
    }

    private fun load() {
        ServerApi.get(this).api().getOrder(intent.getIntExtra(EXTRA_ID, 1), DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                layout!!.isRefreshing = false
                if (response.isSuccessful) parseAnswer(response.body())
                else onServerError(response)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                layout!!.isRefreshing = false
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(obj: JsonObject) {
        LogUtil.logError(obj.toString())
        val status = AndroidUtilities.getStringFieldFromJson(obj.get("status"))
        processStatus(status)
        setLaundryInfo(obj.get("laundry").asJsonObject)
        (findViewById(R.id.date_text_view) as TextView).text = getString(R.string.my_order_code) +
                AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        (findViewById(R.id.number) as TextView).text = getString(R.string.number_sign) + AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        (findViewById(R.id.date_text) as TextView).text = getDateFromJson(obj)

        createOrderListView(obj.get("order_items").asJsonArray)
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

        setSums()
    }

    private fun getOrderFromJson(obj: JsonObject): Order {

        val category = CategoryItem(0, "", "", "", false)

        val order = Order(
                category,
                getTreatments(obj.get("order_treatments").asJsonArray),
                AndroidUtilities.getIntFieldFromJson(obj.get("quantity")),
                ColorsList.findColor(AndroidUtilities.getIntFieldFromJson(obj.get("item_id"))),
                AndroidUtilities.getBooleanFieldFromJson(obj.get("has_decoration")),
                0,
                null)

        var treatmentsPrice = getPrice(order.treatments!!)

        if (order.decoration) {
            order.decorationPrice = processDecoration(AndroidUtilities.getDoubleFieldFromJson(obj.get("multiplier")), treatmentsPrice)
            order.treatments!!.add(Treatment(0, getString(R.string.decoration), "", order.decorationPrice, false, 0))
        }

        treatmentsPrice = getPrice(order.treatments!!)
        cost += treatmentsPrice * order.count

        return order
    }

    private fun getPrice(array: ArrayList<Treatment>): Int {
        return array.sumBy { it.cost }
    }

    private fun getTreatments(array: JsonArray): ArrayList<Treatment> {
        val treatments: ArrayList<Treatment> = (0..array.size() - 1)
                .map { array.get(it).asJsonObject.get("laundry_treatment").asJsonObject }
                .mapTo(ArrayList()) {
                    Treatment(AndroidUtilities.getIntFieldFromJson(it.get("treatment").asJsonObject.get("id")),
                            AndroidUtilities.getStringFieldFromJson(it.get("treatment").asJsonObject.get("name")),
                            "",
                            AndroidUtilities.getIntFieldFromJson(it.get("price")),
                            false, 0)
                }

        return treatments
    }

    private fun setSums() {
        (findViewById(R.id.cost) as TextView).text = Integer.toString(cost) + getString(R.string.ruble_sign)

        if (cost < deliveryBound) {
            (findViewById(R.id.final_cost) as TextView).text = Integer.toString(cost + deliveryCost) + getString(R.string.ruble_sign)
            (findViewById(R.id.delivery) as TextView).text = Integer.toString(deliveryCost) + getString(R.string.ruble_sign)
        } else {
            (findViewById(R.id.final_cost) as TextView).text = Integer.toString(cost) + getString(R.string.ruble_sign)
            (findViewById(R.id.delivery) as TextView).text = getString(R.string.free)
        }
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
                .map { OrderContentAdapter.Basic(it.name, it.cost) }
                .mapTo(listToShow) { Pair<String, Any>("B", it) }
    }

    private fun getFillSum(order: Order): Int {
        if (order.treatments == null) return 0
        var sum = (0..order.treatments!!.size - 1).sumBy { order.treatments!![it].cost }
        sum *= order.count

        return sum
    }

    private fun setLaundryInfo(obj: JsonObject) {
        (findViewById(R.id.name) as TextView).text = AndroidUtilities.getStringFieldFromJson(obj.get("name"))
        (findViewById(R.id.description) as TextView).text = AndroidUtilities.getStringFieldFromJson(obj.get("description"))
        Image.loadPhoto(
                AndroidUtilities.getStringFieldFromJson(obj.get("logo_url")),
                findViewById(R.id.category_icon) as ImageView
        )

        deliveryBound = AndroidUtilities.getIntFieldFromJson(obj.get("free_delivery_from"))
        deliveryCost = AndroidUtilities.getIntFieldFromJson(obj.get("delivery_fee"))
    }

    private fun processStatus(status: String) {
        val icon: Int
        val text: Int
        val textColor: Int

        when (status) {
            "processing" -> {
                icon = R.drawable.ic_process_indicator
                textColor = ContextCompat.getColor(this, R.color.processColor)
                text = R.string.ready_code
            }
            "completed" -> {
                icon = R.drawable.ic_completed_indicator
                textColor = ContextCompat.getColor(this, R.color.completedColor)
                text = R.string.process_code
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

    private fun getDateFromJson(`object`: JsonObject): String {
        var date: Date? = null
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            date = format.parse(`object`.get("created_at").asString)
        } catch (e: Exception) {
            LogUtil.logException(e)
        }

        val format = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(date)
    }

    companion object {
        private val EXTRA_ID = "id"
    }
}
