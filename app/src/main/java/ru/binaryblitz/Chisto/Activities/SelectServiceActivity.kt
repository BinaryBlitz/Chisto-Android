package ru.binaryblitz.Chisto.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonArray
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Adapters.TreatmentsAdapter
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Custom.RecyclerListView
import ru.binaryblitz.Chisto.Model.Treatment
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import ru.binaryblitz.Chisto.Utils.Animations.Animations
import ru.binaryblitz.Chisto.Utils.LogUtil
import ru.binaryblitz.Chisto.Utils.OrderList
import java.util.*

class SelectServiceActivity : BaseActivity() {

    private var adapter: TreatmentsAdapter? = null
    private var layout: SwipeRefreshLayout? = null

    val EXTRA_COLOR = "color"
    val EXTRA_DECORATION = "decoration"
    val EXTRA_EDIT = "edit"
    val EXTRA_ID = "id"
    val EXTRA_NAME = "name"
    val EXTRA_USE_AREA = "userArea"

    private var width: Int = 0
    private var length: Int = 0
    private var dialogOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_select_service)

        initElements()
        setOnClickListeners()
        initSizeDialog()
        initList()

        Handler().post({
            layout!!.isRefreshing = true
            load()
        })
    }

    private fun initElements() {
        findViewById(R.id.appbar).setBackgroundColor(intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))
        AndroidUtilities.colorAndroidBar(this, intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))
        (findViewById(R.id.main_title) as TextView).text = intent.getStringExtra(EXTRA_NAME)
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener {
            finishActivity()
        }

        findViewById(R.id.cont_btn).setOnClickListener {
            if (intent.getBooleanExtra(EXTRA_USE_AREA, false)) showSizeDialog()
            else openActivity()
        }

        findViewById(R.id.size_ok_btn).setOnClickListener {
            Animations.animateRevealHide(findViewById(ru.binaryblitz.Chisto.R.id.dialog))
            openActivity()
        }

        findViewById(R.id.cancel_btn).setOnClickListener {
            Animations.animateRevealHide(findViewById(ru.binaryblitz.Chisto.R.id.dialog))
        }
    }

    override fun onBackPressed() {
        finishActivity()
    }

    private fun initSizeDialog() {
        val lengthEditText = findViewById(R.id.length_text) as EditText
        val widthEditText = findViewById(R.id.width_text) as EditText
        val square = findViewById(R.id.square_text) as TextView

        lengthEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun afterTextChanged(editable: Editable) {
                recomputeSquare(false, editable, square)
            }
        })

        widthEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) { }

            override fun afterTextChanged(editable: Editable) {
                recomputeSquare(true, editable, square)
            }
        })
    }

    private fun showSizeDialog() {
        Handler().post {
            dialogOpened = true
            Animations.animateRevealShow(findViewById(ru.binaryblitz.Chisto.R.id.dialog), this@SelectServiceActivity)
        }
    }

    private fun recomputeSquare(width: Boolean, editable: Editable, square: TextView) {
        try {
            if (editable.isNotEmpty() && editable.toString() != "0") {
                if (width) this.width = Integer.parseInt(editable.toString()) else length = Integer.parseInt(editable.toString())
                square.text = java.lang.Double.toString((length * this.width).toDouble() / 1000.0) + " см"
            }
        } catch (e: Exception) {
            LogUtil.logException(e)
        }
    }

    private fun finishActivity() {
        if (!intent.getBooleanExtra(EXTRA_EDIT, false)) OrderList.removeCurrent()
        finish()
    }

    private fun initList() {
        val view = findViewById(R.id.recyclerView) as RecyclerListView
        view.layoutManager = LinearLayoutManager(this)
        view.itemAnimator = DefaultItemAnimator()
        view.setHasFixedSize(true)

        layout = findViewById(R.id.refresh) as SwipeRefreshLayout
        layout!!.setOnRefreshListener(null)
        layout!!.isEnabled = false
        layout!!.setColorSchemeResources(R.color.colorAccent)

        adapter = TreatmentsAdapter(this)
        adapter!!.setColor(intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))
        view.adapter = adapter
    }

    private fun openActivity() {
        if (adapter!!.getSelected().size != 0) {
            OrderList.changeColor(intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))
            if (!intent.getBooleanExtra(EXTRA_EDIT, false)) {
                OrderList.addTreatments(adapter!!.getSelected())
                val intent = Intent(this@SelectServiceActivity, OrdersActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                OrderList.addTreatmentsForEditing(adapter!!.getSelected())
            }

            finish()
        } else {
            Snackbar.make(findViewById(R.id.main), R.string.nothing_selected_code, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun load() {
        ServerApi.get(this).api().getTreatments(intent.getIntExtra(EXTRA_ID, 0)).enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                layout!!.isRefreshing = false
                if (response.isSuccessful) parseAnswer(response.body())
                else onInternetConnectionError()
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                layout!!.isRefreshing = false
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(array: JsonArray) {
        val collection = ArrayList<Treatment>()

        (0..array.size() - 1)
                .map { array.get(it).asJsonObject }
                .mapTo(collection) {
                    Treatment(
                            AndroidUtilities.getIntFieldFromJson(it.get("id")),
                            AndroidUtilities.getStringFieldFromJson(it.get("name")),
                            AndroidUtilities.getStringFieldFromJson(it.get("description")),
                            0,
                            false)
                }

        adapter!!.setCollection(collection)
        adapter!!.notifyDataSetChanged()

        // TODO change this
        adapter!!.add(Treatment(
                1,
                "Декор",
                "Описание",
                100,
                intent.getBooleanExtra(EXTRA_DECORATION, false)))

        adapter!!.notifyDataSetChanged()
    }
}
