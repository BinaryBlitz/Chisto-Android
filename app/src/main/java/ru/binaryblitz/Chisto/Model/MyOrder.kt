package ru.binaryblitz.Chisto.Model

import com.google.gson.JsonObject
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import java.text.SimpleDateFormat
import java.util.*

class MyOrder(obj: JsonObject) {
    val id: Int
    val laundryId: Int
    val isPaid: Boolean
    val status: MyOrder.Status
    val createAt: Date
    val price: Int

    private fun getStatusFromJson(obj: JsonObject): MyOrder.Status {
        val status = obj.get("status").asString
        when (status) {
            "processing" -> return MyOrder.Status.PROCESS
            "completed" -> return MyOrder.Status.COMPLETED
            "canceled" -> return MyOrder.Status.CANCELED
            "dispatched" -> return MyOrder.Status.DISPATCHED
            "cleaning" -> return MyOrder.Status.CLEANING
            "confirmed" -> return MyOrder.Status.CONFIRMED
            else -> return MyOrder.Status.PROCESS
        }
    }


    private fun getDateFromJson(obj: JsonObject): Date? {
        var date: Date? = null
        try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            date = format.parse(obj.get("created_at").asString)
        } catch (ignored: Exception) { }

        return date
    }

    enum class Status {
        PROCESS,
        COMPLETED,
        CANCELED,
        CLEANING,
        DISPATCHED,
        CONFIRMED
    }

    init {
        id = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        laundryId = AndroidUtilities.getIntFieldFromJson(obj.get("laundry_id"))
        isPaid = AndroidUtilities.getBooleanFieldFromJson(obj.get("paid"))
        status = getStatusFromJson(obj)
        createAt = getDateFromJson(obj)!!
        price = AndroidUtilities.getIntFieldFromJson(obj.get("total_price"))
    }
}
