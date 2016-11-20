package ru.binaryblitz.Chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.binaryblitz.Chisto.Activities.LaundryAndOrderActivity
import ru.binaryblitz.Chisto.Model.Laundry
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Utils.Image
import java.util.*

class LaundriesAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Laundry>()

    init {
        Image.init(context)
        collection = ArrayList<Laundry>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_laundry, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        holder.name.text = collection[position].name
        holder.desc.text = collection[position].desc
        holder.category.text = collection[position].type

        Image.loadPhoto(collection[position].icon, holder.icon)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, LaundryAndOrderActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<Laundry>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val desc: TextView
        val category: TextView
        val icon: ImageView

        init {
            name = itemView.findViewById(R.id.name) as TextView
            desc = itemView.findViewById(R.id.description) as TextView
            category = itemView.findViewById(R.id.type_text) as TextView
            icon = itemView.findViewById(R.id.category_icon) as ImageView
        }
    }
}