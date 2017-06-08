package ru.binaryblitz.Chisto.ui.order.select_service

import android.content.Intent
import android.graphics.Color
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
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Treatment
import ru.binaryblitz.Chisto.network.ServerApi
import ru.binaryblitz.Chisto.ui.base.BaseActivity
import ru.binaryblitz.Chisto.ui.order.ItemInfoActivity.EXTRA_EDIT
import ru.binaryblitz.Chisto.ui.order.OrdersActivity
import ru.binaryblitz.Chisto.ui.order.adapters.TreatmentsAdapter
import ru.binaryblitz.Chisto.utils.*
import ru.binaryblitz.Chisto.utils.Extras.*
import ru.binaryblitz.Chisto.views.RecyclerListView
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

class SelectServiceActivity : BaseActivity(), TreatmentsView {

    private var adapter: TreatmentsAdapter? = null
    private var layout: SwipeRefreshLayout? = null

    val format = "#.#"

    val squareCentimetersInSquareMeters = 10000.0

    private var width: Int = 0
    private var length: Int = 0
    private var dialogOpened = false
    private lateinit var presenter: TreatmentsPresenterImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_select_service)

        initElements()
        setOnClickListeners()
        initSizeDialog()
        initList()

        presenter = TreatmentsPresenterImpl(this, TreatmentsInteractorImpl(ServerApi.get(this).api()), this)
        presenter.setView(this)
        presenter.setCurrentOrder(intent.getParcelableExtra(EXTRA_CURRENT_ORDER))
        presenter.getTreatments(intent.getIntExtra(EXTRA_ID, 0))
    }

    override fun showProgress() {
    }

    override fun hideProgress() {
    }

    override fun showError(appErrorMessage: String?) {
    }

    override fun showTreatments(treatments: List<Treatment>) {
        sort(treatments as ArrayList<Treatment>)
        adapter!!.setCollection(treatments)
        adapter!!.notifyDataSetChanged()
    }

    override fun updateOrderAmount(amount: Int) {
        (findViewById(R.id.orderAmountText) as TextView).text = amount.toString()
    }

    private fun initElements() {
        val color = intent.getStringExtra(EXTRA_COLOR)
        findViewById(R.id.appbar).setBackgroundColor(Color.parseColor(color))
        AndroidUtilities.colorAndroidBar(this, Color.parseColor(color))
        (findViewById(R.id.date_text_view) as TextView).text = intent.getStringExtra(EXTRA_NAME)
        (findViewById(R.id.item_description) as TextView).text = intent.getStringExtra(EXTRA_DESCRIPTION)
    }

    private fun setOnClickListeners() {
        findViewById(R.id.left_btn).setOnClickListener {
            finishActivity()
        }

        findViewById(R.id.cont_btn).setOnClickListener {
            if (intent.getBooleanExtra(EXTRA_USE_AREA, false)) {
                showSizeDialog()
            } else {
                openActivity()
            }
        }

        findViewById(R.id.size_ok_btn).setOnClickListener {
            if (!checkSizes()) {
                showErrorDialog()
                return@setOnClickListener
            }
            closeDialog()
            openActivity()
        }

        findViewById(R.id.plus_btn).setOnClickListener { presenter.increaseOrderAmount() }

        findViewById(R.id.minus).setOnClickListener { presenter.decreaseOrderAmount() }

        findViewById(R.id.dialog).setOnClickListener {
            closeDialog()
        }

        findViewById(R.id.cancel_btn).setOnClickListener {
            closeDialog()
        }
    }

    private fun checkSizes(): Boolean {
        return width > 0 && length > 0
    }

    private fun showErrorDialog() {
        MaterialDialog.Builder(this)
                .title(R.string.error)
                .content(getString(R.string.wrong_width_or_length_code))
                .positiveText(R.string.ok_code)
                .onPositive { dialog, which -> dialog.dismiss() }
                .show()
    }

    private fun closeDialog() {
        dialogOpened = false
        Animations.animateRevealHide(findViewById(R.id.dialog))
    }

    override fun onBackPressed() {
        if (dialogOpened) {
            closeDialog()
        } else {
            finishActivity()
        }
    }

    private fun initSizeDialog() {
        val lengthEditText = findViewById(R.id.length_text) as EditText
        val widthEditText = findViewById(R.id.width_text) as EditText
        val square = findViewById(R.id.square_text) as TextView

        lengthEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                recomputeSquare(false, s, square)
            }
        })

        widthEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                recomputeSquare(true, s, square)
            }
        })
    }

    private fun showSizeDialog() {
        Handler().post {
            dialogOpened = true
            Animations.animateRevealShow(findViewById(R.id.dialog), this@SelectServiceActivity)
        }
    }

    private fun recomputeSquare(width: Boolean, editable: Editable, square: TextView) {
        if (editable.isNotEmpty() && editable.toString() != "0") {
            compute(width, editable.toString(), square)
        }
    }

    private fun compute(width: Boolean, str: String, square: TextView) {
        try {
            val input = Integer.parseInt(str)
            if (width) this.width = input else this.length = input
            formatSquareInputAndSetToTextView(square)
        } catch (e: Exception) {
            LogUtil.logException(e)
        }
    }

    private fun formatSquareInputAndSetToTextView(square: TextView) {
        val defaultSymbols = DecimalFormatSymbols(Locale.getDefault())
        square.text = DecimalFormat(format, defaultSymbols).format((this.length * this.width).toDouble() / squareCentimetersInSquareMeters) +
                getString(R.string.square_meter_symbol)
    }

    private fun finishActivity() {
        if (!intent.getBooleanExtra(EXTRA_EDIT, false)) {
            OrderList.removeCurrent()
        }
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
        if (isTreatmentsSelected()) {
            presenter.proceedOrder()
            processSelectedTreatments()
        } else {
            showNothingSelectedError()
        }
    }

    private fun processSelectedTreatments() {
        OrderList.changeColor(intent.getIntExtra(EXTRA_COLOR, ContextCompat.getColor(this, R.color.blackColor)))

        val size = (findViewById(R.id.square_text) as TextView).text.toString()
        if (size != getString(R.string.zero_size)) {
            OrderList.setSize(getDoubleFromTextView(size.split(" ")[0]))
        }

        if (!intent.getBooleanExtra(EXTRA_EDIT, false)) {
            addTreatments()
        } else {
            editTreatments()
        }

        finish()
    }

    private fun getDoubleFromTextView(text: String): Double {
        val format = NumberFormat.getInstance(Locale.getDefault())
        val number = format.parse(text)
        return number.toDouble()
    }

    private fun showNothingSelectedError() {
        Snackbar.make(findViewById(R.id.main), R.string.nothing_selected, Snackbar.LENGTH_SHORT).show()
    }

    private fun addTreatments() {
        OrderList.addTreatments(adapter!!.getSelected())
        goToOrdersActivity()
    }

    private fun goToOrdersActivity() {
        val intent = Intent(this@SelectServiceActivity, OrdersActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun editTreatments() {
        OrderList.addTreatmentsForEditing(adapter!!.getSelected())
    }

    private fun isTreatmentsSelected(): Boolean {
        return adapter!!.getSelected().size != 0 &&
                !(adapter!!.getSelected().size == 1 && adapter!!.getSelected()[0].id == AppConfig.decorationId)
    }

    private fun sort(collection: ArrayList<Treatment>) {
        Collections.sort(collection) { treatment1, treatment2 -> treatment1.name.compareTo(treatment2.name) }
    }
}
