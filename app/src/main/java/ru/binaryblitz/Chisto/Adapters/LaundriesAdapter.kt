package ru.binaryblitz.Chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import ru.binaryblitz.Chisto.Activities.LaundriesActivity
import ru.binaryblitz.Chisto.Activities.LaundryAndOrderActivity
import ru.binaryblitz.Chisto.Model.Laundry
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Utils.Image
import ru.binaryblitz.Chisto.Utils.OrderList
import java.text.SimpleDateFormat
import java.util.*

class LaundriesAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Laundry>()
    private val EXTRA_ID = "id"

    init {
        Image.init(context)
        collection = ArrayList<Laundry>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_laundry, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val laundry = collection[position]

        holder.name.text = laundry.name
        holder.desc.text = laundry.desc
        holder.ratingBar.rating = laundry.rating

        setDatesAndCosts(laundry, holder)
        setCategory(laundry, holder)

        Image.loadPhoto(laundry.icon, holder.icon)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, LaundryAndOrderActivity::class.java)
            OrderList.setLaundryId(laundry.id)
            (context as LaundriesActivity).countSums(holder.adapterPosition)
            intent.putExtra(EXTRA_ID, laundry.id)
            context.startActivity(intent)
        }
    }

    fun sortByCost() {
        Collections.sort(collection, { first, second -> first.orderCost!!.compareTo(second.orderCost!!) })
        notifyDataSetChanged()
    }

    fun sortBySpeed() {
        Collections.sort(collection, { first, second -> first.deliveryDate!!.compareTo(second.deliveryDate!!) })
        notifyDataSetChanged()
    }

    fun sortByRating() {
        Collections.sort(collection, { first, second -> -first.rating.compareTo(second.rating) })
        notifyDataSetChanged()
    }

    private fun setDatesAndCosts(laundry: Laundry, holder: ViewHolder) {
        holder.collectionCost.text = laundry.deliveryCost.toString() + " \u20bd"
        holder.collectionDate.text = getDateStringRepresentationWithoutTime(laundry.collectionDate!!)
        holder.deliveryDate.text = getDateStringRepresentationWithoutTime(laundry.deliveryDate!!)
        holder.deliveryBounds.text =
                context.getString(R.string.from_code) + getTimeStringRepresentation(laundry.deliveryDateOpensAt!!) +
                        context.getString(R.string.end_bound_code) +
                        getTimeStringRepresentation(laundry.deliveryDateClosesAt!!)
        holder.cost.text = laundry.orderCost.toString() + " \u20bd"
    }

    private fun setCategory(laundry: Laundry, holder: ViewHolder) {
        when (laundry.type) {
            Laundry.Type.FAST -> {
                holder.category.text = context.getString(R.string.premium)
                holder.categoryBack.setImageResource(R.drawable.fast_bg)
            }
            Laundry.Type.PREMIUM -> {
                holder.category.text = context.getString(R.string.fast)
                holder.categoryBack.setImageResource(R.drawable.premium_bg)
            }
            Laundry.Type.ECONOMY -> {
                holder.category.text = context.getString(R.string.economy)
                holder.categoryBack.setImageResource(R.drawable.economy_bg)
            }
            Laundry.Type.EMPTY -> {
                holder.category.visibility = View.GONE
                holder.categoryBack.visibility = View.GONE
            }
        }
    }

    fun getDateStringRepresentationWithoutTime(date: Date): String {
        val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }

    fun getTimeStringRepresentation(date: Date): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        format.timeZone = TimeZone.getDefault()
        return format.format(date)
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Laundry>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val desc: TextView
        val category: TextView
        val collectionDate: TextView
        val collectionCost: TextView
        val deliveryDate: TextView
        val deliveryBounds: TextView
        val cost: TextView
        val icon: ImageView
        val categoryBack: ImageView
        val ratingBar: SimpleRatingBar

        init {
            name = itemView.findViewById(R.id.name) as TextView
            desc = itemView.findViewById(R.id.description) as TextView
            category = itemView.findViewById(R.id.type_text) as TextView
            collectionDate = itemView.findViewById(R.id.curier_date) as TextView
            collectionCost = itemView.findViewById(R.id.curier_cost) as TextView
            deliveryDate = itemView.findViewById(R.id.delivery_date) as TextView
            deliveryBounds = itemView.findViewById(R.id.delivery_bounds) as TextView
            cost = itemView.findViewById(R.id.sum) as TextView
            icon = itemView.findViewById(R.id.category_icon) as ImageView
            categoryBack = itemView.findViewById(R.id.type) as ImageView
            ratingBar = itemView.findViewById(R.id.ratingBar) as SimpleRatingBar
        }
    }
}