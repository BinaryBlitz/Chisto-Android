package ru.binaryblitz.Chisto.Adapters

import android.app.Activity
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.binaryblitz.Chisto.Model.Treatment
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Utils.OrderList
import ru.binaryblitz.Chisto.Utils.SwipeToDeleteAdapter
import java.util.*

class EditTreatmentsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), SwipeToDeleteAdapter {

    private var collection = ArrayList<Treatment>()
    private val PENDING_REMOVAL_TIMEOUT: Long = 2000
    var itemsPendingRemoval: ArrayList<Treatment>? = ArrayList()
    var undoOn: Boolean = false

    private val handler = Handler()
    var pendingRunnables: HashMap<Treatment, Runnable> = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_treatment_info, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val treatment = collection[position]

        holder.name.text = treatment.name
        holder.desc.text = treatment.description
        holder.index.text = (position + 1).toString()
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Treatment>) {
        this.collection = collection
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
            collection.removeAt(position)
            OrderList.removeTreatment(collection[position].id)
            notifyDataSetChanged()
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
        val name: TextView
        val desc: TextView
        val index: TextView

        init {
            name = itemView.findViewById(R.id.name) as TextView
            desc = itemView.findViewById(R.id.description) as TextView
            index = itemView.findViewById(R.id.index) as TextView
        }
    }
}