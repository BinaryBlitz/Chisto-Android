package com.chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.chisto.Activities.ItemInfoActivity
import com.chisto.Model.Order
import com.chisto.R
import com.chisto.Utils.Image

import java.util.ArrayList

class OrdersAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Order>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val order = collection[position]

        holder.name.text = order.category.name

        var description = ""

        if (order.treatments != null) {
            for (i in 0..order.treatments!!.size - 1) {
                description += order.treatments!![i].name + " &#x2022 "
            }

            description += order.treatments!![order.treatments!!.size - 1].name
        }

        holder.description.text = description

        holder.count.text = "&#x2022" + order.count + " шт"

        Image.loadPhoto(order.category.icon, holder.icon)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ItemInfoActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Order>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val description: TextView
        val count: TextView
        val icon: ImageView

        init {
            name = itemView.findViewById(R.id.name) as TextView
            description = itemView.findViewById(R.id.description) as TextView
            count = itemView.findViewById(R.id.count) as TextView
            icon = itemView.findViewById(R.id.category_icon) as ImageView
        }
    }
}