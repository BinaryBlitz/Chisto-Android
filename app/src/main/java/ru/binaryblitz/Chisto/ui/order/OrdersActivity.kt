package ru.binaryblitz.Chisto.ui.order

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonObject
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.R.string
import ru.binaryblitz.Chisto.entities.MyOrder
import ru.binaryblitz.Chisto.network.DeviceInfoStore
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.categories.CategoryActivity
import ru.binaryblitz.Chisto.ui.laundries.LaundriesActivity
import ru.binaryblitz.Chisto.ui.order.adapters.OrdersAdapter
import ru.binaryblitz.Chisto.utils.AndroidUtilities
import ru.binaryblitz.Chisto.utils.Animations
import ru.binaryblitz.Chisto.utils.Extras.EXTRA_COLOR
import ru.binaryblitz.Chisto.utils.OrderList
import ru.binaryblitz.Chisto.views.RecyclerListView

class OrdersActivity : BaseActivity() {

    private lateinit var adapter: OrdersAdapter
    private lateinit var continueBtn: TextView
    private lateinit var titleText: TextView
    private lateinit var addItemButton: FloatingActionButton
    private lateinit var toolbar: Toolbar
    private lateinit var removeBtn: MenuItem
    private var dialogOpened = false
    private lateinit var itemColor: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_orders)

        continueBtn = findViewById(R.id.textView2) as TextView
        initViewElements()
        setOnClickListeners()

        Handler().post {
            if (newOrderId != 0) {
                showOrderDialog(newOrderId)
            } else {
                getUser()
            }
        }
    }

    private fun getUser() {
        ServerApi.get(this).api().getUser(DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful) {
                    parseAnswer(response.body())
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
            }
        })
    }

    private fun showOrderDialog(id: Int) {
        Handler().post {
            dialogOpened = true
            (findViewById(R.id.order_name) as TextView).text = getString(R.string.number_sign) + id.toString()
            Animations.animateRevealShow(findViewById(R.id.dialog_new_order), this@OrdersActivity)
            newOrderId = 0
        }
    }

    private fun parseAnswer(obj: JsonObject) {
        val order = obj.get("order")
        if (order == null || obj.get("order").isJsonNull) {
            return
        }

        val myOrder = MyOrder(order.asJsonObject)

        if (myOrder.status != MyOrder.Status.COMPLETED) {
            return
        }

        val review = order.asJsonObject.get("rating") ?: return

        laundryId = AndroidUtilities.getIntFieldFromJson(order.asJsonObject.get("laundry").asJsonObject.get("id"))
        if (review.isJsonNull) {
            showReviewDialog(AndroidUtilities.getIntFieldFromJson(order.asJsonObject.get("id")))
        }
    }

    private fun sendReview() {
        val dialog = ProgressDialog(this)
        dialog.show()
        ServerApi.get(this).api().sendReview(laundryId, generateJson(), DeviceInfoStore.getToken(this)).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>) {
                dialog.dismiss()
                Animations.animateRevealHide(findViewById(R.id.dialog))
                if (response.isSuccessful) {
                    parseReviewResponse()
                } else {
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                dialog.dismiss()
                Animations.animateRevealHide(findViewById(R.id.dialog))
                onInternetConnectionError()
            }
        })
    }

    private fun showErrorDialog() {
        MaterialDialog.Builder(this)
                .title(getString(R.string.error))
                .content(getString(R.string.wrong_review_code))
                .positiveText(R.string.ok_code)
                .onPositive { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun checkReview(): Boolean {
        return (findViewById(R.id.ratingBar) as SimpleRatingBar).rating.toInt() != 0
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
        Animations.animateRevealHide(findViewById(R.id.dialog))
    }

    private fun showReviewDialog(id: Int) {
        Handler().post {
            dialogOpened = true
            (findViewById(R.id.order_name_completed) as TextView).text =
                    getString(R.string.order) + " № " + id.toString() + getString(R.string.completed)
            Animations.animateRevealShow(findViewById(R.id.dialog), this@OrdersActivity)
        }
    }

    private fun initViewElements() {
        val view = findViewById(R.id.recyclerView) as RecyclerListView
        view.layoutManager = LinearLayoutManager(this)
        view.itemAnimator = DefaultItemAnimator()
        view.setHasFixedSize(true)
        view.emptyView = findViewById(R.id.empty_orders)

        adapter = OrdersAdapter(this)
        itemColor = intent.getStringExtra(EXTRA_COLOR)
        adapter.setItemColor(itemColor)
        view.adapter = adapter
        adapter.onItemSelectAction.subscribe { isSelected -> showSelection(isSelected) }

        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        titleText = findViewById(R.id.date_text_view) as TextView
    }

    private fun showSelection(selected: Boolean) {
        setToolbarTitle(selected)
        removeBtn.isVisible = selected
        update()
    }

    private fun setToolbarTitle(selected: Boolean) {
        if (selected) {
            titleText.text = getString(string.select_order)
        } else {
            titleText.text = getString(string.order)
        }
    }

    private fun openActivity(activity: Class<out Activity>) {
        val intent = Intent(this@OrdersActivity, activity)
        startActivity(intent)
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener {
            finish()
        }

        findViewById(R.id.add_btn).setOnClickListener {
            openActivity(CategoryActivity::class.java)
        }

        findViewById(R.id.cont_btn).setOnClickListener {
            if (!checkReview()) {
                showErrorDialog()
            } else {
                sendReview()
            }
        }

        findViewById(R.id.new_order_dialog_btn).setOnClickListener {
            Animations.animateRevealHide(findViewById(R.id.dialog_new_order))
            getUser()
        }

        addItemButton = findViewById(R.id.add_item) as FloatingActionButton
        addItemButton.setOnClickListener { openActivity(CategoryActivity::class.java) }
    }

    override fun onBackPressed() {
        if (dialogOpened) {
            return
        }
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    private fun update() {
        if (OrderList.get() == null) {
            return
        }

        adapter.setCollection(OrderList.get()!!)
        adapter.notifyDataSetChanged()

        if (adapter.itemCount != 0) {
            addItemButton.visibility = View.VISIBLE
            setContinueButtonEnabled()
        } else {
            addItemButton.visibility = View.GONE
            setContinueButtonDisabled()
        }
    }

    private fun setContinueButtonEnabled() {
        continueBtn.setText(R.string.continue_btn)
        continueBtn.setOnClickListener {
            LaundriesActivity.longTreatment = adapter!!.hasItemsWithLongTreatment()
            val intent = Intent(this@OrdersActivity, LaundriesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setContinueButtonDisabled() {
        continueBtn.setText(R.string.nothing_selected)
        continueBtn.isEnabled = false
        continueBtn.setOnClickListener(null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_order, menu)

        removeBtn = menu.findItem(R.id.action_remove)
        removeBtn.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_remove -> {
                adapter.removeSelectedOrders()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        var laundryId = 0
        var newOrderId = 0
    }
}
