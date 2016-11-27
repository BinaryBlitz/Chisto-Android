package ru.binaryblitz.Chisto.Adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Utils.Image

class OrderContentAdapter(private val context: Activity?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class Header(val name: String, val sum: Int, val count: Int, val icon: String, val color: Int)

    class Basic(val name: String, val sum: Int)

    private var collection = ArrayList<Pair<String, Any>>()

    init {
        Image.init(context)
    }

    fun clear() {
        collection.clear()
        notifyDataSetChanged()
    }

    fun setCollection(collection: ArrayList<Pair<String, Any>>) {
        this.collection = collection
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView: View

        when (viewType) {
            HEADER -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_order_content_header, parent, false)
                return HeaderViewHolder(itemView)
            }
            BASIC -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_order_content_basic, parent, false)
                return BasicViewHolder(itemView)
            }
            else -> {
                itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_order_content_basic, parent, false)
                return BasicViewHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == HEADER) bindHeader(position, viewHolder as HeaderViewHolder)
        else bindBasic(position, viewHolder as BasicViewHolder)
    }

    private fun bindHeader(position: Int, holder: HeaderViewHolder) {
        val header = collection[position].second as Header
        Image.loadPhoto(header.icon, holder.icon)
        holder.icon.setColorFilter(header.color)
        holder.name.text = header.name + " " + (header.sum / header.count).toString() +
                " \u20bd" + "  \u00D7" + header.count.toString()
        holder.cost.text = header.sum.toString() + " \u20bd"
    }

    private fun bindBasic(position: Int, holder: BasicViewHolder) {
        val basic = collection[position].second as Basic
        holder.name.text = basic.name
        holder.cost.text = basic.sum.toString() + " \u20bd"
    }

    override fun getItemViewType(position: Int): Int {
        if (collection[position].first == "H")
            return HEADER
        else
            return BASIC
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    private inner class HeaderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val icon: ImageView
        val cost: TextView

        init {
            name = itemView.findViewById(R.id.name) as TextView
            cost = itemView.findViewById(R.id.cost) as TextView
            icon = itemView.findViewById(R.id.category_icon) as ImageView
        }
    }

    private inner class BasicViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val cost: TextView

        init {
            name = itemView.findViewById(R.id.name) as TextView
            cost = itemView.findViewById(R.id.cost) as TextView
        }
    }

    companion object {

        private val HEADER = 1
        private val BASIC = 2
    }
}