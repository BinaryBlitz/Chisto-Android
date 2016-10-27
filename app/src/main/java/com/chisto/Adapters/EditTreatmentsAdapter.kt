package com.chisto.Adapters

import android.app.Activity
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chisto.Model.Treatment
import com.chisto.R
import com.chisto.Utils.OrderList
import java.util.*

class EditTreatmentsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Treatment>()
    private val PENDING_REMOVAL_TIMEOUT: Long = 2000
    var itemsPendingRemoval: ArrayList<Treatment>? = null
    var undoOn: Boolean = false

    private val handler = Handler()
    var pendingRunnables: HashMap<Treatment, Runnable> = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        holder.name.text = collection[position].name
        holder.desc.text = collection[position].description
        holder.index.text = position.toString()
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Treatment>) {
        this.collection = collection
    }

    fun pendingRemoval(position: Int) {
        val item = collection.get(position)
        if (!itemsPendingRemoval!!.contains(item)) {
            itemsPendingRemoval!!.add(item)
            notifyItemChanged(position)
            val pendingRemovalRunnable = Runnable { remove(collection.indexOf(item)) }
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT)
            pendingRunnables.put(item, pendingRemovalRunnable)
        }
    }

    fun remove(position: Int) {
        val item = collection.get(position)
        if (itemsPendingRemoval!!.contains(item)) {
            itemsPendingRemoval!!.remove(item)
        }
        if (collection.contains(item)) {
            collection.removeAt(position)
            OrderList.removeTreatment(collection[position].id)
            notifyDataSetChanged()
        }
    }

    fun isPendingRemoval(position: Int): Boolean {
        val item = collection.get(position)
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