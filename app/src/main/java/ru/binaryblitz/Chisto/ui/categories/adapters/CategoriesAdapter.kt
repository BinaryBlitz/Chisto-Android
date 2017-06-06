package ru.binaryblitz.Chisto.ui.categories.adapters

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import io.reactivex.subjects.PublishSubject
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Category
import java.util.*

class CategoriesAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categories: ArrayList<Category>
    val onCategoryClickAction: PublishSubject<Category> = PublishSubject.create()
    private var selectedPosition = 0

    init {
        categories = ArrayList<Category>()
    }

    fun setCategories(categories: ArrayList<Category>) {
        this.categories = categories
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val category = categories[position]

        holder.name.text = category.name

        Picasso.with(context)
                .load(category.iconUrl)
                .fit()
                .into(holder.icon)

        if (selectedPosition == holder.adapterPosition) {
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary))
        } else {
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.greyColor))
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.greyColor))
        }

        holder.itemView.setOnClickListener {
            notifyItemChanged(selectedPosition)
            selectedPosition = holder.adapterPosition
            notifyItemChanged(selectedPosition)
            onCategoryClickAction.onNext(categories[position])
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name = itemView.findViewById(R.id.name) as TextView
        internal var icon = itemView.findViewById(R.id.category_icon) as ImageView
    }
}
