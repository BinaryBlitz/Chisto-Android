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
import it.sephiroth.android.library.tooltip.Tooltip
import it.sephiroth.android.library.tooltip.Tooltip.ClosePolicy
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.CategoryItem
import ru.binaryblitz.Chisto.entities.Order
import ru.binaryblitz.Chisto.ui.order.select_service.SelectServiceActivity
import ru.binaryblitz.Chisto.utils.Extras.*
import ru.binaryblitz.Chisto.utils.Image


class CategoryItemsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var categories = listOf<CategoryItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var color: String = Color.parseColor("#212121").toString()

    private var tooltip: Tooltip.TooltipView? = null

    fun setColor(color: String) {
        this.color = color
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
        holder.icon.setColorFilter(Color.parseColor(item.categoryColor))

        holder.longTreatmentIndicator.visibility = if (item.isLongTreatment) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            openSelectServiceScreen(item, true)
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

    private fun openSelectServiceScreen(item: CategoryItem, decor: Boolean) {
        val intent = Intent(context, SelectServiceActivity::class.java)
        intent.putExtra(EXTRA_DECORATION, decor)
        intent.putExtra(EXTRA_ID, item.id)
        intent.putExtra(EXTRA_NAME, item.name)
        intent.putExtra(EXTRA_DESCRIPTION, item.description)
        intent.putExtra(EXTRA_COLOR, color)
        intent.putExtra(EXTRA_USE_AREA, item.userArea)
        intent.putExtra(EXTRA_CURRENT_ORDER, Order(item, null, 1, item.color, decor, 0, null, item.isLongTreatment))
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name = itemView.findViewById(R.id.name) as TextView
        internal var description = itemView.findViewById(R.id.description) as TextView
        internal var icon = itemView.findViewById(R.id.category_icon) as ImageView
        internal var longTreatmentIndicator = itemView.findViewById(R.id.longTreatmentIndicator) as View
    }
}
