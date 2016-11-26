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

class OrderContentAdapter(private var context: Activity?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class Header(val name: String, val sum: Int, val count: Int, val icon: Int)

    inner class Basic(val name: String, val sum: Int)

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

    fun setContext(context: Activity) {
        this.context = context
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
        if (getItemViewType(position) == HEADER) {
            val holder = viewHolder as HeaderViewHolder
            val header = collection[position].second as Header
            holder.icon.setImageResource(header.icon)
            holder.name.text = header.name
            holder.cost.setText(header.sum)
        } else if (getItemViewType(position) == BASIC) {
            val holder = viewHolder as BasicViewHolder
            val basic = collection[position].second as Basic
            holder.name.text = basic.name
            holder.cost.setText(basic.sum)
        }
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
            cost = itemView.findViewById(R.id.textView) as TextView
            icon = itemView.findViewById(R.id.category_icon) as ImageView
        }
    }

    private inner class BasicViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val cost: TextView

        init {
            name = itemView.findViewById(R.id.name_text) as TextView
            cost = itemView.findViewById(R.id.textView) as TextView
        }
    }

    companion object {

        private val HEADER = 1
        private val BASIC = 2
    }
}