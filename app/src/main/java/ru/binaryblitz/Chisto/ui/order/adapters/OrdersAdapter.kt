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
import timber.log.Timber
import java.util.*

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

        holder.icon.setColorFilter(Color.parseColor(order.category.categoryColor))
        setItemViewSelection(holder, holder.itemView)

        holder.itemView.setOnClickListener {
            if (!isSelectionEnabled) {
                openItemInfoScreen(holder)
                return@setOnClickListener
            }

            if (holder.itemView.isSelected) {
                removeSelectedItem(holder, holder.itemView, collection[position].category.id)
            } else {
                addSelectedItem(viewHolder, viewHolder.itemView, position)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (isSelectionEnabled) {
                removeSelectedItem(viewHolder, viewHolder.itemView, position)
            } else {
                addSelectedItem(viewHolder, viewHolder.itemView, position)
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

    private fun setItemViewSelection(holder: ViewHolder, itemView: View) {
        if (itemView.isSelected) {
            holder.selectionIcon.visibility = View.VISIBLE
            holder.icon.visibility = View.GONE
        } else {
            holder.icon.visibility = View.VISIBLE
            holder.selectionIcon.visibility = View.GONE
        }
    }

    fun setItemColor(color: String) {
        if (color.isEmpty()) return
        this.color = color
    }

    private fun addSelectedItem(holder: ViewHolder, itemView: View, position: Int): Boolean {
        selectItemView(holder, itemView, true)
        selectedPositions.add(collection[position])
        onItemSelectAction.onNext(!selectedPositions.isEmpty())
        isSelectionEnabled = true
        return true
    }

    private fun removeSelectedItem(holder: ViewHolder, itemView: View, categoryId: Int): Boolean {
        selectItemView(holder, itemView, false)
        selectedPositions = ArrayList(selectedPositions.filter { it.category.id != categoryId })
        onItemSelectAction.onNext(!selectedPositions.isEmpty())
        if (selectedPositions.isEmpty()) {
            isSelectionEnabled = false
        }
        return false
    }

    private fun selectItemView(holder: ViewHolder, itemView: View, isSelected: Boolean) {
        itemView.isSelected = isSelected
        setItemViewSelection(holder, itemView)
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

    override fun getItemCount(): Int = collection.size

    fun setCollection(collection: ArrayList<Order>) {
        Timber.d(collection.toString())
        this.collection = collection
        notifyDataSetChanged()
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById(R.id.name) as TextView
        val description = itemView.findViewById(R.id.description) as TextView
        val count = itemView.findViewById(R.id.count) as TextView
        val icon = itemView.findViewById(R.id.category_icon) as ImageView
        val selectionIcon = itemView.findViewById(R.id.selection_icon) as ImageView
    }
}
