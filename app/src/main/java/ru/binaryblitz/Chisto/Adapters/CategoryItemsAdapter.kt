package ru.binaryblitz.Chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import ru.binaryblitz.Chisto.Activities.SelectServiceActivity
import ru.binaryblitz.Chisto.Model.CategoryItem
import ru.binaryblitz.Chisto.Model.Order
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Utils.Image
import ru.binaryblitz.Chisto.Utils.OrderList
import java.util.*

class CategoryItemsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categories: ArrayList<CategoryItem>? = null
    private var color: Int = Color.parseColor("#212121")

    val EXTRA_DECORATION = "decoration"
    val EXTRA_ID = "id"
    val EXTRA_NAME = "name"
    val EXTRA_COLOR = "color"
    val EXTRA_USE_AREA = "userArea"

    init {
        Image.init(context)
        categories = ArrayList<CategoryItem>()
    }

    fun setColor(color: Int) {
        this.color = color
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
        holder.icon.setColorFilter(color)

        holder.itemView.setOnClickListener {
            showDialog(item)
        }
    }

    private fun showDialog(item: CategoryItem) {
        MaterialDialog.Builder(context)
                .title(R.string.decoration_question)
                .content(context.getString(R.string.decoration_help))
                .positiveText(R.string.yes_code)
                .negativeText(R.string.no_code)
                .onPositive { dialog, action ->
                    run { openActivity(item, true) }
                }
                .onNegative { dialog, action ->
                    run { openActivity(item, false) }
                }
                .show()
    }

    private fun openActivity(item: CategoryItem, decor: Boolean) {
        val intent = Intent(context, SelectServiceActivity::class.java)
        intent.putExtra(EXTRA_DECORATION, decor)
        intent.putExtra(EXTRA_ID, item.id)
        intent.putExtra(EXTRA_NAME, item.name)
        intent.putExtra(EXTRA_COLOR, color)
        intent.putExtra(EXTRA_USE_AREA, item.userArea)
        OrderList.add(Order(item, null, 1, color, decor))
        context.startActivity(intent)
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