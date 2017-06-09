package ru.binaryblitz.Chisto.ui.order.adapters

import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.refactor.library.SmoothCheckBox
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.R.color.greyColor
import ru.binaryblitz.Chisto.entities.Treatment
import ru.binaryblitz.Chisto.ui.order.ItemInfoActivity
import ru.binaryblitz.Chisto.utils.SwipeToDeleteAdapter
import java.util.ArrayList
import java.util.HashMap

class EditTreatmentsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), SwipeToDeleteAdapter {

    private var collection = ArrayList<Treatment>()
    private val PENDING_REMOVAL_TIMEOUT: Long = 2000
    var itemsPendingRemoval: ArrayList<Treatment>? = ArrayList()
    var undoOn: Boolean = false
    private var color: Int = Color.parseColor("#4bc2f7")

    private val handler = Handler()
    var pendingRunnables: HashMap<Treatment, Runnable> = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val treatment = collection[position]

        setColorForCheckBox(holder.checkBox, color)

        holder.description.text = treatment.description
        holder.checkBox.isChecked = treatment.select

        holder.itemView.setOnClickListener {
            setCheckedTreatment(treatment, holder)
        }

        holder.checkBox.setOnCheckedChangeListener { compoundButton, b -> collection[holder.adapterPosition].select = b }
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Treatment>) {
        this.collection = collection
    }

    fun getCollection(): ArrayList<Treatment> {
        return collection
    }

    fun getSelected(): ArrayList<Treatment> {
        val selected = collection.indices
                .filter { collection[it].select }
                .mapTo(ArrayList <Treatment>()) { collection[it] }

        return selected
    }

    fun setColor(color: Int) {
        this.color = color
    }

    private fun setCheckedTreatment(treatment: Treatment, holder: ViewHolder) {
        treatment.select = !treatment.select
        holder.checkBox.isChecked = treatment.select
    }

    private fun setColorForCheckBox(checkBox: SmoothCheckBox, color: Int) {
        val checkedColor = SmoothCheckBox::class.java.getDeclaredField("mCheckedColor")
        checkedColor.isAccessible = true
        checkedColor.set(checkBox, color)

        val unCheckedColor = SmoothCheckBox::class.java.getDeclaredField("mUnCheckedColor")
        unCheckedColor.isAccessible = true
        unCheckedColor.set(checkBox, greyColor)
    }

    override fun pendingRemoval(position: Int) {
        val item = collection[position]
        if (!itemsPendingRemoval!!.contains(item)) {
            itemsPendingRemoval!!.add(item)
            notifyItemChanged(position)
            val pendingRemovalRunnable = Runnable { remove(collection.indexOf(item)) }
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT)
            pendingRunnables.put(item, pendingRemovalRunnable)
        }
    }

    override fun remove(position: Int) {
        val item = collection[position]
        if (itemsPendingRemoval!!.contains(item)) {
            itemsPendingRemoval!!.remove(item)
        }
        if (collection.contains(item)) {
            collection.remove(item)
            notifyDataSetChanged()
        }

        if (collection.size == 0) {
            collection.add(item)
            notifyDataSetChanged()
            (context as ItemInfoActivity).onRemovalError()
        }
    }

    override fun isUndo(): Boolean {
        return undoOn
    }

    override fun isPendingRemoval(position: Int): Boolean {
        val item = collection[position]
        return itemsPendingRemoval!!.contains(item)
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description = itemView.findViewById(R.id.description) as TextView
        val checkBox = itemView.findViewById(R.id.checkBox) as SmoothCheckBox
    }
}
