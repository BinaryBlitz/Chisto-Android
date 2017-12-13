package ru.binaryblitz.Chisto.ui.categories

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.util.Pair
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.crashlytics.android.Crashlytics
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_select_category.*
import kotlinx.android.synthetic.main.toolbar_cart_icon.*
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.ResourceManager
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.extension.clear
import ru.binaryblitz.Chisto.extension.hideKeyboard
import ru.binaryblitz.Chisto.extension.visible
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.ui.about.AboutActivity
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.categories.adapters.CategoriesAdapter
import ru.binaryblitz.Chisto.ui.categories.adapters.CategoryItemsAdapter
import ru.binaryblitz.Chisto.ui.order.MyOrdersActivity
import ru.binaryblitz.Chisto.ui.order.OrdersActivity
import ru.binaryblitz.Chisto.ui.order.WebActivity
import ru.binaryblitz.Chisto.ui.profile.RegistrationActivity
import ru.binaryblitz.Chisto.utils.AppConfig
import ru.binaryblitz.Chisto.utils.ColorsList
import ru.binaryblitz.Chisto.utils.Extras
import ru.binaryblitz.Chisto.utils.OrderList
import ru.binaryblitz.Chisto.views.RecyclerListView
import timber.log.Timber
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

    private lateinit var categoriesListView: RecyclerListView
    private lateinit var categoryItemsListView: RecyclerListView

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
                CategoryPresenterImpl(
                        this,
                        CategoryInteractorImpl(ServerApi.get(this).api()),
                        this,
                        ResourceManager(this)
                )
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

    override fun showAllItems(categoryItems: List<CategoryItem>) {
        categoriesListView.visibility = View.GONE
        categoryItemsListView.apply {
            translationY = categoryItemsListView.height.toFloat() / 2
            alpha = 0f
            adapter = allItemsAdapter
        }
        allItemsAdapter.apply {
            setColor(color)
            categories = categoryItems
        }

        categoryItemsListView.post {
            categoryItemsListView.run {
                animate().translationY(0f).alpha(1f).setDuration(250)
            }
        }
    }

    override fun showCategoryInfo(categoryItems: List<CategoryItem>) {
        categoryItemsListView.adapter = categoryInfoAdapter
        categoryInfoAdapter.categories = categoryItems
    }

    private fun searchForItems(query: String) {
        allItemsAdapter.categories = categoryPresenter.allItems
                .filter { it.name.startsWith(query, ignoreCase = true) }
    }

    private fun initToolbar() {
        toolbar.title = getString(R.string.select_part)
        setSupportActionBar(toolbar)
        cartViewLayoutCategory.setOnClickListener {
            val intent = Intent(this@CategoryActivity, OrdersActivity::class.java)
            intent.putExtra(Extras.EXTRA_COLOR, color)
            startActivity(intent)
        }
        updateCartBadgeCount(OrderList.get()!!.size)
    }

    private fun initSearchView() {
        clearSearchImageButton.setOnClickListener {
            main.post {
                main.requestFocus()
                hideKeyboard()
            }
            searchEditText.post { searchEditText.clear() }
        }

        searchEditText.setOnFocusChangeListener { _, focus ->
            Timber.d(focus.toString())
            clearSearchImageButton.visible(focus)

            if (!focus) {
                categoryPresenter.getCategories()
                categoriesListView.visible(true)
                categoriesListView.adapter = categoryAdapter
            } else {
                categoryPresenter.getAllItems()
            }
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Timber.d("change")
                if (searchEditText.hasFocus()) {
                    if (s.isNullOrEmpty() && categoryPresenter.allItems.isEmpty()) {
                        categoryPresenter.getAllItems()
                    } else {
                        searchForItems(s.toString())
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        updateCartBadgeCount(OrderList.get()!!.size)
    }

    private fun initList() {
        dialog = ProgressDialog(this)
        categoriesListView = findViewById<RecyclerListView>(R.id.recyclerView)
        categoryItemsListView = findViewById<RecyclerListView>(R.id.categoriesItems)

        categoriesListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        categoriesListView.setHasFixedSize(true)

        categoryItemsListView.layoutManager = LinearLayoutManager(this)
        categoriesListView.setHasFixedSize(true)

        allItemsAdapter = CategoryItemsAdapter(this)
        categoryAdapter = CategoriesAdapter()
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

    private fun initDrawer(toolbar: Toolbar, activity: Activity) {
        val itemContactData = PrimaryDrawerItem().withName(getString(R.string.contact_data)).withIcon(R.drawable.ic_user_blue).withSelectable(false)
        val itemOrders = PrimaryDrawerItem().withName(getString(R.string.my_orders)).withIcon(R.drawable.ic_my_orders).withSelectable(false)
        val itemAbout = PrimaryDrawerItem().withName(getString(R.string.about)).withIcon(R.drawable.ic_app_mini_logo).withSelectable(false)
        val itemRules = PrimaryDrawerItem().withName(getString(R.string.rules)).withIcon(R.drawable.ic_help).withSelectable(false)

        val accountHeader: AccountHeader = AccountHeaderBuilder()
                .withActivity(this)
                .withAccountHeader(R.layout.header_drawer_layout)
                .build()

        DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(false)
                .withTranslucentStatusBar(false)
                .withAccountHeader(accountHeader)
                .withSelectedItem(-1)
                .addDrawerItems(
                        itemContactData,
                        itemOrders,
                        DividerDrawerItem(),
                        itemAbout,
                        itemRules
                )
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    when (drawerItem) {
                        itemContactData -> openActivity(RegistrationActivity::class.java)
                        itemOrders -> openActivity(MyOrdersActivity::class.java)
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
        intent.putExtra(RegistrationActivity.EXTRA_SELECTED, RegistrationActivity.SELECTED_CONTACT_INFO_ACTIVITY)
        startActivity(intent)
    }

    private fun save(collection: List<Category>) {
        for (i in collection.indices) {
            val (id, _, _, _, color) = collection[i]
            ColorsList.add(Pair(id, Color.parseColor(color)))
        }
        ColorsList.saveColors(this)
    }

    private fun updateCartBadgeCount(count: Int) {
        runOnUiThread {
            if (count == 0) {
                badge_count_text.visibility = View.INVISIBLE
            } else {
                badge_count_text.visibility = View.VISIBLE
                badge_count_text.text = count.toString()
            }
        }
    }
}
