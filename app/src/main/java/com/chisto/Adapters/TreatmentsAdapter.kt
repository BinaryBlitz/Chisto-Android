package com.chisto.Adapters

import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.chisto.Custom.CheckBox.SmoothCheckBox
import com.chisto.Model.Treatment
import com.chisto.R
import java.util.*

class TreatmentsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Treatment>()
    private var color: Int = Color.parseColor("#212121")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)

        return ViewHolder(itemView)
    }

    fun setColor(color: Int) {
        this.color = color
    }

    fun getSelected(): ArrayList<Treatment> {
        val selected = ArrayList <Treatment>()
        for (i in collection.indices) {
            if (collection[i].select) {
                selected.add(collection[i])
            }
        }

        return selected
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
        holder.checkBox.setmCheckedColor(color)
        holder.checkBox.setOnCheckedChangeListener { compoundButton, b -> collection[holder.adapterPosition].select = b }
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
        val checkBox: SmoothCheckBox

        init {
            name = itemView.findViewById(R.id.name) as TextView
            desc = itemView.findViewById(R.id.description) as TextView
            checkBox = itemView.findViewById(R.id.checkBox) as SmoothCheckBox
        }
    }
}