package ru.binaryblitz.Chisto.ui.categories.adapters

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import io.reactivex.subjects.PublishSubject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_category.*
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Category
import ru.binaryblitz.Chisto.extension.inflate
import java.util.*

class CategoriesAdapter : RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    private var categories = mutableListOf<Category>()
    val onCategoryClickAction: PublishSubject<Category> = PublishSubject.create()
    private var selectedPosition = 0

    fun setCategories(categories: ArrayList<Category>) {
        this.categories = categories
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(
                    parent.inflate(R.layout.item_category),
                    { position ->
                        selectedPosition = position
                        notifyDataSetChanged()
                        onCategoryClickAction.onNext(categories[position])
                    }
            )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], selectedPosition)
    }

    override fun getItemCount(): Int = categories.size

    class ViewHolder(
            override val containerView: View,
            private val listener: (position: Int) -> Unit
    ) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            itemView.setOnClickListener { listener.invoke(adapterPosition) }
        }

        fun bind(item: Category, selectedPosition: Int) {
            name.text = item.name

            Picasso.with(itemView.context)
                    .load(item.iconUrl)
                    .fit()
                    .into(category_icon)

            val color = if (selectedPosition == adapterPosition) {
                Color.parseColor(item.color)
            } else {
                ContextCompat.getColor(itemView.context, R.color.greyColor)
            }

            name.setTextColor(color)
            category_icon.setColorFilter(color)
        }
    }
}
