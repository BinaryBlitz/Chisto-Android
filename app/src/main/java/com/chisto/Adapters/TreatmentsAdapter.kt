package com.chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.chisto.Activities.ItemInfoActivity
import com.chisto.Model.Treatment
import com.chisto.R
import java.util.*

class TreatmentsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Treatment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        holder.name.text = collection[position].name
        holder.desc.text = collection[position].description

        if (collection[position].select) {
            holder.checkBox.isChecked = true
        } else {
            holder.checkBox.isChecked = false
        }

        holder.checkBox.setOnCheckedChangeListener { compoundButton, b -> collection[holder.adapterPosition].select = b }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ItemInfoActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Treatment>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val desc: TextView
        val checkBox: CheckBox

        init {
            name = itemView.findViewById(R.id.name) as TextView
            desc = itemView.findViewById(R.id.description) as TextView
            checkBox = itemView.findViewById(R.id.checkBox) as CheckBox
        }
    }
}