package ru.binaryblitz.Chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.binaryblitz.Chisto.Activities.MyOrderActivity
import ru.binaryblitz.Chisto.Model.MyOrder
import ru.binaryblitz.Chisto.R
import java.text.SimpleDateFormat
import java.util.*

class MyOrdersAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var collection = ArrayList<MyOrder>()
    private val EXTRA_ID = "id"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_my_order, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val order = collection[position]

        holder.name.text = context.getString(R.string.my_order_code) + order.id
        setIconAndColor(order, holder)

        holder.desc.text = getDateStringRepresentation(order.createAt)
        holder.cost.text = order.price.toString() + context.getString(R.string.ruble_sign)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MyOrderActivity::class.java)
            intent.putExtra(EXTRA_ID, order.id)
            context.startActivity(intent)
        }
    }

    private fun setIconAndColor(order: MyOrder, holder: ViewHolder) {
        when (order.status) {
            MyOrder.Status.COMPLETED -> {
                holder.marker.setImageResource(R.drawable.completed_indicator)
                holder.cost.setTextColor(ContextCompat.getColor(context, R.color.completedColor))
            }
            MyOrder.Status.CANCELED -> {
                holder.marker.setImageResource(R.drawable.canceled_indicator)
                holder.cost.setTextColor(ContextCompat.getColor(context, R.color.canceledColor))
            }
            MyOrder.Status.CONFIRMED -> {
                holder.marker.setImageResource(R.drawable.confirmed_indicator)
                holder.cost.setTextColor(ContextCompat.getColor(context, R.color.confirmedColor))
            }
            MyOrder.Status.DISPATCHED -> {
                holder.marker.setImageResource(R.drawable.dispatched_indicator)
                holder.cost.setTextColor(ContextCompat.getColor(context, R.color.dispatchedColor))
            }
            MyOrder.Status.CLEANING -> {
                holder.marker.setImageResource(R.drawable.cleaning_indicator)
                holder.cost.setTextColor(ContextCompat.getColor(context, R.color.cleaningColor))
            }
            else -> {
                holder.cost.setTextColor(ContextCompat.getColor(context, R.color.processColor))
                holder.marker.setImageResource(R.drawable.process_indicator)
            }
        }
    }

    fun getDateStringRepresentation(date: Date): String {
        val format = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(date)
    }


    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<MyOrder>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val cost: TextView
        val desc: TextView
        val marker: ImageView

        init {
            name = itemView.findViewById(R.id.name) as TextView
            cost = itemView.findViewById(R.id.cost) as TextView
            desc = itemView.findViewById(R.id.desc) as TextView
            marker = itemView.findViewById(R.id.marker) as ImageView
        }
    }
}
