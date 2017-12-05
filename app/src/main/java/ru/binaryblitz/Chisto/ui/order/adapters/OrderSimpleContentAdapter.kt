package ru.binaryblitz.Chisto.ui.order.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_order_content_header.*
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.utils.Image


class OrderSimpleContentAdapter : RecyclerView.Adapter<OrderSimpleContentAdapter.ViewHolder>() {

    data class Item(val name: String, val sum: Int, val count: Int, val icon: String, val color: Int)

    private val items = mutableListOf<Item>()

    fun setCollection(items: List<Item>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order_content_header, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
            override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(item: Item) {
            Image.loadPhoto(itemView.context, item.icon, category_icon)
            category_icon.setColorFilter(item.color)
            name.text = item.name + " " + (item.sum / item.count).toString() +
                    itemView.context.getString(R.string.ruble_sign) + "  \u00D7" + item.count.toString()
            price.text = item.sum.toString() + itemView.context.getString(R.string.ruble_sign)
        }
    }
}
