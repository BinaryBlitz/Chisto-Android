package com.chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.chisto.Activities.CategoryInfoActivity
import com.chisto.Model.Category
import com.chisto.R
import com.chisto.Utils.Image
import java.util.*

class CategoriesAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categories: ArrayList<Category>? = null

    init {
        Image.init(context)
        categories = ArrayList<Category>()
    }

    fun setCategories(categories: ArrayList<Category>) {
        this.categories = categories
    }

    @SuppressWarnings("unused")
    fun clear() {
        categories!!.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val category = categories!![position]

        holder.name.text = category.name
        holder.description.text = category.desc

        Image.loadPhoto(category.icon, holder.icon)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CategoryInfoActivity::class.java)
            intent.putExtra("id", category.id)
            intent.putExtra("color", category.color)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return categories!!.size
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var name: TextView
        internal var description: TextView
        internal var icon: ImageView

        init {
            name = itemView.findViewById(R.id.name) as TextView
            description = itemView.findViewById(R.id.description) as TextView
            icon = itemView.findViewById(R.id.category_icon) as ImageView
        }
    }
}