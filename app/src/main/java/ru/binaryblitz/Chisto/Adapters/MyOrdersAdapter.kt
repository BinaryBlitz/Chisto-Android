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
        holder.status.text = getDateStringRepresentation(order.createAt)

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
                setInformation(holder, R.drawable.ic_completed_indicator, R.color.completedColor, R.string.ready_code)
            }
            MyOrder.Status.CANCELED -> {
                setInformation(holder, R.drawable.ic_canceled_indicator, R.color.canceledColor, R.string.canceled_code)
            }
            MyOrder.Status.CONFIRMED -> {
                setInformation(holder, R.drawable.ic_confirmed_indicator, R.color.confirmedColor, R.string.confirmed_code)
            }
            MyOrder.Status.DISPATCHED -> {
                setInformation(holder, R.drawable.ic_dispatched_indicator, R.color.dispatchedColor, R.string.dispatched_code)
            }
            MyOrder.Status.CLEANING -> {
                setInformation(holder, R.drawable.ic_cleaning_indicator, R.color.cleaningColor, R.string.cleaning_code)
            }
            else -> {
                setInformation(holder, R.drawable.ic_process_indicator, R.color.processColor, R.string.process_code)
            }
        }
    }

    private fun setInformation(holder: ViewHolder, icon: Int, color: Int, text: Int) {
        holder.marker.setImageResource(icon)
        holder.cost.setTextColor(ContextCompat.getColor(context, color))
        holder.status.setTextColor(ContextCompat.getColor(context, color))
        holder.status.text = context.getString(text)
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
        val name: TextView = itemView.findViewById(R.id.name) as TextView
        val cost: TextView = itemView.findViewById(R.id.cost) as TextView
        val desc: TextView = itemView.findViewById(R.id.desc) as TextView
        val status: TextView = itemView.findViewById(R.id.status) as TextView
        val marker: ImageView = itemView.findViewById(R.id.marker) as ImageView

    }
}
