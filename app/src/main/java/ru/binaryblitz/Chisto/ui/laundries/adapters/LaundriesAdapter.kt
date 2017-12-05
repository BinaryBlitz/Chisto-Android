package ru.binaryblitz.Chisto.ui.laundries.adapters

import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_laundry.*
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Laundry
import ru.binaryblitz.Chisto.extension.inflate
import ru.binaryblitz.Chisto.extension.visible
import ru.binaryblitz.Chisto.ui.laundries.LaundriesActivity
import ru.binaryblitz.Chisto.ui.laundries.LaundryAndOrderActivity
import ru.binaryblitz.Chisto.utils.DateUtils
import ru.binaryblitz.Chisto.utils.Image
import ru.binaryblitz.Chisto.utils.OrderList
import java.util.*

class LaundriesAdapter : RecyclerView.Adapter<LaundriesAdapter.ViewHolder>() {

    private var collection = mutableListOf<Laundry>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_laundry))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(collection[position])
    }

    fun sortByCost() {
        Collections.sort(collection, { first, second -> comparePrices(first, second) })
        notifyDataSetChanged()
    }

    private fun comparePrices(first: Laundry, second: Laundry): Int = when {
        first.isPassingMinimumPrice && !second.isPassingMinimumPrice -> -1
        !first.isPassingMinimumPrice && second.isPassingMinimumPrice -> 1
        else -> getPrice(first).compareTo(getPrice(second))
    }

    private fun getPrice(laundry: Laundry): Int {
        var price = laundry.orderPrice

        if (laundry.orderPrice < laundry.freeDeliveryFrom) {
            price = laundry.orderPrice + laundry.deliveryFee
        }

        return price
    }

    fun sortBySpeed() {
        Collections.sort(collection, { first, second -> compareSpeed(first, second) })
        notifyDataSetChanged()
    }

    private fun compareSpeed(first: Laundry, second: Laundry): Int = when {
        first.isPassingMinimumPrice && !second.isPassingMinimumPrice -> -1
        !first.isPassingMinimumPrice && second.isPassingMinimumPrice -> 1
        else -> first.deliveryDate!!.compareTo(second.deliveryDate!!)
    }

    fun sortByRating() {
        Collections.sort(collection, { first, second -> compareRating(first, second) })
        notifyDataSetChanged()
    }

    private fun compareRating(first: Laundry, second: Laundry): Int = when {
        first.isPassingMinimumPrice && !second.isPassingMinimumPrice -> -1
        !first.isPassingMinimumPrice && second.isPassingMinimumPrice -> 1
        else -> -first.rating.compareTo(second.rating)
    }

    override fun getItemCount(): Int = collection.size

    fun setCollection(collection: ArrayList<Laundry>) {
        this.collection = collection
    }

    class ViewHolder(
            override val containerView: View
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        private lateinit var item: Laundry

        init {
            itemView.setOnClickListener { }
        }

        fun bind(item: Laundry) {
            this.item = item
            nameTextView.text = item.name
            descriptionTextView.text = item.description
            ratingBar.rating = item.rating

            setDatesAndCosts(item)

            if (item.isPassingMinimumPrice) {
                enableLaundry(item)
            } else {
                disableLaundry(item)
            }
        }

        private fun setDatesAndCosts(laundry: Laundry) {
            priceTextView.text = if (laundry.orderPrice >= laundry.freeDeliveryFrom) {
                laundry.orderPrice.toString() + itemView.context.getString(R.string.ruble_sign)
            } else {
                (laundry.orderPrice + laundry.deliveryFee).toString() +
                        itemView.context.getString(R.string.ruble_sign)
            }

            miniPriceTextView.text = laundry.minimumOrderPrice.toString() + itemView.context.getString(R.string.ruble_sign)
        }

        private fun disableLaundry(laundry: Laundry) {
            Image.loadGrayScalePhoto(itemView.context, laundry.icon, categoryIconCircularImageView)
            setDisabledColors()
            itemView.setOnClickListener(null)
        }

        private fun enableLaundry(laundry: Laundry) {
            Image.loadPhoto(itemView.context, laundry.icon, categoryIconCircularImageView)
            setActiveColors()
            itemView.setOnClickListener { selectLaundry(laundry) }
        }

        private fun setDisabledColors() {
            val color = Color.parseColor("#b6b6b6")
            nameTextView.setTextColor(color)
            descriptionTextView.setTextColor(color)
            ratingBar.borderColor = color
            ratingBar.fillColor = color
            priceTextView.setTextColor(color)
            priceTextView.visible(false, gone = false)
            miniPriceTextView.setTextColor(color)
        }

        private fun setActiveColors() {
            val color = Color.parseColor("#212121")
            val blueColor = Color.parseColor("#4bc2f7")
            val greenColor = Color.parseColor("#388E3C")

            nameTextView.setTextColor(color)
            descriptionTextView.setTextColor(color)
            ratingBar.borderColor = blueColor
            ratingBar.fillColor = blueColor
            priceTextView.setTextColor(greenColor)

            priceTextView.visible(true)
            minPriceLayout.visible(false, gone = false)
        }

        private fun selectLaundry(laundry: Laundry) {
            val intent = Intent(itemView.context, LaundryAndOrderActivity::class.java)
            OrderList.setLaundry(laundry)
            OrderList.setDecorationMultiplier(laundry.decorationMultipliers!!)
            OrderList.resetDecorationPrices()
            (itemView.context as LaundriesActivity).countSums(laundry.index!!)
            (itemView.context as LaundriesActivity).setLaundryTreatmentsIds(laundry.index)
            intent.putExtra(EXTRA_ID, laundry.id)
            intent.putExtra(EXTRA_COLLECTION_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry.collectionDate))
            intent.putExtra(EXTRA_DELIVERY_DATE, DateUtils.getDateStringRepresentationWithoutTime(laundry.deliveryDate))

            if (laundry.orderPrice < laundry.freeDeliveryFrom) {
                intent.putExtra(EXTRA_DELIVERY_FEE, laundry.deliveryFee)
            }

            intent.putExtra(EXTRA_DELIVERY_BOUNDS, getPeriod(laundry))
            itemView.context.startActivity(intent)
        }

        private fun getPeriod(laundry: Laundry): String {
            return itemView.context.getString(R.string.from_code) +
                    DateUtils.getTimeStringRepresentation(laundry.deliveryDateOpensAt) +
                    itemView.context.getString(R.string.end_bound_code) +
                    DateUtils.getTimeStringRepresentation(laundry.deliveryDateClosesAt)
        }
    }

    private companion object {
        private const val EXTRA_ID = "id"
        private const val EXTRA_COLLECTION_DATE = "collectionDate"
        private const val EXTRA_DELIVERY_DATE = "deliveryDate"
        private const val EXTRA_DELIVERY_BOUNDS = "deliveryBounds"
        private const val EXTRA_DELIVERY_FEE = "deliveryFee"
    }
}
