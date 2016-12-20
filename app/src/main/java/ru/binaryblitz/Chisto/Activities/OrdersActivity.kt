package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonObject
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Adapters.OrdersAdapter
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Custom.RecyclerListView
import ru.binaryblitz.Chisto.Model.MyOrder
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Utils.*
import ru.binaryblitz.Chisto.Utils.Animations.Animations

class OrdersActivity : BaseActivity() {

    private var adapter: OrdersAdapter? = null
    private var contBtn: TextView? = null
    private var laundryId: Int = 0
    private var dialogOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(ru.binaryblitz.Chisto.R.layout.activity_orders)

        contBtn = findViewById(ru.binaryblitz.Chisto.R.id.textView2) as TextView
        initRecyclerView()
        setOnClickListeners()

        Handler().post { getUser() }
    }

    private fun getUser() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                dialog.dismiss()
                if (response.isSuccessful) parseAnswer(response.body())
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                dialog.dismiss()
            }
        })
    }

    private fun parseAnswer(obj: JsonObject) {
        LogUtil.logError(obj.toString())

        val order = obj.get("order")
        if (order == null || obj.get("order").isJsonNull) return

        val myOrder = MyOrder(order.asJsonObject)

        if (myOrder.status != MyOrder.Status.COMPLETED) return

        val review = order.asJsonObject.get("rating") ?: return

        laundryId = AndroidUtilities.getIntFieldFromJson(order.asJsonObject.get("laundry").asJsonObject.get("id"))
        if (review.isJsonNull) showReviewDialog(AndroidUtilities.getIntFieldFromJson(order.asJsonObject.get("id")))
    }

    private fun sendReview() {
        val dialog = ProgressDialog(this)
        dialog.show()
        ServerApi.get(this).api().sendReview(laundryId, generateJson(), DeviceInfoStore.getToken(this)).enqueue( object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>) {
                dialog.dismiss()
                Animations.animateRevealHide(findViewById(R.id.dialog))
                if (response.isSuccessful) parseReviewResponse()
                else onServerError(response)
            }

            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                dialog.dismiss()
                Animations.animateRevealHide(findViewById(R.id.dialog))
                onInternetConnectionError()
            }
        })
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

    }

    private fun showReviewDialog(id: Int) {
        Handler().post {
            dialogOpened = true
            (findViewById(R.id.order_name_completed) as TextView).text =
                    getString(R.string.order) + " № " + id.toString() + getString(R.string.completed)
            Animations.animateRevealShow(findViewById(R.id.dialog), this@OrdersActivity)
        }
    }

    private fun initRecyclerView() {
        val view = findViewById(R.id.recyclerView) as RecyclerListView
        view.layoutManager = LinearLayoutManager(this)
        view.itemAnimator = DefaultItemAnimator()
        view.setHasFixedSize(true)
        view.emptyView = findViewById(R.id.empty_orders)

        adapter = OrdersAdapter(this)
        view.adapter = adapter

        val mItemTouchHelper = ItemTouchHelper(TouchHelper(0, ItemTouchHelper.LEFT, this, view))
        mItemTouchHelper.attachToRecyclerView(view)
        view.addItemDecoration(SwipeItemDecoration())
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener {
            val intent = Intent(this@OrdersActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        findViewById(R.id.right_btn).setOnClickListener {
            val intent = Intent(this@OrdersActivity, SelectCategoryActivity::class.java)
            startActivity(intent)
        }

        findViewById(R.id.add_btn).setOnClickListener {
            val intent = Intent(this@OrdersActivity, SelectCategoryActivity::class.java)
            startActivity(intent)
        }

        findViewById(R.id.cont_btn).setOnClickListener {
            sendReview()
        }
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    private fun update() {
        if (OrderList.get() != null) {
            adapter!!.setCollection(OrderList.get()!!)
            adapter!!.notifyDataSetChanged()

            if (adapter!!.itemCount != 0) {
                contBtn!!.setText(R.string.cont_code)
                contBtn!!.setOnClickListener {
                    val intent = Intent(this@OrdersActivity, LaundriesActivity::class.java)
                    startActivity(intent)
                }
            } else {
                contBtn!!.setText(R.string.nothing_selected_code)
                contBtn!!.isEnabled = false
                contBtn!!.setOnClickListener(null)
            }
        }
    }
}
