package ru.binaryblitz.Chisto.ui.order.adapters

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.refactor.library.SmoothCheckBox
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.entities.Treatment
import java.util.ArrayList

class EditTreatmentsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Treatment>()
    private var color: Int = Color.parseColor("#4bc2f7")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_edit_treatment, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        val treatment = collection[position]

        setColorForCheckboxes(holder.checkBox, color)

        holder.description.text = treatment.description
        holder.checkBox.isChecked = treatment.select
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

    fun setColor(color: String?) {
        if (color!!.isEmpty()) return
        this.color = Color.parseColor(color)
    }

    private fun setCheckedTreatment(position: Int, holder: ViewHolder) {
        collection[position].select = !collection[position].select
        holder.checkBox.isChecked = collection[position].select
    }

    private fun setColorForCheckboxes(checkBox: SmoothCheckBox, color: Int) {
        val checkedColor = SmoothCheckBox::class.java.getDeclaredField("mCheckedColor")
        checkedColor.isAccessible = true
        checkedColor.set(checkBox, color)
    }


    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description = itemView.findViewById(R.id.description) as TextView
        val checkBox = itemView.findViewById(R.id.checkbox) as SmoothCheckBox

        init {
            checkBox.setOnClickListener { }
        }
    }
}
