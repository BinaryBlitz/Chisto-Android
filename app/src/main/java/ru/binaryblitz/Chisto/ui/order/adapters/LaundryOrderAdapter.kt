package ru.binaryblitz.Chisto.ui.order.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_order_content_header.*
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.extension.inflate
import ru.binaryblitz.Chisto.utils.Image


class LaundryOrderAdapter : RecyclerView.Adapter<LaundryOrderAdapter.ViewHolder>() {

    data class Item(val name: String, val sum: Int, val count: Int, val icon: String, val color: Int)

    private val items = mutableListOf<Item>()

    fun setCollection(items: List<Item>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_order_content_header))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
            override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: Item) {
            itemView.context?.run {
                Image.loadPhoto(this, item.icon, category_icon)
                category_icon.setColorFilter(item.color)
                name.text = "${item.name} ${item.sum / item.count} ${getString(R.string.ruble_sign)} \u00D7 ${item.count}"
                price.text = "${item.sum} ${getString(R.string.ruble_sign)}"
            }
        }
    }
}
