package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.util.Pair
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Adapters.LaundriesAdapter
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Custom.RecyclerListView
import ru.binaryblitz.Chisto.Model.Laundry
import ru.binaryblitz.Chisto.Model.Order
import ru.binaryblitz.Chisto.Model.Treatment
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Server.ServerConfig
import ru.binaryblitz.Chisto.Utils.*
import ru.binaryblitz.Chisto.Utils.Animations.Animations
import java.text.SimpleDateFormat
import java.util.*

class LaundriesActivity : BaseActivity() {

    private var adapter: LaundriesAdapter? = null
    private var layout: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_laundries)

        setOnClickListeners()
        initList()

        Handler().post { loadLastOrder() }
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener { finish() }

        findViewById(R.id.right_btn).setOnClickListener { showDialog() }

        findViewById(R.id.order_current_btn).setOnClickListener {
            val intent = Intent(this@LaundriesActivity, LaundryAndOrderActivity::class.java)
            OrderList.setLaundryId(laundry!!.id)
            countSums(laundryObject!!.get("laundry_treatments").asJsonArray)
            intent.putExtra(EXTRA_ID, laundry!!.id)
            intent.putExtra(EXTRA_COLLECTION_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry!!.collectionDate))
            intent.putExtra(EXTRA_DELIVERY_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry!!.deliveryDate))
            startActivity(intent)
        }

        findViewById(R.id.cont_btn).setOnClickListener {
            if (dialogOpened) {
                Handler().post {
                    dialogOpened = false
                    layout!!.isRefreshing = true
                    Animations.animateRevealHide(findViewById(ru.binaryblitz.Chisto.R.id.dialog))
                    load()
                }
            }
        }
    }

    private fun initList() {
        val view = findViewById(R.id.recyclerView) as RecyclerListView
        view.layoutManager = LinearLayoutManager(this)
        view.itemAnimator = DefaultItemAnimator()
        view.setHasFixedSize(true)
        view.emptyView = null

        layout = findViewById(R.id.refresh) as SwipeRefreshLayout
        layout!!.setOnRefreshListener(null)
        layout!!.isEnabled = false
        layout!!.setColorSchemeResources(R.color.colorAccent)

        adapter = LaundriesAdapter(this)
        view.adapter = adapter
    }

    private fun showDialog() {
        val items = ArrayList<String>()
        items.add(getString(R.string.cost_filter))
        items.add(getString(R.string.speed_filter))
        items.add(getString(R.string.rate_filter))

        MaterialDialog.Builder(this)
                .title(R.string.title)
                .items(items)
                .itemsCallbackSingleChoice(-1) { dialog, view, which, text ->
                    sort(which)
                    true
                }
                .positiveText(R.string.choose)
                .show()
    }

    private fun sort(which: Int) {
        when (which) {
            0 -> adapter!!.sortByCost()
            1 -> adapter!!.sortBySpeed()
            2 -> adapter!!.sortByRating()
            else -> { }
        }
    }

    private fun load() {
        ServerApi.get(this).api().getLaundries(DeviceInfoStore.getCityObject(this)!!.id).enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                layout!!.isRefreshing = false
                if (response.isSuccessful) parseAnswer(response.body())
                else onServerError(response)
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                layout!!.isRefreshing = false
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(array: JsonArray) {
        LogUtil.logError(array.toString())
        LaundriesActivity.array = array
        val collection = ArrayList<Laundry>()

        for (i in 0..array.size() - 1) {
            val obj = array.get(i).asJsonObject
            if (!checkTreatments(obj)) continue
            countSums(i)
            if (!checkMinimumCost(obj)) continue
            collection.add(parseLaundry(i, obj))
        }
        adapter!!.sortByRating()
        adapter!!.setCollection(collection)
        adapter!!.notifyDataSetChanged()
    }

    private fun checkMinimumCost(obj: JsonObject): Boolean {
        val minimum = AndroidUtilities.getIntFieldFromJson(obj.get("minimum_order_price"))
        return allOrdersCost >= minimum
    }

    private fun getDecorationMultipliers(array: JsonArray): ArrayList<android.support.v4.util.Pair<Int, Double>> {
        val multipliers = ArrayList<android.support.v4.util.Pair<Int, Double>>()
        for (i in 0..array.size() - 1) {
            val obj = array.get(i).asJsonObject
            multipliers.add(Pair(AndroidUtilities.getIntFieldFromJson(obj.get("item_id")),
                    AndroidUtilities.getDoubleFieldFromJson(obj.get("decoration_multiplier"))))
        }

        return multipliers
    }

    private fun parseLaundry(index: Int, obj: JsonObject): Laundry {
        return Laundry(
                AndroidUtilities.getIntFieldFromJson(obj.get("id")),
                ServerConfig.imageUrl + AndroidUtilities.getStringFieldFromJson(obj.get("logo_url")),
                AndroidUtilities.getStringFieldFromJson(obj.get("name")),
                AndroidUtilities.getStringFieldFromJson(obj.get("description")),
                AndroidUtilities.getDoubleFieldFromJson(obj.get("rating")).toFloat(),
                parseDate(obj, "collection_date", "yyyy-MM-dd"),
                parseDate(obj, "delivery_date", "yyyy-MM-dd"),
                parseDate(obj, "delivery_date_opens_at", "HH:mm"),
                parseDate(obj, "delivery_date_closes_at", "HH:mm"),
                0,
                allOrdersCost,
                index,
                getDecorationMultipliers(obj.get("laundry_items").asJsonArray),
                AndroidUtilities.getIntFieldFromJson(obj.get("delivery_fee")),
                AndroidUtilities.getIntFieldFromJson(obj.get("free_delivery_from"))
        )
    }

    private fun getFillSum(order: Order): Int {
        if (order.treatments == null) return 0
        var sum = (0..order.treatments!!.size - 1).sumBy { order.treatments!![it].cost }
        sum *= order.count
        return sum
    }

    private val allOrdersCost: Int
        get() {
            val cost = (0..OrderList.get()!!.size - 1).sumBy { getFillSum(OrderList.get(it)!!) }
            return cost
        }

    private fun checkTreatments(obj: JsonObject): Boolean {
        if (obj.get("laundry_treatments") == null || obj.get("laundry_treatments").isJsonNull) return false
        val treatments = obj.get("laundry_treatments").asJsonArray
        if (treatments.size() == 0) return false

        val laundryTreatments = fillLaundryTreatments(treatments)
        val orderTreatments = OrderList.getTreatments()

        return checkTreatmentsAvailability(orderTreatments, laundryTreatments)
    }

    private fun fillLaundryTreatments(treatments: JsonArray): ArrayList<Int> {
        val laundryTreatments = (0..treatments.size() - 1)
                .map { treatments.get(it).asJsonObject }
                .mapTo(ArrayList<Int>()) { AndroidUtilities.getIntFieldFromJson(it.get("treatment_id")) }

        return laundryTreatments
    }

    private fun checkTreatmentsAvailability(orderTreatments: ArrayList<Treatment>, laundryTreatments: ArrayList<Int>): Boolean {
        return orderTreatments.indices.none { orderTreatments[it].id != -1 && !checkTreatmentAvailability(orderTreatments[it], laundryTreatments) }
    }

    private fun checkTreatmentAvailability(treatment: Treatment, laundryTreatments: ArrayList<Int>): Boolean {
        for (j in laundryTreatments.indices) {
            if (treatment.id == laundryTreatments[j]) return true
            if (j == laundryTreatments.size - 1) return false
        }

        return true
    }

    fun countSums(index: Int) {
        val treatments = array!!.get(index).asJsonObject.get("laundry_treatments").asJsonArray
        if (treatments.size() == 0) return

        val laundryTreatments = fillPrices(treatments)
        val orderTreatments = OrderList.getTreatments()

        fillOrderList(orderTreatments, laundryTreatments)
    }

    fun countSums(treatments: JsonArray) {
        if (treatments.size() == 0) return

        val laundryTreatments = fillPrices(treatments)
        val orderTreatments = OrderList.getTreatments()

        fillOrderList(orderTreatments, laundryTreatments)
    }

    private fun fillOrderList(orderTreatments: ArrayList<Treatment>, laundryTreatments: ArrayList<Pair<Int, Int>>) {
        orderTreatments.indices
                .filter { orderTreatments[it].id != -1 }
                .forEach { setPriceForTreatment(orderTreatments[it], laundryTreatments) }
    }

    private fun setPriceForTreatment(treatment: Treatment, laundryTreatments: ArrayList<Pair<Int, Int>>) {
        laundryTreatments.indices
                .filter { treatment.id == laundryTreatments[it].first }
                .forEach { OrderList.setCost(treatment.id, laundryTreatments[it].second) }
    }

    private fun fillPrices(treatments: JsonArray): ArrayList<Pair<Int, Int>> {
        val laundryTreatments = (0..treatments.size() - 1)
                .map { treatments.get(it).asJsonObject }
                .mapTo(ArrayList<Pair<Int, Int>>()) {
                    Pair(AndroidUtilities.getIntFieldFromJson(it.get("treatment_id")),
                            AndroidUtilities.getIntFieldFromJson(it.get("price")))
                }

        return laundryTreatments
    }

    private fun loadLastOrder() {
        if (DeviceInfoStore.getToken(this) == "null") {
            load()
            return
        }

        val dialog = ProgressDialog(this)
        dialog.show()
        ServerApi.get(this@LaundriesActivity).api().getOrders(DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                dialog.dismiss()
                if (response.isSuccessful) parseAnswerForPopup(response.body())
                else onServerError(response)
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    private fun loadLaundry(id: Int) {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this@LaundriesActivity).api().getLaundry(id).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                if (response.isSuccessful) parseAnswer(response.body())
                else onInternetConnectionError()
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(obj: JsonObject) {
        if (!checkTreatments(obj)) {
            load()
            return
        }
        laundryObject = obj
        laundry = parseLaundry(0, obj)
        if (obj.get("laundry_treatments") != null && !obj.get("laundry_treatments").isJsonNull) {
            countSums(obj.get("laundry_treatments").asJsonArray)
            setCosts(laundry!!)
        }

        setTextToField(R.id.name_text, laundry!!.name)
        setTextToField(R.id.desc_text, laundry!!.desc)
        setTextToField(R.id.order_current_btn, getString(R.string.ordering_code))
        setTextToField(R.id.name_text, laundry!!.name)
        setDates(laundry!!)

        Image.loadPhoto(ServerConfig.imageUrl + obj.get("background_image_url").asString, findViewById(ru.binaryblitz.Chisto.R.id.back_image) as ImageView)
        Image.loadPhoto(ServerConfig.imageUrl + obj.get("logo_url").asString, findViewById(ru.binaryblitz.Chisto.R.id.logo_image) as ImageView)

        Handler().post {
            dialogOpened = true
            Animations.animateRevealShow(findViewById(ru.binaryblitz.Chisto.R.id.dialog), this@LaundriesActivity)
        }
    }

    private fun setDates(laundry: Laundry) {
        setTextToField(R.id.curier_date, DateUtils.getDateStringRepresentationWithoutTime(laundry.collectionDate))
        setTextToField(R.id.delivery_date, DateUtils.getDateStringRepresentationWithoutTime(laundry.deliveryDate))
        setTextToField(R.id.delivery_bounds, getString(R.string.from_code) + DateUtils.getTimeStringRepresentation(laundry.deliveryDateOpensAt) +
                getString(R.string.end_bound_code) +
                DateUtils.getTimeStringRepresentation(laundry.deliveryDateClosesAt))
    }

    private fun setCosts(laundry: Laundry) {
        setTextToField(R.id.curier_cost, laundry.deliveryCost.toString() + " \u20bd")
        setTextToField(R.id.sum, laundry.orderCost.toString() + " \u20bd")
    }

    private fun setTextToField(id: Int, text: String) {
        (findViewById(id) as TextView).text = text
    }

    fun parseDate(obj: JsonObject, elementName: String, pattern: String): Date? {
        if (obj.isJsonNull) return null

        var date: Date? = null
        try {
            val format = SimpleDateFormat(pattern, Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            date = format.parse(AndroidUtilities.getStringFieldFromJson(obj.get(elementName)))
        } catch (e: Exception) {
            LogUtil.logException(e)
        }

        return date
    }

    private fun parseAnswerForPopup(array: JsonArray) {
        if (array.size() == 0) {
            load()
            return
        }
        val id = array.get(array.size() - 1).asJsonObject.get("laundry_id").asInt
        loadLaundry(id)
    }

    companion object {
        private var dialogOpened = false
        private var array: JsonArray? = null
        private var laundry: Laundry? = null
        private var laundryObject: JsonObject? = null
        private val EXTRA_ID = "id"
        private val EXTRA_COLLECTION_DATE = "collectionDate"
        private val EXTRA_DELIVERY_DATE = "deliveryDate"
    }
}
