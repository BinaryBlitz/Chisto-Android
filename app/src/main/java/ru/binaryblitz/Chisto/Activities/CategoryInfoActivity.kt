package ru.binaryblitz.Chisto.Activities

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonArray
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Adapters.CategoryItemsAdapter
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Custom.RecyclerListView
import ru.binaryblitz.Chisto.Model.CategoryItem
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Server.ServerConfig
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import java.util.*

class CategoryInfoActivity : BaseActivity() {
    private var adapter: CategoryItemsAdapter? = null
    private var layout: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_category_info)

        findViewById(ru.binaryblitz.Chisto.R.id.toolbar).setBackgroundColor(intent.getIntExtra("color", Color.parseColor("#212121")))
        AndroidUtilities.colorAndroidBar(this, intent.getIntExtra("color", Color.parseColor("#212121")))

        findViewById(R.id.left_btn).setOnClickListener { finish() }

        initList()

        Handler().post {
            layout!!.isRefreshing = true
            load()
        }
    }

    private fun initList() {
        val view = findViewById(ru.binaryblitz.Chisto.R.id.recyclerView) as RecyclerListView
        view.layoutManager = LinearLayoutManager(this)
        view.itemAnimator = DefaultItemAnimator()
        view.setHasFixedSize(true)
        adapter = CategoryItemsAdapter(this)
        view.adapter = adapter

        layout = findViewById(R.id.refresh) as SwipeRefreshLayout
        layout!!.setOnRefreshListener(null)
        layout!!.isEnabled = false
        layout!!.setColorSchemeResources(R.color.colorAccent)

        adapter!!.setColor(intent.getIntExtra("color", Color.parseColor("#212121")))
    }

    private fun load() {
        ServerApi.get(this).api().getItems(intent.getIntExtra("id", 0)).enqueue(object : Callback<JsonArray> {
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
        val collection = (0..array.size() - 1)
                .map { array.get(it).asJsonObject }
                .mapTo(ArrayList<CategoryItem>()) {
                    CategoryItem(
                            AndroidUtilities.getIntFieldFromJson(it.get("id")),
                            ServerConfig.imageUrl + AndroidUtilities.getStringFieldFromJson(it.get("icon_url")),
                            AndroidUtilities.getStringFieldFromJson(it.get("name")),
                            AndroidUtilities.getStringFieldFromJson(it.get("description")),
                            AndroidUtilities.getBooleanFieldFromJson(it.get("use_area"))
                    )
                }

        sort(collection)

        adapter!!.setCategories(collection)
        adapter!!.notifyDataSetChanged()
    }

    private fun sort(collection: ArrayList<CategoryItem>) {
        Collections.sort(collection) { categoryItem, t1 -> categoryItem.name.compareTo(t1.name) }
    }
}
