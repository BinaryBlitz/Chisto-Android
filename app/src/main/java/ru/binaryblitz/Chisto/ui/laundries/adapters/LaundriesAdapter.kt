package ru.binaryblitz.Chisto.ui.laundries.adapters

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
import ru.binaryblitz.Chisto.ui.laundries.LaundriesActivity
import ru.binaryblitz.Chisto.ui.laundries.LaundryAndOrderActivity
import ru.binaryblitz.Chisto.entities.Laundry
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.utils.DateUtils
import ru.binaryblitz.Chisto.utils.Image
import ru.binaryblitz.Chisto.utils.OrderList
import java.util.*

class LaundriesAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Laundry>()
    private val EXTRA_ID = "id"
    private val EXTRA_COLLECTION_DATE = "collectionDate"
    private val EXTRA_DELIVERY_DATE = "deliveryDate"
    private val EXTRA_DELIVERY_BOUNDS = "deliveryBounds"
    private val EXTRA_DELIVERY_FEE = "deliveryFee"

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
        holder.description.text = laundry.description
        holder.ratingBar.rating = laundry.rating

        setDatesAndCosts(laundry, holder)

        if (laundry.isPassingMinimumPrice) {
            enableLaundry(holder, laundry)
        } else {
            disableLaundry(holder, laundry)
        }
    }

    private fun disableLaundry(holder: ViewHolder, laundry: Laundry) {
        Image.loadGrayScalePhoto(context, laundry.icon, holder.icon)
        setDisabledColors(holder)
        holder.itemView.setOnClickListener(null)
    }

    private fun enableLaundry(holder: ViewHolder, laundry: Laundry) {
        Image.loadPhoto(context, laundry.icon, holder.icon)
        setActiveColors(holder)
        holder.itemView.setOnClickListener { selectLaundry(laundry) }
    }

    private fun setActiveColors(holder: ViewHolder) {
        val color = Color.parseColor("#212121")
        val blueColor = Color.parseColor("#4bc2f7")
        val greenColor = Color.parseColor("#388E3C")

        holder.name.setTextColor(color)
        holder.description.setTextColor(color)
        holder.ratingBar.borderColor = blueColor
        holder.ratingBar.fillColor = blueColor
        holder.collectionPeriod.setTextColor(color)
        holder.collectionDate.setTextColor(color)
        holder.deliveryBounds.setTextColor(color)
        holder.deliveryDate.setTextColor(color)
        holder.deliveryTitle.setTextColor(blueColor)
        holder.collectionTitle.setTextColor(blueColor)
        holder.price.setTextColor(greenColor)

        holder.itemView.findViewById(R.id.basic_price_layout).visibility = View.VISIBLE
        holder.itemView.findViewById(R.id.minimum_price_layout).visibility = View.GONE
    }

    private fun setDisabledColors(holder: ViewHolder) {
        val color = Color.parseColor("#b6b6b6")
        holder.name.setTextColor(color)
        holder.description.setTextColor(color)
        holder.ratingBar.borderColor = color
        holder.ratingBar.fillColor = color
        holder.collectionPeriod.setTextColor(color)
        holder.collectionDate.setTextColor(color)
        holder.deliveryBounds.setTextColor(color)
        holder.deliveryDate.setTextColor(color)
        holder.deliveryTitle.setTextColor(color)
        holder.collectionTitle.setTextColor(color)
        holder.price.setTextColor(color)

        holder.itemView.findViewById(R.id.basic_price_layout).visibility = View.GONE
        holder.itemView.findViewById(R.id.minimum_price_layout).visibility = View.VISIBLE
    }

    private fun selectLaundry(laundry: Laundry) {
        val intent = Intent(context, LaundryAndOrderActivity::class.java)
        OrderList.setLaundry(laundry)
        OrderList.setDecorationMultiplier(laundry.decorationMultipliers!!)
        OrderList.resetDecorationPrices()
        (context as LaundriesActivity).countSums(laundry.index!!)
        context.setLaundryTreatmentsIds(laundry.index)
        intent.putExtra(EXTRA_ID, laundry.id)
        intent.putExtra(EXTRA_COLLECTION_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry.collectionDate))
        intent.putExtra(EXTRA_DELIVERY_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry.deliveryDate))
        if (laundry.orderPrice!! < laundry.freeDeliveryFrom!!) {
            intent.putExtra(EXTRA_DELIVERY_FEE, laundry.deliveryFee!!)
        }
        intent.putExtra(EXTRA_DELIVERY_BOUNDS, getPeriod(laundry))
        context.startActivity(intent)
    }

    fun sortByCost() {
        Collections.sort(collection, { first, second -> comparePrices(first, second) })
        notifyDataSetChanged()
    }

    fun comparePrices(first: Laundry, second: Laundry): Int {
        if (first.isPassingMinimumPrice && !second.isPassingMinimumPrice) {
            return -1
        } else if (!first.isPassingMinimumPrice && second.isPassingMinimumPrice) {
            return 1
        } else {
            return getPrice(first).compareTo(getPrice(second))
        }
    }

    fun getPrice(laundry: Laundry): Int {
        var price = laundry.orderPrice!!
        if (laundry.orderPrice!! < laundry.freeDeliveryFrom!!) {
            price = laundry.orderPrice!! + laundry.deliveryFee!!
        }

        return price
    }

    fun sortBySpeed() {
        Collections.sort(collection, { first, second -> compareSpeed(first, second) })
        notifyDataSetChanged()
    }

    fun compareSpeed(first: Laundry, second: Laundry): Int {
        if (first.isPassingMinimumPrice && !second.isPassingMinimumPrice) {
            return -1
        } else if (!first.isPassingMinimumPrice && second.isPassingMinimumPrice) {
            return 1
        } else {
            return first.deliveryDate!!.compareTo(second.deliveryDate!!)
        }
    }

    fun sortByRating() {
        Collections.sort(collection, { first, second -> compareRating(first, second) })
        notifyDataSetChanged()
    }

    fun compareRating(first: Laundry, second: Laundry): Int {
        if (first.isPassingMinimumPrice && !second.isPassingMinimumPrice) {
            return -1
        } else if (!first.isPassingMinimumPrice && second.isPassingMinimumPrice) {
            return 1
        } else {
            return -first.rating.compareTo(second.rating)
        }
    }

    private fun setDatesAndCosts(laundry: Laundry, holder: ViewHolder) {
        holder.collectionPeriod.text = getCollectionPeriod(laundry)
        holder.collectionDate.text = DateUtils.getDateStringRepresentationWithoutTime(laundry.collectionDate)
        holder.deliveryDate.text = DateUtils.getDateStringRepresentationWithoutTime(laundry.deliveryDate)
        holder.deliveryBounds.text = getPeriod(laundry)
        if (laundry.orderPrice!! >= laundry.freeDeliveryFrom!!) {
            holder.price.text = laundry.orderPrice!!.toString() + context.getString(R.string.ruble_sign)
        } else {
            holder.price.text = (laundry.orderPrice!! + laundry.deliveryFee!!).toString() + context.getString(R.string.ruble_sign)
        }

        holder.minimumPriceValue.text = laundry.minimumOrderPrice.toString() + context.getString(R.string.ruble_sign)
        holder.minimumPrice.text = context.getString(R.string.price) + " " + laundry.orderPrice!!.toString() + context.getString(R.string.ruble_sign)
    }

    private fun getPeriod(laundry: Laundry): String {
        return context.getString(R.string.from_code) + DateUtils.getTimeStringRepresentation(laundry.deliveryDateOpensAt) +
                context.getString(R.string.end_bound_code) +
                DateUtils.getTimeStringRepresentation(laundry.deliveryDateClosesAt)
    }

    private fun getCollectionPeriod(laundry: Laundry): String {
        return context.getString(R.string.from_code) + DateUtils.getTimeStringRepresentation(laundry.collectionDateOpensAt) +
                context.getString(R.string.end_bound_code) +
                DateUtils.getTimeStringRepresentation(laundry.collectionDateClosesAt)
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Laundry>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById(R.id.name) as TextView
        val description = itemView.findViewById(R.id.description) as TextView
        val collectionDate = itemView.findViewById(R.id.collection_date) as TextView
        val collectionPeriod = itemView.findViewById(R.id.collection_period) as TextView
        val deliveryDate = itemView.findViewById(R.id.delivery_date) as TextView
        val deliveryBounds = itemView.findViewById(R.id.delivery_bounds) as TextView
        val price = itemView.findViewById(R.id.sum) as TextView
        val deliveryTitle = itemView.findViewById(R.id.delivery_title) as TextView
        val minimumPrice = itemView.findViewById(R.id.minimum_cost_title) as TextView
        val minimumPriceValue = itemView.findViewById(R.id.minimum_cost_price) as TextView
        val collectionTitle = itemView.findViewById(R.id.collection_title) as TextView
        val icon = itemView.findViewById(R.id.category_icon) as ImageView
        val ratingBar = itemView.findViewById(R.id.ratingBar) as SimpleRatingBar
    }
}