package ru.binaryblitz.Chisto.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
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
import ru.binaryblitz.Chisto.Utils.OrderList
import java.util.*

class SelectServiceActivity : BaseActivity() {

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

        findViewById(R.id.left_btn).setOnClickListener {
            finishActivity()
        }

        findViewById(R.id.cont_btn).setOnClickListener {
            openActivity()
        }

        initList()

        Handler().post({
            layout!!.isRefreshing = true
            load()
        })
    }

    override fun onBackPressed() {
        finishActivity()
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
            OrderList.addTreatments(adapter!!.getSelected())
            OrderList.changeColor(intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))
            if (!intent.getBooleanExtra(EXTRA_EDIT, false)) {
                val intent = Intent(this@SelectServiceActivity, OrdersActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
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
                intent.getBooleanExtra(EXTRA_DECOR, false)))

        adapter!!.notifyDataSetChanged()
    }
}
