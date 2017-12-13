package ru.binaryblitz.Chisto.ui.start.adapters

import android.app.Activity
import android.location.Location
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ru.binaryblitz.Chisto.R
import ru.binaryblitz.Chisto.extension.inflate
import java.util.*

class CitiesAdapter(
        private val context: Activity,
        private val listener: (ru.binaryblitz.Chisto.entities.City) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class City(val city: ru.binaryblitz.Chisto.entities.City, var selected: Boolean)

    private var collection = ArrayList<City>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            ViewHolder(parent.inflate(R.layout.item_city))

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder
        val city = collection[position].city
        holder.name.text = city.name

        if (collection[position].selected) {
            setSelected(holder)
        } else {
            setDeselected(holder)
        }

        holder.itemView.setOnClickListener { listener.invoke(city) }
    }

    private fun setSelected(holder: ViewHolder) {
        holder.name.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        holder.marker.visibility = View.VISIBLE
    }

    private fun setDeselected(holder: ViewHolder) {
        holder.name.setTextColor(ContextCompat.getColor(context, R.color.greyColor))
        holder.marker.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun setCollection(collection: ArrayList<City>) {
        this.collection = collection
    }

    fun selectCity(latitude: Double, longitude: Double) {
        var position = 0
        var min = Float.MAX_VALUE
        for (i in collection.indices) {
            val dist = distanceBetween(collection[i].city.latitude, collection[i].city.longitude, latitude, longitude)

            if (dist < min) {
                position = i
                min = dist
            }
        }

        collection[position].selected = true
        val city = collection[0]
        collection[0] = collection[position]
        collection[position] = city

        notifyDataSetChanged()
    }

    fun distanceBetween(firstLatitude: Double, firstLongitude: Double, secondLatitude: Double, secondLongitude: Double): Float {
        val startPoint = Location("")
        val endPoint = Location("")

        startPoint.latitude = firstLatitude
        startPoint.longitude = firstLongitude
        endPoint.latitude = secondLatitude
        endPoint.longitude = secondLongitude

        return startPoint.distanceTo(endPoint)
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById(R.id.name) as TextView
        val marker = itemView.findViewById(R.id.marker) as ImageView
    }
}
