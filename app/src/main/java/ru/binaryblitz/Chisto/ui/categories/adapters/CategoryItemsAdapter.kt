package ru.binaryblitz.Chisto.ui.categories.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import it.sephiroth.android.library.tooltip.Tooltip
import it.sephiroth.android.library.tooltip.Tooltip.ClosePolicy
import ru.binaryblitz.Chisto.ui.order.SelectServiceActivity
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.entities.Order
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.network.DeviceInfoStore
import ru.binaryblitz.Chisto.utils.Image
import ru.binaryblitz.Chisto.utils.OrderList
import java.util.*


class CategoryItemsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categories: List<CategoryItem> = ArrayList()
    private var color: Int = Color.parseColor("#212121")

    val EXTRA_DECORATION = "decoration"
    val EXTRA_ID = "id"
    val EXTRA_NAME = "name"
    val EXTRA_COLOR = "color"
    val EXTRA_USE_AREA = "userArea"

    private var tooltip: Tooltip.TooltipView? = null

    fun setColor(color: Int) {
        this.color = color
    }

    fun setCategories(categories: List<CategoryItem>) {
        this.categories = categories
    }

    fun getCategories(): List<CategoryItem>? {
        return categories
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_category_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val item = categories[position]

        holder.name.text = item.name
        holder.description.text = item.description

        Image.loadPhoto(context, item.icon, holder.icon)
        holder.icon.setColorFilter(item.color)

        holder.longTreatmentIndicator.visibility = if (item.isLongTreatment) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            showDialog(item)
        }

        holder.longTreatmentIndicator.setOnClickListener {
            showToolTip(holder.itemView.findViewById(R.id.longTreatmentIndicatorAnchorImageView))
        }
    }

    private fun showToolTip(anchorView: View) {
        if (tooltip != null && tooltip!!.isShown) {
            return
        }

        tooltip = Tooltip.make(context,
                    Tooltip.Builder(101)
                        .anchor(anchorView, Tooltip.Gravity.TOP)
                        .closePolicy(ClosePolicy()
                                .insidePolicy(true, false)
                                .outsidePolicy(true, false), 3000)
                        .activateDelay(500)
                        .showDelay(100)
                        .text(context.getString(R.string.slow_item_tooltip))
                        .maxWidth(500)
                        .withStyleId(R.style.ToolTipLayoutDefaultStyle)
                        .withArrow(true)
                        .withOverlay(true)
                        .build())

        tooltip?.show()
    }

    private fun showDialog(item: CategoryItem) {
        if (!DeviceInfoStore.getShowDialogFlag(context)) {
            openActivity(item, false)
            return
        }

        MaterialDialog.Builder(context)
                .title(R.string.decoration_question)
                .content(context.getString(R.string.decoration_help))
                .positiveText(R.string.yes_code)
                .negativeText(R.string.no_code)
                .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                .onPositive { _, _ ->
                    run { openActivity(item, true) }
                }
                .onNegative { _, _ ->
                    run { openActivity(item, false) }
                }
                .onAny { dialog, _ ->
                    run { saveShowDialogFlag(!dialog.isPromptCheckBoxChecked) }
                }
                .show()
    }

    private fun saveShowDialogFlag(isShow: Boolean) {
        DeviceInfoStore.saveShowDialogFlag(context, isShow)
    }

    private fun openActivity(item: CategoryItem, decor: Boolean) {
        val intent = Intent(context, SelectServiceActivity::class.java)
        intent.putExtra(EXTRA_DECORATION, decor)
        intent.putExtra(EXTRA_ID, item.id)
        intent.putExtra(EXTRA_NAME, item.name)
        intent.putExtra(EXTRA_COLOR, item.color)
        intent.putExtra(EXTRA_USE_AREA, item.userArea)
        OrderList.add(Order(item, null, 1, item.color, decor, 0, null, item.isLongTreatment))
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name = itemView.findViewById(R.id.name) as TextView
        internal var description = itemView.findViewById(R.id.description) as TextView
        internal var icon = itemView.findViewById(R.id.category_icon) as ImageView
        internal var longTreatmentIndicator = itemView.findViewById(R.id.longTreatmentIndicator)
    }
}