package ru.binaryblitz.Chisto.Adapters

import android.app.Activity
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.binaryblitz.Chisto.Activities.OrdersActivity
import ru.binaryblitz.Chisto.Activities.SelectCityActivity
import ru.binaryblitz.Chisto.Model.User
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.Server.DeviceInfoStore
import java.util.*

class CitiesAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class City(val city: ru.binaryblitz.Chisto.Model.City, var selected: Boolean)

    private var collection = ArrayList<City>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        holder.name.text = collection[position].city.name

        if (collection[position].selected) {
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            holder.marker.visibility = View.VISIBLE
        } else {
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.greyColor))
            holder.marker.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, OrdersActivity::class.java)
            DeviceInfoStore.saveCity(context, collection[position].city)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            DeviceInfoStore.saveUser(context, User(1, null, null, null, collection[position].name, null, null, null))
            context.finish()
        }
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<City>) {
        this.collection = collection
    }

    fun selectCity(cityName: String) {
        var position = 0
        for (i in collection.indices) {
            if (collection[i].city.name == cityName) {
                position = i
            }

            if (i == collection.size - 1) {
                (context as SelectCityActivity).cityError()
                return
            }
        }
        collection[position].selected = true
        val city = collection[0]
        collection[0] = collection[position]
        collection[position] = city

        notifyDataSetChanged()
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val marker: ImageView

        init {
            name = itemView.findViewById(R.id.name) as TextView
            marker = itemView.findViewById(R.id.marker) as ImageView
        }
    }
}