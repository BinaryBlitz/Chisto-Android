package ru.binaryblitz.Chisto.ui.order.adapters

import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.refactor.library.SmoothCheckBox
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Treatment
import ru.binaryblitz.Chisto.utils.OrderList
import java.util.*

class TreatmentsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Treatment>()
    private var color: Int = Color.parseColor("#4bc2f7")
    private var greyColor: Int = Color.parseColor("#CFCFCF")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_service, parent, false)

        return ViewHolder(itemView)
    }

    fun setColor(color: Int) {
        this.color = color
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
        setColorForCheckBox(holder.checkBox, color)

        holder.checkBox.isChecked = treatment.select

        holder.itemView.setOnClickListener {
            treatment.select = !treatment.select
            holder.checkBox.isChecked = treatment.select
        }

        holder.checkBox.setOnCheckedChangeListener { compoundButton, b -> collection[holder.adapterPosition].select = b }
    }

    private fun setColorForCheckBox(checkBox: SmoothCheckBox, color: Int) {
        val checkedColor = SmoothCheckBox::class.java.getDeclaredField("mCheckedColor")
        checkedColor.isAccessible = true
        checkedColor.set(checkBox, color)

        val unCheckedColor = SmoothCheckBox::class.java.getDeclaredField("mUnCheckedColor")
        unCheckedColor.isAccessible = true
        unCheckedColor.set(checkBox, greyColor)
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Treatment>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description = itemView.findViewById(R.id.description) as TextView
        val checkBox = itemView.findViewById(R.id.checkBox) as SmoothCheckBox
    }
}
