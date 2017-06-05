package ru.binaryblitz.Chisto.ui.categories

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.util.Pair
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
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import io.fabric.sdk.android.Fabric
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.network.ServerConfig
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.about.AboutActivity
import ru.binaryblitz.Chisto.ui.categories.adapters.CategoriesAdapter
import ru.binaryblitz.Chisto.ui.categories.adapters.CategoryItemsAdapter
import ru.binaryblitz.Chisto.ui.order.OrdersActivity
import ru.binaryblitz.Chisto.ui.order.WebActivity
import ru.binaryblitz.Chisto.ui.profile.ContactInfoActivity
import ru.binaryblitz.Chisto.utils.AndroidUtilities
import ru.binaryblitz.Chisto.utils.AppConfig
import ru.binaryblitz.Chisto.utils.ColorsList
import ru.binaryblitz.Chisto.utils.LogUtil
import ru.binaryblitz.Chisto.views.RecyclerListView
import java.util.*

class SelectCategoryActivity : BaseActivity() {
    val EXTRA_URL = "url"
    val EXTRA_ID = "id"
    private var color: Int = 0
    private var id: Int = 0

    private lateinit var categoryAdapter: CategoriesAdapter
    private lateinit var categoryInfoAdapter: CategoryItemsAdapter
    private var allItemsAdapter: CategoryItemsAdapter? = null
    private var allItemsList: ArrayList<CategoryItem>? = null

    private lateinit var searchView: MaterialSearchView
    private lateinit var categoriesListView: RecyclerListView
    private lateinit var categoryItemsListView: RecyclerListView

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_select_category)

        initToolbar()
        initDrawer(toolbar, this)
        setOnCLickListeners()
        initList()
        initSearchView()

        load()
    }

    private fun getAllItems(forSearch: Boolean) {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().allItems.enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    parseAllItems(response.body(), forSearch)
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                dialog.dismiss()
            }
        })
    }

    private fun getCategoriesInfo() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().getItems(id).enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                if (response.isSuccessful) {
                    dialog.dismiss()
                    parseItems(response.body())
                } else {
                    dialog.dismiss()
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                onInternetConnectionError()
            }
        })
    }

    private fun searchForItems(query: String) {
        if (allItemsAdapter?.getCategories() == null) {
            return
        }

        val foundItems = allItemsAdapter?.getCategories()!!.filter { nameEqualsTo(it, query) }
        allItemsAdapter?.setCategories(foundItems)
        allItemsAdapter?.notifyDataSetChanged()
    }


    private fun parseAllItems(array: JsonArray, forSearch: Boolean) {
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
                            ColorsList.findColor(AndroidUtilities.getIntFieldFromJson(it.get("category_id"))),
                            AndroidUtilities.getBooleanFieldFromJson(it.get("long_treatment"))
                    )
                }

        if (forSearch) {
            allItemsAdapter = CategoryItemsAdapter(this@SelectCategoryActivity)
            categoriesListView.adapter = allItemsAdapter
            allItemsAdapter!!.setCategories(allItemsList!!)
            allItemsAdapter!!.notifyDataSetChanged()
        } else {
            categoryInfoAdapter.setCategories(allItemsList!!)
            categoryInfoAdapter.notifyDataSetChanged()
        }

        sortAllItems(allItemsList!!)

    }

    private fun sortAllItems(collection: ArrayList<CategoryItem>) {
        Collections.sort(collection) { categoryItem, t1 -> categoryItem.name.compareTo(t1.name) }
    }


    private fun nameEqualsTo(item: CategoryItem, query: String): Boolean {
        return item.name.toLowerCase().contains(query)
    }

    private fun setOnCLickListeners() {
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = getString(R.string.select_part)
        setSupportActionBar(toolbar)
    }

    override fun onBackPressed() {
        if (searchView == null) {
            return
        }
        if (searchView.isSearchOpen) {
            searchView.closeSearch()
        } else {
            finishActivity()
        }
    }

    private fun finishActivity() {
        finish()
    }

    private fun initSearchView() {
        searchView = findViewById(R.id.search_view) as MaterialSearchView
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
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

        searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {
                getAllItems(true)
            }

            override fun onSearchViewClosed() {
                categoriesListView.adapter = categoryAdapter
            }
        })

        searchView.setVoiceSearch(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val item = menu.findItem(R.id.action_search)
        searchView.setMenuItem(item)

        return true
    }

    private fun initList() {
        categoriesListView = findViewById(R.id.recyclerView) as RecyclerListView
        categoryItemsListView = findViewById(R.id.categoriesItems) as RecyclerListView

        categoriesListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoriesListView.setHasFixedSize(true)

        categoryItemsListView.layoutManager = LinearLayoutManager(this)
        categoriesListView.setHasFixedSize(true)

        categoryAdapter = CategoriesAdapter(this)
        categoryInfoAdapter = CategoryItemsAdapter(this)
        categoryAdapter.onCategoryClickAction.subscribe { category ->
            color = category.color
            id = category.id
            if (id == 0)
                getAllItems(false)
            else
                getCategoriesInfo()

        }
        categoriesListView.adapter = categoryAdapter
        categoryItemsListView.adapter = categoryInfoAdapter
    }

    private fun load() {
        val dialog = ProgressDialog(this)
        dialog.show()

        ServerApi.get(this).api().categories.enqueue(object : Callback<JsonArray> {
            override fun onResponse(call: Call<JsonArray>, response: Response<JsonArray>) {
                dialog.dismiss()
                if (response.isSuccessful) {
                    parseAnswer(response.body())
                } else {
                    onServerError(response)
                }
            }

            override fun onFailure(call: Call<JsonArray>, t: Throwable) {
                dialog.dismiss()
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
        sortCategories(collection)

        collection.add(0, Category(0,
                "https://chisto-staging.s3.amazonaws.com/uploads/category/icon/2/2b192e163d524db18d3b0d3abfdd3a2d.png",
                getString(R.string.all_categories),
                SpannableString("empty"), R.color.greyColor, false))

        save(collection)

        categoryAdapter.setCategories(collection)
        categoryAdapter.notifyDataSetChanged()
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

    private fun sortCategories(collection: ArrayList<Category>) {
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

    fun initDrawer(toolbar: android.support.v7.widget.Toolbar, activity: Activity) {
        val itemMain = PrimaryDrawerItem().withName(getString(R.string.select_part)).withIcon(R.drawable.ic_app_mini_logo)
        val itemContactData = PrimaryDrawerItem().withName(getString(R.string.contact_data)).withIcon(R.drawable.ic_user_blue)
        val itemOrders = PrimaryDrawerItem().withName(getString(R.string.my_orders)).withIcon(R.drawable.ic_my_orders)
        val itemAbout = PrimaryDrawerItem().withName(getString(R.string.about)).withIcon(R.drawable.ic_app_mini_logo)
        val itemRules = PrimaryDrawerItem().withName(getString(R.string.rules)).withIcon(R.drawable.ic_help)

        val accountHeader: AccountHeader = AccountHeaderBuilder()
                .withActivity(this)
                //TODO Add image with Chisto logo in primary color background
                .withHeaderBackground(R.color.primary)
                .build()

        val result = DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(false)
                .withTranslucentStatusBar(false)
                .withAccountHeader(accountHeader)
                .withSelectedItem(-1)
                .addDrawerItems(
                        itemMain,
                        itemContactData,
                        itemOrders,
                        DividerDrawerItem(),
                        itemAbout,
                        itemRules
                )
                .withOnDrawerItemClickListener { view, position, drawerItem ->
                    when (drawerItem) {
                        itemMain -> openActivity(SelectCategoryActivity::class.java)
                        itemContactData -> openActivity(ContactInfoActivity::class.java)
                        itemOrders -> openActivity(OrdersActivity::class.java)
                        itemAbout -> openActivity(AboutActivity::class.java)
                        itemRules -> {
                            val intent = Intent(this@SelectCategoryActivity, WebActivity::class.java)
                            intent.putExtra(EXTRA_URL, AppConfig.terms)
                            startActivity(intent)
                        }
                    }
                    false
                }
                .build()

    }

    private fun openActivity(activity: Class<out Activity>) {
        val intent = Intent(this@SelectCategoryActivity, activity)
        startActivity(intent)
    }


    private fun parseItems(array: JsonArray) {
        LogUtil.logError(array.toString())
        val collection = (0..array.size() - 1)
                .map { array.get(it).asJsonObject }
                .mapTo(ArrayList<CategoryItem>()) {
                    CategoryItem(
                            AndroidUtilities.getIntFieldFromJson(it.get("id")),
                            ServerConfig.imageUrl + AndroidUtilities.getStringFieldFromJson(it.get("icon_url")),
                            AndroidUtilities.getStringFieldFromJson(it.get("name")),
                            AndroidUtilities.getStringFieldFromJson(it.get("description")),
                            AndroidUtilities.getBooleanFieldFromJson(it.get("use_area")),
                            color,
                            AndroidUtilities.getBooleanFieldFromJson(it.get("long_treatment"))
                    )
                }

        sortCategoryItems(collection)

        categoryInfoAdapter.setCategories(collection)
        categoryInfoAdapter.notifyDataSetChanged()
    }

    private fun sortCategoryItems(collection: ArrayList<CategoryItem>) {
        Collections.sort(collection) { categoryItem, t1 -> categoryItem.name.compareTo(t1.name) }
    }

}
