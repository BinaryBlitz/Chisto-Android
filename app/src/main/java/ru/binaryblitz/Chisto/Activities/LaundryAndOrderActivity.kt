package ru.binaryblitz.Chisto.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Pair
import android.widget.Button
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
import ru.binaryblitz.Chisto.Utils.Image
import ru.binaryblitz.Chisto.Utils.OrderList
import java.util.*

class LaundryAndOrderActivity : BaseActivity() {
    private var layout: SwipeRefreshLayout? = null
    private var adapter: OrderContentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_laundry_and_order)

        initElements()
        setOnClickListeners()

        load()
    }

    private fun setOnClickListeners() {

        findViewById(R.id.left_btn).setOnClickListener { finish() }

        findViewById(R.id.cont_btn).setOnClickListener {
            val userNotLogged = DeviceInfoStore.getUserObject(this@LaundryAndOrderActivity) == null || DeviceInfoStore.getUserObject(this@LaundryAndOrderActivity)!!.phone == "null"
            if (userNotLogged) openActivity(RegistrationActivity::class.java)
            else openActivity(PersonalInfoActivity::class.java)
        }

        findViewById(R.id.reviews_btn).setOnClickListener {
            val intent = Intent(this@LaundryAndOrderActivity, ReviewsActivity::class.java)
            intent.putExtra(EXTRA_ID, getIntent().getIntExtra(EXTRA_ID, 1))
            startActivity(intent)
        }
    }

    private fun initElements() {
        layout = findViewById(R.id.refresh) as SwipeRefreshLayout
        layout!!.setOnRefreshListener(null)
        layout!!.isEnabled = false
        layout!!.setColorSchemeResources(R.color.colorAccent)

        load()
        initList()
        createOrderListView()
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

    private fun createOrderListView() {
        val orderList = OrderList.get()
        val listToShow = ArrayList<Pair<String, Any>>()

        for (i in orderList!!.indices) {
            val order = orderList[i]
            addHeader(order, listToShow)
            addBasic(order, listToShow)
        }

        adapter!!.setCollection(listToShow)
        adapter!!.notifyDataSetChanged()

        setSums()
    }

    private fun setSums() {
        (findViewById(R.id.cost) as TextView).text = Integer.toString(allOrdersCost) + " \u20bd"

        val deliveryCost = intent.getIntExtra(EXTRA_DELIVERY_COST, 0)
        if (deliveryCost != 0) {
            (findViewById(R.id.delivery) as TextView).text = Integer.toString(deliveryCost) + " \u20bd"
        }
        (findViewById(R.id.cont_btn) as Button).text = getString(R.string.create_order_code) +
                Integer.toString(allOrdersCost) + " \u20bd"
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
        (0..order.treatments!!.size - 1)
                .map { order.treatments!![it] }
                .map { OrderContentAdapter.Basic(it.name, it.cost) }
                .mapTo(listToShow) { Pair<String, Any>("B", it) }
    }

    private fun getFillSum(order: Order): Int {
        if (order.treatments == null) return 0
        var sum = (0..order.treatments!!.size - 1).sumBy { order.treatments!![it].cost }
        sum *= order.count

        if (order.decoration) sum = (sum.toDouble() * OrderList.getDecorationMultiplier()).toInt()

        return sum
    }

    private val allOrdersCost: Int
        get() {
            val cost = (0..OrderList.get()!!.size - 1).sumBy { getFillSum(OrderList.get(it)!!) }
            return cost
        }

    private fun openActivity(activity: Class<out Activity>) {
        val intent = Intent(this@LaundryAndOrderActivity, activity)
        intent.putExtra(EXTRA_PRICE, allOrdersCost);
        startActivity(intent)
    }

    private fun load() {
        layout!!.isRefreshing = true
        ServerApi.get(this@LaundryAndOrderActivity).api().getLaundry(intent.getIntExtra(EXTRA_ID, 1)).enqueue(object : Callback<JsonObject> {
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
        (findViewById(R.id.name_text) as TextView).text = obj.get("name").asString
        (findViewById(R.id.desc_text) as TextView).text = obj.get("description").asString

        Image.loadPhoto(ServerConfig.imageUrl + obj.get("background_image_url").asString, findViewById(R.id.back_image) as ImageView)
        Image.loadPhoto(ServerConfig.imageUrl + obj.get("logo_url").asString, findViewById(R.id.logo_image) as ImageView)

        val count = obj.get("ratings_count").asInt
        val pluralText = resources.getQuantityString(R.plurals.review, count, count)
        (findViewById(R.id.reviews_btn) as TextView).text = pluralText

        (findViewById(R.id.ratingBar) as SimpleRatingBar).rating = obj.get("rating").asFloat
        setDates()
    }

    private fun setDates() {
        (findViewById(R.id.curier_date) as TextView).text = intent.getStringExtra(EXTRA_COLLECTION_DATE)
        (findViewById(R.id.delivery_date) as TextView).text = intent.getStringExtra(EXTRA_DELIVERY_DATE)
        (findViewById(R.id.delivery_bounds) as TextView).text = intent.getStringExtra(EXTRA_DELIVERY_BOUNDS)
        (findViewById(R.id.delivery_bounds2) as TextView).text = intent.getStringExtra(EXTRA_DELIVERY_BOUNDS)
    }

    companion object {
        private val EXTRA_ID = "id"
        private val EXTRA_PRICE = "price"
        private val EXTRA_DELIVERY_COST = "deliveryCost"
        private val EXTRA_COLLECTION_DATE = "collectionDate"
        private val EXTRA_DELIVERY_DATE = "deliveryDate"
        private val EXTRA_DELIVERY_BOUNDS = "deliveryBounds"
    }
}
