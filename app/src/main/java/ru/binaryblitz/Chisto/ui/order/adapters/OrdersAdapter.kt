package ru.binaryblitz.Chisto.ui.order.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Order
import ru.binaryblitz.Chisto.ui.order.ItemInfoActivity
import ru.binaryblitz.Chisto.utils.Image
import ru.binaryblitz.Chisto.utils.OrderList
import ru.binaryblitz.Chisto.utils.SwipeToDeleteAdapter
import java.util.ArrayList
import java.util.HashMap

class OrdersAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), SwipeToDeleteAdapter {
    private var collection = ArrayList<Order>()
    private val PENDING_REMOVAL_TIMEOUT: Long = 2000
    var itemsPendingRemoval: ArrayList<Order> = ArrayList()
    var undoOn = false
    val EXTRA_COLOR = "color"
    val EXTRA_INDEX = "index"
    private val handler = Handler()
    var pendingRunnables: HashMap<Order, Runnable> = HashMap()
    private var color = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(itemView)
    }

    fun hasItemsWithLongTreatment(): Boolean {
        return collection.any { it.hasItemsWithLongTreatment }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val order = collection[position]

        holder.name.text = order.category.name

        var description = ""

        if (order.treatments != null) {
            for (i in 0..order.treatments!!.size - 2) {
                description += order.treatments!![i].name + " \u2022 "
            }

            description += order.treatments!![order.treatments!!.size - 1].name
        }

        holder.description.text = description
        holder.count.text = "\u00D7" + order.count + context.getString(R.string.count_postfix)

        Image.loadPhoto(context, order.category.icon, holder.icon)
        holder.icon.setColorFilter(color)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ItemInfoActivity::class.java)
            OrderList.edit(holder.adapterPosition)
            intent.putExtra(EXTRA_COLOR, color)
            intent.putExtra(EXTRA_INDEX, holder.adapterPosition)
            context.startActivity(intent)
        }
    }

    fun setItemColor(color: String) {
        if (color.isEmpty()) return
        this.color = Color.parseColor(color)
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Order>) {
        this.collection = collection
    }

    override fun pendingRemoval(position: Int) {
        val item = collection[position]
        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item)
            notifyItemChanged(position)
            val pendingRemovalRunnable = Runnable { remove(collection.indexOf(item)) }
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT)
            pendingRunnables.put(item, pendingRemovalRunnable)
        }
    }

    override fun isUndo(): Boolean {
        return undoOn
    }

    override fun remove(position: Int) {
        val item = collection[position]
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item)
        }
        if (collection.contains(item)) {
            OrderList.remove(position)
            notifyItemRemoved(position)
        }
    }

    override fun isPendingRemoval(position: Int): Boolean {
        val item = collection[position]
        return itemsPendingRemoval.contains(item)
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById(R.id.name) as TextView
        val description = itemView.findViewById(R.id.description) as TextView
        val count = itemView.findViewById(R.id.count) as TextView
        val icon = itemView.findViewById(R.id.category_icon) as ImageView
    }
}
