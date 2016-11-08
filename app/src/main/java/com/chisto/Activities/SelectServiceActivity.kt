package com.chisto.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import com.chisto.Adapters.TreatmentsAdapter
import com.chisto.Base.BaseActivity
import com.chisto.Custom.RecyclerListView
import com.chisto.Model.Treatment
import com.chisto.R
import com.chisto.Server.ServerApi
import com.chisto.Utils.AndroidUtilities
import com.chisto.Utils.LogUtil
import com.chisto.Utils.OrderList
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonArray
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class SelectServiceActivity : BaseActivity(), SwipeRefreshLayout.OnRefreshListener {

    private var adapter: TreatmentsAdapter? = null
    private var layout: SwipeRefreshLayout? = null

    val EXTRA_COLOR = "color"
    val EXTRA_DECOR = "decor"
    val EXTRA_EDIT = "edit"
    val EXTRA_ID = "id"
    val EXTRA_NAME = "name"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_select_service)

        findViewById(R.id.appbar).setBackgroundColor(intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))
        AndroidUtilities.colorAndroidBar(this, intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))
        (findViewById(R.id.main_title) as TextView).text = intent.getStringExtra(EXTRA_NAME)

        findViewById(R.id.left_btn).setOnClickListener { finish() }

        findViewById(R.id.cont_btn).setOnClickListener {
            openActivity()
        }

        initList()

        Handler().postDelayed({ load() }, 200)
    }

    override fun onRefresh() {
        load()
    }

    private fun initList() {
        val view = findViewById(R.id.recyclerView) as RecyclerListView
        view.layoutManager = LinearLayoutManager(this)
        view.itemAnimator = DefaultItemAnimator()
        view.setHasFixedSize(true)

        adapter = TreatmentsAdapter(this)
        adapter!!.setColor(intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))
        view.adapter = adapter

        layout = findViewById(R.id.refresh) as SwipeRefreshLayout
        layout!!.setOnRefreshListener(this)
        layout!!.setColorSchemeResources(R.color.colorAccent)
    }

    private fun openActivity() {
        if (adapter!!.getSelected().size != 0) {
            OrderList.addTreatments(adapter!!.getSelected())
            OrderList.changeColor(intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))
            if (!intent.getBooleanExtra(EXTRA_EDIT, false)) {
                val intent = Intent(this@SelectServiceActivity, OrdersActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

            finish()
        } else {
            Snackbar.make(findViewById(R.id.main), R.string.nothing_selected_code_str, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun load() {
        ServerApi.get(this).api().getTreatments(intent.getIntExtra(EXTRA_ID, 0)).enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                LogUtil.logError(response.body().toString())
                layout!!.isRefreshing = false
                if (response.isSuccessful) {
                    parseAnswer(response.body())
                } else {
                    onInternetConnectionError()
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                layout!!.isRefreshing = false
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(array: JsonArray) {
        val collection = ArrayList<Treatment>()

        // TODO change this
        collection.add(Treatment(
                1,
                "Декор",
                "Описание",
                intent.getBooleanExtra(EXTRA_DECOR, false)))

        for (i in 0..array.size() - 1) {
            val obj = array.get(i).asJsonObject
            collection.add(Treatment(
                    obj.get("id").asInt,
                    obj.get("name").asString,
                    obj.get("description").asString,
                    false))
        }

        main_loop@ for (i in collection.indices) {
            for (j in 0..OrderList.getTreatments()!!.size - 1) {
                if (collection[i].id == OrderList.getTreatments()!![j].id) {
                    collection[i].select = true
                    continue@main_loop
                }
            }
        }

        adapter!!.setCollection(collection)
        adapter!!.notifyDataSetChanged()
    }
}
