package ru.binaryblitz.Chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.graphics.*
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
import ru.binaryblitz.Chisto.Utils.DateUtils
import ru.binaryblitz.Chisto.Utils.Image
import ru.binaryblitz.Chisto.Utils.OrderList
import java.util.*
import android.graphics.ColorMatrixColorFilter
import android.graphics.ColorMatrix




class LaundriesAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Laundry>()
    private val EXTRA_ID = "id"
    private val EXTRA_COLLECTION_DATE = "collectionDate"
    private val EXTRA_DELIVERY_DATE = "deliveryDate"
    private val EXTRA_DELIVERY_BOUNDS = "deliveryBounds"
    private val EXTRA_DELIVERY_COST = "deliveryCost"

    init {
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

        if (laundry.isPassingMinimumPrice) setDisabledColors(holder)
        else setDisabledColors(holder)

        Image.loadGrayScalePhoto(context, laundry.icon, holder.icon)

        holder.itemView.setOnClickListener { selectLaundry(laundry) }
    }

    private fun setActiveColors(holder: ViewHolder) {
        val color = Color.parseColor("#212121")
        holder.name.setTextColor(color)
        holder.desc.setTextColor(color)
        holder.ratingBar.borderColor = color
        holder.ratingBar.fillColor = color
        holder.collectionCost.setTextColor(color)
        holder.collectionDate.setTextColor(color)
        holder.deliveryBounds.setTextColor(color)
        holder.deliveryDate.setTextColor(color)
        holder.icon.setColorFilter(color)
        holder.cost.setTextColor(color)
    }

    private fun setDisabledColors(holder: ViewHolder) {
        val color = Color.parseColor("#b6b6b6")
        holder.name.setTextColor(color)
        holder.desc.setTextColor(color)
        holder.ratingBar.borderColor = color
        holder.ratingBar.fillColor = color
        holder.collectionCost.setTextColor(color)
        holder.collectionDate.setTextColor(color)
        holder.deliveryBounds.setTextColor(color)
        holder.deliveryDate.setTextColor(color)
        holder.deliveryTitle.setTextColor(color)
        holder.collectionTitle.setTextColor(color)

        val matrix = ColorMatrix()
        matrix.setSaturation(0f)

        val filter = ColorMatrixColorFilter(matrix)
        holder.icon.colorFilter = filter

        holder.cost.setTextColor(color)

        holder.itemView.findViewById(R.id.basic_price_layout).visibility = View.GONE
        holder.itemView.findViewById(R.id.minimum_price_layout).visibility = View.VISIBLE
    }

    private fun selectLaundry(laundry: Laundry) {
        val intent = Intent(context, LaundryAndOrderActivity::class.java)
        OrderList.setLaundry(laundry)
        OrderList.setDecorationMultiplier(laundry.decorationMultipliers!!)
        OrderList.resetDecorationCosts()
        (context as LaundriesActivity).countSums(laundry.index!!)
        context.setLaundryTreatmentsIds(laundry.index)
        intent.putExtra(EXTRA_ID, laundry.id)
        intent.putExtra(EXTRA_COLLECTION_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry.collectionDate))
        intent.putExtra(EXTRA_DELIVERY_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry.deliveryDate))
        if (laundry.orderPrice!! < laundry.freeDeliveryFrom!!) {
            intent.putExtra(EXTRA_DELIVERY_COST, laundry.deliveryFee!!)
        }
        intent.putExtra(EXTRA_DELIVERY_BOUNDS, getPeriod(laundry))
        context.startActivity(intent)
    }

    fun sortByCost() {
        Collections.sort(collection, { first, second -> first.orderPrice!!.compareTo(second.orderPrice!!) })
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
        holder.collectionCost.text = getPeriod(laundry)
        holder.collectionDate.text = DateUtils.getDateStringRepresentationWithoutTime(laundry.collectionDate)
        holder.deliveryDate.text = DateUtils.getDateStringRepresentationWithoutTime(laundry.deliveryDate)
        holder.deliveryBounds.text = getPeriod(laundry)
        if (laundry.orderPrice!! >= laundry.freeDeliveryFrom!!) {
            holder.cost.text = laundry.orderPrice!!.toString() + context.getString(R.string.ruble_sign)
        } else {
            holder.cost.text = (laundry.orderPrice!! + laundry.deliveryFee!!).toString() + context.getString(R.string.ruble_sign)
        }

        holder.minimumPriceValue.text = holder.cost.text
        holder.minimumPrice.text = context.getString(R.string.cost) + " " + laundry.minimumOrderPrice.toString() + context.getString(R.string.ruble_sign)
    }

    private fun getPeriod(laundry: Laundry): String {
        return context.getString(R.string.from_code) + DateUtils.getTimeStringRepresentation(laundry.deliveryDateOpensAt) +
                context.getString(R.string.end_bound_code) +
                DateUtils.getTimeStringRepresentation(laundry.deliveryDateClosesAt)
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Laundry>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name) as TextView
        val desc: TextView = itemView.findViewById(R.id.description) as TextView
        val collectionDate: TextView = itemView.findViewById(R.id.curier_date) as TextView
        val collectionCost: TextView = itemView.findViewById(R.id.curier_cost) as TextView
        val deliveryDate: TextView = itemView.findViewById(R.id.delivery_date) as TextView
        val deliveryBounds: TextView = itemView.findViewById(R.id.delivery_bounds) as TextView
        val cost: TextView = itemView.findViewById(R.id.sum) as TextView
        val deliveryTitle: TextView = itemView.findViewById(R.id.delivery_title) as TextView
        val minimumPrice: TextView = itemView.findViewById(R.id.minimum_cost_title) as TextView
        val minimumPriceValue: TextView = itemView.findViewById(R.id.minimum_cost_price) as TextView
        val collectionTitle: TextView = itemView.findViewById(R.id.collection_title) as TextView
        val icon: ImageView = itemView.findViewById(R.id.category_icon) as ImageView
        val ratingBar: SimpleRatingBar = itemView.findViewById(R.id.ratingBar) as SimpleRatingBar
    }
}