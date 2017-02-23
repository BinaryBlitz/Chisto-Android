package ru.binaryblitz.Chisto.Activities

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.util.Pair
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.Menu
import com.crashlytics.android.Crashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.miguelcatalan.materialsearchview.MaterialSearchView
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.Adapters.CategoriesAdapter
import ru.binaryblitz.Chisto.Adapters.CategoryItemsAdapter
import ru.binaryblitz.Chisto.Base.BaseActivity
import ru.binaryblitz.Chisto.Custom.RecyclerListView
import ru.binaryblitz.Chisto.Model.Category
import ru.binaryblitz.Chisto.Model.CategoryItem
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.ServerApi
import ru.binaryblitz.Chisto.Server.ServerConfig
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import ru.binaryblitz.Chisto.Utils.ColorsList
import java.util.*

class SelectCategoryActivity : BaseActivity() {
    private var adapter: CategoriesAdapter? = null
    private var allItemsAdapter: CategoryItemsAdapter? = null
    private var layout: SwipeRefreshLayout? = null
    private var searchView: MaterialSearchView? = null
    private var listView: RecyclerListView? = null
    private var allItemsList: ArrayList<CategoryItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_select_category)

        initToolbar()
        setOnCLickListeners()
        initList()
        initSearchView()

        Handler().post {
            layout!!.isRefreshing = true
            load()
        }
    }

    private fun nameEqualsTo(item: CategoryItem, query: String): Boolean {
        return item.name.toLowerCase().contains(query)
    }

    private fun searchForItems(query: String) {
        if (allItemsAdapter?.getCategories() == null) {
            return
        }

        val foundItems = allItemsAdapter?.getCategories()!!.filter { nameEqualsTo(it, query) }
        allItemsAdapter?.setCategories(foundItems)
        allItemsAdapter?.notifyDataSetChanged()
    }

    private fun getAllItems() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().allItems.enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    parseAllItems(response.body())
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                dialog.dismiss()
            }
        })
    }

    private fun parseAllItems(array: JsonArray) {
        ColorsList.load(this)

        allItemsList = (0..array.size() - 1)
                .map { array.get(it).asJsonObject }
                .mapTo(ArrayList<CategoryItem>()) {
                    CategoryItem(
                            AndroidUtilities.getIntFieldFromJson(it.get("id")),
                            ServerConfig.imageUrl + AndroidUtilities.getStringFieldFromJson(it.get("icon_url")),
                            AndroidUtilities.getStringFieldFromJson(it.get("name")),
                            AndroidUtilities.getStringFieldFromJson(it.get("description")),
                            AndroidUtilities.getBooleanFieldFromJson(it.get("use_area")),
                            ColorsList.findColor(AndroidUtilities.getIntFieldFromJson(it.get("category_id")))
                    )
                }

        sortAllItems(allItemsList!!)

        allItemsAdapter = CategoryItemsAdapter(this)
        listView?.adapter = allItemsAdapter

        allItemsAdapter!!.setCategories(allItemsList!!)
        allItemsAdapter!!.notifyDataSetChanged()
    }

    private fun sortAllItems(collection: ArrayList<CategoryItem>) {
        Collections.sort(collection) { categoryItem, t1 -> categoryItem.name.compareTo(t1.name) }
    }

    private fun setOnCLickListeners() {
        findViewById(R.id.left_btn).setOnClickListener { finishActivity() }
    }

    private fun initToolbar() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = null
    }

    override fun onBackPressed() {
        if (searchView!!.isSearchOpen) {
            searchView!!.closeSearch()
        } else {
            finishActivity()
        }
    }

    private fun finishActivity() {
        finish()
    }

    private fun initSearchView() {
        searchView = findViewById(R.id.search_view) as MaterialSearchView
        searchView!!.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty() && allItemsList != null) {
                    allItemsAdapter?.setCategories(allItemsList!!)
                    allItemsAdapter?.notifyDataSetChanged()
                    return false
                }

                searchForItems(newText)
                return false
            }
        })

        searchView!!.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {
                getAllItems()
            }

            override fun onSearchViewClosed() {
                listView?.adapter = adapter
            }
        })

        searchView!!.setVoiceSearch(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val item = menu.findItem(R.id.action_search)
        searchView!!.setMenuItem(item)

        return true
    }

    private fun initList() {
        listView = findViewById(R.id.recyclerView) as RecyclerListView
        listView?.layoutManager = LinearLayoutManager(this)
        listView?.itemAnimator = DefaultItemAnimator()
        listView?.setHasFixedSize(true)

        layout = findViewById(R.id.refresh) as SwipeRefreshLayout
        layout?.setOnRefreshListener(null)
        layout?.isEnabled = false
        layout?.setColorSchemeResources(R.color.colorAccent)

        adapter = CategoriesAdapter(this)
        listView?.adapter = adapter
    }

    private fun load() {
        ServerApi.get(this).api().categories.enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                layout!!.isRefreshing = false
                if (response.isSuccessful) {
                    parseAnswer(response.body())
                } else {
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                layout!!.isRefreshing = false
                onInternetConnectionError()
            }
        })
    }

    private fun parseAnswer(array: JsonArray) {
        val collection = ArrayList<Category>()

        (0..array.size() - 1)
                .map { array.get(it).asJsonObject }
                .forEach {
                    if (AndroidUtilities.getBooleanFieldFromJson(it.get("featured"))) {
                        collection.add(0, parseCategory(it))
                    } else {
                        collection.add(parseCategory(it))
                    }
                }

        sort(collection)
        save(collection)

        adapter?.setCategories(collection)
        adapter?.notifyDataSetChanged()
    }

    private fun save(collection: ArrayList<Category>) {
        for (i in collection.indices) {
            val (id, icon, name, description, color) = collection[i]
            ColorsList.add(Pair(id, color))
        }

        ColorsList.saveColors(this)
    }

    private fun parseCategory(obj: JsonObject): Category {
        return Category(
                AndroidUtilities.getIntFieldFromJson(obj.get("id")),
                ServerConfig.imageUrl + AndroidUtilities.getStringFieldFromJson(obj.get("icon_url")),
                AndroidUtilities.getStringFieldFromJson(obj.get("name")),
                generateDesсription(obj),
                Color.parseColor(AndroidUtilities.getStringFieldFromJson(obj.get("color"))),
                AndroidUtilities.getBooleanFieldFromJson(obj.get("featured"))
        )
    }

    private fun generateDesсription(obj: JsonObject): Spannable {
        val itemsCount = AndroidUtilities.getIntFieldFromJson(obj.get("items_count"))
        val description: String
        val preview = HashSet<String>()
        val array = obj.get("items_preview").asJsonArray

        description = generateStartOfDescription(array, preview)
        return generateEndOfDescription(itemsCount, description.length, description, preview)
    }

    private fun generateStartOfDescription(array: JsonArray, preview: MutableSet<String>): String {
        for (i in 0..array.size() - 1) {
            var item = array.get(i).asString
            item = item.trim { it <= ' ' }
            preview.add(item.split(" или ".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()[0])
        }
        return TextUtils.join(" \u2022 ", preview)
    }

    private fun generateEndOfDescription(itemsCount: Int, start: Int, description: String, preview: Set<String>): Spannable {
        var description = description
        if (itemsCount > preview.size) {
            val pluralText = resources.getQuantityString(R.plurals.items,
                    itemsCount - preview.size, itemsCount - preview.size)
            description += getString(R.string.splitter_code) + pluralText
        }

        val span = SpannableString(description)
        span.setSpan(ForegroundColorSpan(Color.parseColor("#b6b6b6")), start, description.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return span
    }

    private fun sort(collection: ArrayList<Category>) {
        Collections.sort(collection) { category, t ->
            if (category.featured && !t.featured) {
                -1
            } else if (!category.featured && t.featured) {
                1
            } else {
                category.name.compareTo(t.name)
            }
        }
    }
}
