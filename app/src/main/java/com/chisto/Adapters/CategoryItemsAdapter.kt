package com.chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.chisto.Activities.CategoryInfoActivity
import com.chisto.Activities.SelectServiceActivity
import com.chisto.Model.CategoryItem
import com.chisto.R
import com.chisto.Utils.Image
import java.util.*

class CategoryItemsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categories: ArrayList<CategoryItem>? = null

    init {
        Image.init(context)
        categories = ArrayList<CategoryItem>()
    }

    fun setCategories(categories: ArrayList<CategoryItem>) {
        this.categories = categories
    }

    @SuppressWarnings("unused")
    fun clear() {
        categories!!.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_category_item, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val item = categories!![position]

        holder.name.text = item.name
        holder.description.text = item.desc

        Image.loadPhoto(item.icon, holder.icon)

        holder.itemView.setOnClickListener {
            MaterialDialog.Builder(context)
                    .title("Chisto")
                    .content("Eсть ли у вещи декоративная отделка?")
                    .positiveText("ДА")
                    .negativeText("НЕТ")
                    .onPositive { dialog, action ->
                        run {
                            val intent = Intent(context, SelectServiceActivity::class.java)
                            intent.putExtra("decor", true)
                            context.startActivity(intent)
                        }
                    }
                    .onNegative { dialog, action ->
                        run {
                            val intent = Intent(context, SelectServiceActivity::class.java)
                            intent.putExtra("decor", false)
                            context.startActivity(intent)
                        }
                    }
                    .show()
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