package ru.binaryblitz.Chisto.ui.order.adapters

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.iarcuschin.simpleratingbar.SimpleRatingBar
import ru.binaryblitz.Chisto.entities.Review
import ru.binaryblitz.Chisto.R
import java.text.SimpleDateFormat
import java.util.*

class ReviewsAdapter(private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var collection = ArrayList<Review>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as ViewHolder

        holder.name.text = collection[position].userName
        holder.date.text = getDateFullString(collection[position].date)
        if (collection[position].comment!!.isEmpty()) {
            holder.comment.text = context.getString(R.string.no_comment)
        } else {
            holder.comment.text = collection[position].comment
        }
        holder.rating.rating = collection[position].rating
    }

    override fun getItemCount(): Int {
        return collection.size
    }

    fun getDateFullString(date: Date?): String {
        if (date == null) return ""

        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(date)
    }


    fun setCollection(collection: ArrayList<Review>) {
        this.collection = collection
    }

    private inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById(R.id.user_text) as TextView
        val date = itemView.findViewById(R.id.date_text) as TextView
        val comment = itemView.findViewById(R.id.comment_text) as TextView
        val rating = itemView.findViewById(R.id.ratings) as SimpleRatingBar
    }
}
