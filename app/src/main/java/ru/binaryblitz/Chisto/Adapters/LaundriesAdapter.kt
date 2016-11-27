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

        holder.category.visibility = View.VISIBLE
        holder.categoryBack.visibility = View.VISIBLE

        holder.ratingBar.rating = laundry.rating

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

        Image.loadPhoto(laundry.icon, holder.icon)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, LaundryAndOrderActivity::class.java)
            OrderList.setLaundryId(laundry.id)
            (context as LaundriesActivity).countSums(holder.adapterPosition)
            intent.putExtra(EXTRA_ID, laundry.id)
            context.startActivity(intent)
        }
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
        val icon: ImageView
        val categoryBack: ImageView
        val ratingBar: SimpleRatingBar

        init {
            name = itemView.findViewById(R.id.name) as TextView
            desc = itemView.findViewById(R.id.description) as TextView
            category = itemView.findViewById(R.id.type_text) as TextView
            icon = itemView.findViewById(R.id.category_icon) as ImageView
            categoryBack = itemView.findViewById(R.id.type) as ImageView
            ratingBar = itemView.findViewById(R.id.ratingBar) as SimpleRatingBar
        }
    }
}