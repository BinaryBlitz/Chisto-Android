package ru.binaryblitz.Chisto.ui.categories

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.crashlytics.android.Crashlytics
import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import io.fabric.sdk.android.Fabric
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.ui.about.AboutActivity
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.categories.adapters.CategoriesAdapter
import ru.binaryblitz.Chisto.ui.categories.adapters.CategoryItemsAdapter
import ru.binaryblitz.Chisto.ui.order.OrdersActivity
import ru.binaryblitz.Chisto.ui.order.WebActivity
import ru.binaryblitz.Chisto.ui.profile.ContactInfoActivity
import ru.binaryblitz.Chisto.utils.AppConfig
import ru.binaryblitz.Chisto.utils.ColorsList
import ru.binaryblitz.Chisto.views.RecyclerListView
import java.util.*

class CategoryActivity : BaseActivity(), CategoryView {
    val EXTRA_URL = "url"
    val EXTRA_ID = "id"
    private var color: String = ""
    private var id: Int = 0

    lateinit var categoryPresenter: CategoryPresenterImpl
    private lateinit var categoryAdapter: CategoriesAdapter
    private lateinit var categoryInfoAdapter: CategoryItemsAdapter
    private lateinit var allItemsAdapter: CategoryItemsAdapter
    private lateinit var allItemsList: ArrayList<CategoryItem>

    private lateinit var searchView: MaterialSearchView
    private lateinit var categoriesListView: RecyclerListView
    private lateinit var categoryItemsListView: RecyclerListView

    private lateinit var toolbar: Toolbar
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_select_category)

        initToolbar()
        initDrawer(toolbar, this)
        initList()
        initSearchView()

        categoryPresenter =
                CategoryPresenterImpl(this,
                        CategoryInteractorImpl(ServerApi.get(this).api()), this)
        categoryPresenter.setView(this)
        categoryPresenter.getCategories()
    }

    override fun showProgress() {
        dialog.show()
    }

    override fun hideProgress() {
        dialog.dismiss()
    }

    override fun showError(appErrorMessage: String?) {
        onInternetConnectionError()
    }

    override fun showCategories(categories: List<Category>) {
        save(categories)
        categoryAdapter.setCategories(categories as ArrayList<Category>)
        categoryAdapter.notifyDataSetChanged()
        color = categories[0].color
        categoryInfoAdapter.setColor(color)
        categoryPresenter.getCategoriesItems(categories[0].id)
    }

    override fun showCategoryInfo(categoryItems: List<CategoryItem>) {
        categoryInfoAdapter.setCategories(categoryItems)
        categoryInfoAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        if (searchView == null) {
            return
        }
        if (searchView.isSearchOpen) {
            searchView.closeSearch()
        } else {
            finish()
        }
    }


    private fun searchForItems(query: String) {
        if (allItemsAdapter?.getCategories() == null) {
            return
        }

        val foundItems = allItemsAdapter?.getCategories()!!.filter { nameEqualsTo(it, query) }
        allItemsAdapter?.setCategories(foundItems)
        allItemsAdapter?.notifyDataSetChanged()
    }

    private fun nameEqualsTo(item: CategoryItem, query: String): Boolean {
        return item.name.toLowerCase().contains(query)
    }

    private fun initToolbar() {
        toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = getString(R.string.select_part)
        setSupportActionBar(toolbar)
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
                categoryPresenter.getAllItems()
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.action_my_orders -> {openActivity(OrdersActivity::class.java)}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initList() {
        dialog = ProgressDialog(this)
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
            categoryInfoAdapter.setColor(color)
            if (id == 0)
                categoryPresenter.getAllItems()
            else
                categoryPresenter.getCategoriesItems(id)

        }
        categoriesListView.adapter = categoryAdapter
        categoryItemsListView.adapter = categoryInfoAdapter
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
                        itemMain -> openActivity(CategoryActivity::class.java)
                        itemContactData -> openActivity(ContactInfoActivity::class.java)
                        itemOrders -> openActivity(OrdersActivity::class.java)
                        itemAbout -> openActivity(AboutActivity::class.java)
                        itemRules -> {
                            val intent = Intent(this@CategoryActivity, WebActivity::class.java)
                            intent.putExtra(EXTRA_URL, AppConfig.terms)
                            startActivity(intent)
                        }
                    }
                    false
                }
                .build()

    }

    private fun openActivity(activity: Class<out Activity>) {
        val intent = Intent(this@CategoryActivity, activity)
        startActivity(intent)
    }


    private fun save(collection: List<Category>) {
        for (i in collection.indices) {
            val (id, icon, name, description, color) = collection[i]
            ColorsList.add(Pair(id, Color.parseColor(color)))
        }

        ColorsList.saveColors(this)
    }

}
