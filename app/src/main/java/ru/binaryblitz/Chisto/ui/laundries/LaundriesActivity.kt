package ru.binaryblitz.Chisto.ui.laundries

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.util.Pair
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_laundries.*
import kotlinx.android.synthetic.main.old_order_popup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Laundry
import ru.binaryblitz.Chisto.entities.Order
import ru.binaryblitz.Chisto.entities.Treatment
import ru.binaryblitz.Chisto.network.DeviceInfoStore
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.network.ServerConfig
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.laundries.adapters.LaundriesAdapter
import ru.binaryblitz.Chisto.ui.order.OrdersActivity
import ru.binaryblitz.Chisto.utils.AndroidUtilities
import ru.binaryblitz.Chisto.utils.Animations
import ru.binaryblitz.Chisto.utils.AppConfig
import ru.binaryblitz.Chisto.utils.DateUtils
import ru.binaryblitz.Chisto.utils.Image
import ru.binaryblitz.Chisto.utils.OrderList
import java.util.*

class LaundriesActivity : BaseActivity() {

    private val adapter by lazy { LaundriesAdapter() }
    private var selectedIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_laundries)

        setOnClickListeners()
        initList()

        Handler().post { load() }
    }

    private fun setOnClickListeners() {
        left_btn.setOnClickListener { finish() }
        right_btn.setOnClickListener { showDialog() }
        order_current_btn.setOnClickListener { clickCurrentBtn() }
        cont_btn.setOnClickListener {
            if (dialogOpened) {
                Handler().post {
                    dialogOpened = false
                    Animations.animateRevealHide(findViewById(R.id.dialog))
                }
            }
        }
    }

    private fun clickCurrentBtn() {
        val intent = Intent(this@LaundriesActivity, LaundryAndOrderActivity::class.java)
        OrderList.resetDecorationPrices()
        OrderList.setLaundry(laundry!!)
        OrderList.setDecorationMultiplier(laundry!!.decorationMultipliers!!)
        countSums(laundryObject!!.get("laundry_treatments").asJsonArray)

        OrderList.setDecorationPrice()

        setLaundryTreatmentsIds(laundry!!.index!!)
        intent.putExtra(EXTRA_ID, laundry!!.id)
        intent.putExtra(EXTRA_COLLECTION_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry!!.collectionDate))
        intent.putExtra(EXTRA_DELIVERY_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry!!.deliveryDate))
        intent.putExtra(EXTRA_COLLECTION_BOUNDS, getCollectionPeriod(laundry!!))

        if (laundry!!.orderPrice < laundry!!.freeDeliveryFrom) {
            intent.putExtra(EXTRA_DELIVERY_FEE, laundry!!.deliveryFee)
        }

        intent.putExtra(EXTRA_DELIVERY_BOUNDS, getPeriod(laundry!!))
        startActivity(intent)
    }

    private fun getPeriod(laundry: Laundry): String {
        return getString(R.string.from_code) + DateUtils.getTimeStringRepresentation(laundry.deliveryDateOpensAt) +
                getString(R.string.end_bound_code) +
                DateUtils.getTimeStringRepresentation(laundry.deliveryDateClosesAt)
    }

    private fun getCollectionPeriod(laundry: Laundry): String {
        return getString(R.string.from_code) + DateUtils.getTimeStringRepresentation(laundry.collectionDateOpensAt) +
                getString(R.string.end_bound_code) +
                DateUtils.getTimeStringRepresentation(laundry.collectionDateClosesAt)
    }

    private fun initList() {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@LaundriesActivity)
            itemAnimator = DefaultItemAnimator()
            emptyView = null
            adapter = this@LaundriesActivity.adapter
        }

        refresh.apply {
            setOnRefreshListener(null)
            isEnabled = false
            setColorSchemeResources(R.color.colorAccent)
        }
    }

    private fun showDialog() {
        val items = ArrayList<String>()
        items.add(getString(R.string.rate_filter))
        items.add(getString(R.string.cost_filter))
        items.add(getString(R.string.speed_filter))

        MaterialDialog.Builder(this)
                .title(R.string.title)
                .items(items)
                .itemsCallbackSingleChoice(selectedIndex) { _, _, which, _ ->
                    selectedIndex = which
                    sort(which)
                    true
                }
                .positiveText(R.string.choose)
                .show()
    }

    private fun sort(which: Int) {
        when (which) {
            0 -> adapter.sortByRating()
            1 -> adapter.sortByCost()
            2 -> adapter.sortBySpeed()
        }
    }

    private fun load() {
        val request = if (longTreatment) {
            ServerApi.get(this).api().getLaundries(DeviceInfoStore.getCityObject(this)!!.id, true)
        } else {
            ServerApi.get(this).api().getLaundries(DeviceInfoStore.getCityObject(this)!!.id)
        }

        request.enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                refresh.isRefreshing = false

                if (response.isSuccessful) {
                    parseAnswer(response.body() as JsonArray)
                } else {
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                refresh.isRefreshing = false
                onInternetConnectionError()
            }
        })
    }

    fun setLaundryTreatmentsIds(index: Int) {
        val laundryTreatments = array!!.get(index).asJsonObject.get("laundry_treatments").asJsonArray

        if (laundryTreatments.size() == 0) {
            return
        }

        for ((_, treatments) in OrderList.get()!!) {
            treatments!!
                    .filter { it.id != AppConfig.decorationId }
                    .forEach { it.laundryTreatmentId = findId(it.id, laundryTreatments) }
        }
    }

    private fun findId(id: Int, array: JsonArray): Int {
        (0 until array.size())
                .map { array.get(it).asJsonObject }
                .filter { AndroidUtilities.getIntFieldFromJson(it.get("treatment_id")) == id }
                .forEach { return AndroidUtilities.getIntFieldFromJson(it.get("id")) }

        return 0
    }

    private fun parseAnswer(array: JsonArray) {
        Companion.array = array
        val collection = ArrayList<Laundry>()

        for (i in 0 until array.size()) {
            val obj = array.get(i).asJsonObject
            processOrderForLaundry(i, obj, collection)
        }
        adapter.setCollection(collection)
        adapter.notifyDataSetChanged()
        adapter.sortByRating()

        loadLaundry()
    }

    private fun processOrderForLaundry(i: Int, obj: JsonObject, collection: ArrayList<Laundry>) {
        val laundry = parseLaundry(i, obj)

        if (!checkTreatments(obj)) {
            return
        }

        OrderList.resetDecorationPrices()
        OrderList.setLaundry(laundry)
        OrderList.setDecorationMultiplier(laundry.decorationMultipliers!!)
        countSums(i)
        OrderList.setDecorationPrice()
        laundry.orderPrice = totalPrice

        if (!checkMinimumCost(laundry, obj)) {
            laundry.isPassingMinimumPrice = false
        }

        collection.add(laundry)
    }

    private fun checkMinimumCost(laundry: Laundry, obj: JsonObject): Boolean {
        val minimum = AndroidUtilities.getIntFieldFromJson(obj.get("minimum_order_price"))
        laundry.minimumOrderPrice = minimum
        return laundry.orderPrice >= minimum
    }

    private fun getDecorationMultipliers(array: JsonArray): ArrayList<Pair<Int, Double>> {
        return (0 until array.size())
                .map { array.get(it).asJsonObject }
                .mapTo(ArrayList()) {
                    Pair(AndroidUtilities.getIntFieldFromJson(it.get("item_id")),
                            AndroidUtilities.getDoubleFieldFromJson(it.get("decoration_multiplier")))
                }
    }

    private fun parseLaundry(index: Int, obj: JsonObject): Laundry {
        return Laundry(
                AndroidUtilities.getIntFieldFromJson(obj.get("id")),
                ServerConfig.imageUrl + AndroidUtilities.getStringFieldFromJson(obj.get("logo_url")),
                AndroidUtilities.getStringFieldFromJson(obj.get("name")),
                AndroidUtilities.getStringFieldFromJson(obj.get("description")),
                AndroidUtilities.getDoubleFieldFromJson(obj.get("rating")).toFloat(),
                DateUtils.parse(AndroidUtilities.getStringFieldFromJson(obj.get("collection_from"))),
                DateUtils.parse(AndroidUtilities.getStringFieldFromJson(obj.get("delivery_from"))),
                DateUtils.parse(AndroidUtilities.getStringFieldFromJson(obj.get("delivery_from"))),
                DateUtils.parse(AndroidUtilities.getStringFieldFromJson(obj.get("delivery_to"))),
                totalPrice,
                index,
                getDecorationMultipliers(obj.get("laundry_items").asJsonArray),
                AndroidUtilities.getIntFieldFromJson(obj.get("delivery_fee")),
                AndroidUtilities.getIntFieldFromJson(obj.get("free_delivery_from")),
                true,
                0,
                DateUtils.parse(AndroidUtilities.getStringFieldFromJson(obj.get("collection_from"))),
                DateUtils.parse(AndroidUtilities.getStringFieldFromJson(obj.get("collection_to")))
        )
    }

    private fun getOrderPartPrice(order: Order): Int {
        if (order.treatments == null) return 0
        var sum = (0 until order.treatments!!.size).sumBy { order.treatments!![it].price }
        sum *= order.count
        return sum
    }

    private val totalPrice: Int
        get() = (0 until OrderList.get()!!.size).sumBy { getOrderPartPrice(OrderList.get(it)!!) }

    private fun checkTreatments(obj: JsonObject): Boolean {
        if (obj.get("laundry_treatments") == null || obj.get("laundry_treatments").isJsonNull) {
            return false
        }

        val treatments = obj.get("laundry_treatments").asJsonArray

        if (treatments.size() == 0) {
            return false
        }

        val laundryTreatments = fillLaundryTreatments(treatments)
        val orderTreatments = OrderList.getAllTreatments()

        return checkTreatmentsAvailability(orderTreatments, laundryTreatments)
    }

    private fun fillLaundryTreatments(treatments: JsonArray): ArrayList<Int> {
        return (0 until treatments.size())
                .map { treatments.get(it).asJsonObject }
                .mapTo(ArrayList()) { AndroidUtilities.getIntFieldFromJson(it.get("treatment_id")) }
    }

    private fun checkTreatmentsAvailability(orderTreatments: ArrayList<Treatment>, laundryTreatments: ArrayList<Int>): Boolean {
        return orderTreatments.indices.none { orderTreatments[it].id != AppConfig.decorationId && !checkTreatmentAvailability(orderTreatments[it], laundryTreatments) }
    }

    private fun checkTreatmentAvailability(treatment: Treatment, laundryTreatments: ArrayList<Int>): Boolean {
        for (j in laundryTreatments.indices) {
            if (treatment.id == laundryTreatments[j]) {
                return true
            }

            if (j == laundryTreatments.size - 1) {
                return false
            }
        }

        return true
    }

    fun countSums(index: Int) {
        val treatments = array!!.get(index).asJsonObject.get("laundry_treatments").asJsonArray

        if (treatments.size() == 0) {
            return
        }

        val laundryTreatments = fillPrices(treatments)
        val orderTreatments = OrderList.getAllTreatments()

        fillOrderList(orderTreatments, laundryTreatments)
    }

    private fun countSums(treatments: JsonArray) {
        if (treatments.size() == 0) {
            return
        }

        val laundryTreatments = fillPrices(treatments)
        val orderTreatments = OrderList.getAllTreatments()

        fillOrderList(orderTreatments, laundryTreatments)
    }

    private fun fillOrderList(orderTreatments: ArrayList<Treatment>, laundryTreatments: ArrayList<Pair<Int, Int>>) {
        orderTreatments.indices
                .filter { orderTreatments[it].id != AppConfig.decorationId }
                .forEach { setPriceForTreatment(orderTreatments[it], laundryTreatments) }
    }

    private fun setPriceForTreatment(treatment: Treatment, laundryTreatments: ArrayList<Pair<Int, Int>>) {
        laundryTreatments.indices
                .filter { treatment.id == laundryTreatments[it].first }
                .forEach { OrderList.setPrice(treatment.id, laundryTreatments[it].second!!) }
    }

    private fun fillPrices(treatments: JsonArray): ArrayList<Pair<Int, Int>> {
        return (0 until treatments.size())
                .map { treatments.get(it).asJsonObject }
                .mapTo(ArrayList()) {
                    Pair(AndroidUtilities.getIntFieldFromJson(it.get("treatment_id")),
                            AndroidUtilities.getIntFieldFromJson(it.get("price")))
                }
    }

    private fun loadLaundry() {
        val dialog = ProgressDialog(this)
        dialog.show()

        for (i in 0 until array!!.size()) {
            if (OrdersActivity.laundryId == AndroidUtilities.getIntFieldFromJson(array!!.get(i).asJsonObject.get("id"))) {
                dialog.dismiss()
                parseAnswer(i, array!!.get(i).asJsonObject)
                break
            }

            if (i == array!!.size() - 1) {
                dialog.dismiss()
            }
        }
    }

    private fun parseAnswer(index: Int, obj: JsonObject) {
        if (!checkTreatments(obj)) return

        laundryObject = obj

        laundry = parseLaundry(index, obj)
        OrderList.resetDecorationPrices()
        OrderList.setLaundry(laundry!!)
        OrderList.setDecorationMultiplier(laundry!!.decorationMultipliers!!)

        if (obj.get("laundry_treatments") != null && !obj.get("laundry_treatments").isJsonNull) {
            countSums(obj.get("laundry_treatments").asJsonArray)
        }

        OrderList.setDecorationPrice()
        laundry!!.orderPrice = totalPrice

        setPrices(laundry!!)

        if (!checkMinimumCost(laundry!!, obj)) return

        setTextToField(R.id.desc_text, laundry!!.description)
        setTextToField(R.id.order_current_btn, getString(R.string.ordering_code))
        setTextToField(R.id.name_text, laundry!!.name)
        setDates(laundry!!)

        Image.loadPhoto(this, ServerConfig.imageUrl + obj.get("background_image_url").asString, findViewById<ImageView>(R.id.back_image))
        Image.loadPhoto(this, ServerConfig.imageUrl + obj.get("logo_url").asString, findViewById<ImageView>(R.id.logo_image))

        Handler().post {
            dialogOpened = true
            Animations.animateRevealShow(findViewById(R.id.dialog), this@LaundriesActivity)
        }
    }

    private fun setDates(laundry: Laundry) {
        setTextToField(R.id.curier_date_dialog, DateUtils.getDateStringRepresentationWithoutTime(laundry.collectionDate))
        setTextToField(R.id.delivery_date_dialog, DateUtils.getDateStringRepresentationWithoutTime(laundry.deliveryDate))
        setTextToField(R.id.delivery_bounds_dialog, getPeriod(laundry))
    }

    private fun setPrices(laundry: Laundry) {
        setTextToField(R.id.curier_cost_dialog, getString(R.string.from_code) +
                DateUtils.getTimeStringRepresentation(laundry.deliveryDateOpensAt) +
                getString(R.string.end_bound_code) + DateUtils.getTimeStringRepresentation(laundry.deliveryDateClosesAt))
        setTextToField(R.id.sum_dialog, (laundry.orderPrice + getDeliveryFee(laundry)).toString() + " \u20bd")
    }

    private fun getDeliveryFee(laundry: Laundry): Int {
        return if (laundry.orderPrice >= laundry.freeDeliveryFrom) {
            0
        } else {
            laundry.deliveryFee
        }
    }

    private fun setTextToField(id: Int, text: String) {
        (findViewById<TextView>(id)).text = text
    }

    companion object {
        private var dialogOpened = false
        private var array: JsonArray? = null
        private var laundry: Laundry? = null
        private var laundryObject: JsonObject? = null
        var longTreatment = false
        private val EXTRA_ID = "id"
        private val EXTRA_DELIVERY_BOUNDS = "deliveryBounds"
        private val EXTRA_DELIVERY_FEE = "deliveryFee"
        private val EXTRA_COLLECTION_DATE = "collectionDate"
        private val EXTRA_DELIVERY_DATE = "deliveryDate"
        private const val EXTRA_COLLECTION_BOUNDS = "extra_collection_bounds"
    }
}
