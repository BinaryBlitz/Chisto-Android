package ru.binaryblitz.Chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import ru.binaryblitz.Chisto.Activities.ItemInfoActivity
import ru.binaryblitz.Chisto.Model.Order
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Utils.Image
import ru.binaryblitz.Chisto.Utils.OrderList
import ru.binaryblitz.Chisto.Utils.SwipeToDeleteAdapter
import java.util.*

class OrdersAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), SwipeToDeleteAdapter {
    private var collection = ArrayList<Order>()
    private val PENDING_REMOVAL_TIMEOUT: Long = 2000
    var itemsPendingRemoval: ArrayList<Order> = ArrayList()
    var undoOn = false
    val EXTRA_COLOR = "color"
    private val handler = Handler()
    var pendingRunnables: HashMap<Order, Runnable> = HashMap()

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
            for (i in 0..order.treatments!!.size - 2) {
                description += order.treatments!![i].name + " \u2022 "
            }

            description += order.treatments!![order.treatments!!.size - 1].name
        }

        holder.description.text = description
        holder.count.text = "\u00D7" + order.count + " шт"

        Image.loadPhoto(order.category.icon, holder.icon)
        holder.icon.setColorFilter(order.color)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ItemInfoActivity::class.java)
            intent.putExtra(EXTRA_COLOR, order.color)
            context.startActivity(intent)
        }
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