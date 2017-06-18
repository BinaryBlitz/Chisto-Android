package ru.binaryblitz.Chisto.ui.order.adapters

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.reactivex.subjects.PublishSubject
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Order
import ru.binaryblitz.Chisto.ui.order.ItemInfoActivity
import ru.binaryblitz.Chisto.utils.Image
import ru.binaryblitz.Chisto.utils.OrderList
import java.util.ArrayList

class OrdersAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var collection = ArrayList<Order>()
    private var selectedPositions = ArrayList<Order>()
    val onItemSelectAction: PublishSubject<Boolean> = PublishSubject.create()

    val EXTRA_COLOR = "color"
    val EXTRA_INDEX = "index"
    private var isSelectionEnabled: Boolean = false
    private var color = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(itemView)
    }

    fun hasItemsWithLongTreatment(): Boolean {
        return collection.any { it.hasItemsWithLongTreatment }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val order = collection[position]

        holder.name.text = order.category.name

        var description = ""

        if (order.treatments != null) {
            for (i in 0..order.treatments!!.size - 2) {
                description += order.treatments!![i].name + " \u2022 "
            }

            description += order.treatments!![order.treatments!!.size - 1].description
        }

        holder.description.text = description
        holder.count.text = "\u00D7" + order.count + context.getString(R.string.count_postfix)

        Image.loadPhoto(context, order.category.icon, holder.icon)
        holder.icon.setColorFilter(Color.parseColor(color))
        setItemViewColor(holder.itemView)

        holder.itemView.setOnClickListener {
            if (!isSelectionEnabled) {
                openItemInfoScreen(holder)
                return@setOnClickListener
            }

            if (holder.itemView.isSelected) {
                removeSelectedItem(holder.itemView, collection[position].category.id)
            } else {
                addSelectedItem(viewHolder.itemView, position)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (isSelectionEnabled) {
                removeSelectedItem(viewHolder.itemView, position)
            } else {
                addSelectedItem(viewHolder.itemView, position)
            }
        }

    }

    private fun openItemInfoScreen(holder: ViewHolder) {
        val intent = Intent(context, ItemInfoActivity::class.java)
        OrderList.edit(holder.adapterPosition)
        intent.putExtra(EXTRA_COLOR, color)
        intent.putExtra(EXTRA_INDEX, holder.adapterPosition)
        context.startActivity(intent)
    }

    private fun setItemViewColor(itemView: View) {
        if (itemView.isSelected) {
            itemView.setBackgroundColor(context.resources.getColor(R.color.primary_light))
        } else {
            itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun setItemColor(color: String) {
        if (color.isEmpty()) return
        this.color = color
    }

    private fun addSelectedItem(itemView: View, position: Int): Boolean {
        selectItemView(itemView, true)
        selectedPositions.add(collection[position])
        onItemSelectAction.onNext(!selectedPositions.isEmpty())
        isSelectionEnabled = true
        return true
    }

    private fun removeSelectedItem(itemView: View, categoryId: Int): Boolean {
        selectItemView(itemView, false)
        selectedPositions = ArrayList(selectedPositions.filter { it.category.id != categoryId })
        onItemSelectAction.onNext(!selectedPositions.isEmpty())
        if (selectedPositions.isEmpty()) {
            isSelectionEnabled = false
        }
        return false
    }

    private fun selectItemView(itemView: View, isSelected: Boolean) {
        itemView.isSelected = isSelected
        setItemViewColor(itemView)
    }

    fun removeSelectedOrders() {
        if (collection.removeAll(selectedPositions)) {
            OrderList.get()!!.removeAll(selectedPositions)
            selectedPositions.clear()
            notifyDataSetChanged()
            clearSelections()
        }
    }

    private fun clearSelections() {
        onItemSelectAction.onNext(false)
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Order>) {
        this.collection = collection
    }

    fun remove(position: Int): Boolean {
        val item = collection[position]

        if (!collection.contains(item)) return false

        OrderList.remove(position)
        notifyItemRemoved(position)
        return true
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById(R.id.name) as TextView
        val description = itemView.findViewById(R.id.description) as TextView
        val count = itemView.findViewById(R.id.count) as TextView
        val icon = itemView.findViewById(R.id.category_icon) as ImageView
    }
}
