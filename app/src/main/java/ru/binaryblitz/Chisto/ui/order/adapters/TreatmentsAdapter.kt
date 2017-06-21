package ru.binaryblitz.Chisto.ui.order.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.widget.AppCompatRadioButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Treatment
import ru.binaryblitz.Chisto.utils.OrderList
import java.util.ArrayList

class TreatmentsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Treatment>()
    private var color: Int = Color.parseColor("#4bc2f7")
    private var lastCheckedPosition = 0
    private var isFirstSelection: Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)

        return ViewHolder(itemView)
    }

    fun setColor(color: String?) {
        if (color!!.isEmpty()) return
        this.color = Color.parseColor(color)
    }

    fun add(treatment: Treatment) {
        collection.add(0, treatment)

        main_loop@ for (i in collection.indices) {
            for (j in 0..OrderList.getTreatments()!!.size - 1) {
                if (collection[i].id == OrderList.getTreatments()!![j].id) {
                    collection[i].select = true
                    continue@main_loop
                }
            }
        }
    }

    fun getSelected(): ArrayList<Treatment> {
        val selected = collection.indices
                .filter { collection[it].select }
                .mapTo(ArrayList <Treatment>()) { collection[it] }

        return selected
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val treatment = collection[position]

        holder.description.text = treatment.description
        setColorForRadioButton(holder.radioButton, color)

        holder.radioButton.isChecked = position == lastCheckedPosition
        if (position == 0 && isFirstSelection) {
            setCheckedTreatment(position)
            isFirstSelection = false
        }
    }

    private fun setCheckedTreatment(position: Int) {
        for (i in 0..collection.size - 1) {
            collection[i].select = false
        }
        collection[position].select = true
        val a: Int
    }

    private fun setColorForRadioButton(radioButton: AppCompatRadioButton, color: Int) {
        radioButton.supportButtonTintList = ColorStateList.valueOf(color)
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Treatment>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description = itemView.findViewById(R.id.description) as TextView
        val radioButton = itemView.findViewById(R.id.radioButton) as AppCompatRadioButton

        init {
            radioButton.supportButtonTintMode
            radioButton.supportButtonTintList
            itemView.setOnClickListener {
                selectTreatment()
            }
            radioButton.setOnClickListener {
                selectTreatment()
            }
        }
    }

    private fun ViewHolder.selectTreatment() {
        setCheckedTreatment(adapterPosition)
        lastCheckedPosition = adapterPosition
        notifyItemRangeChanged(0, collection.size)
    }
}
