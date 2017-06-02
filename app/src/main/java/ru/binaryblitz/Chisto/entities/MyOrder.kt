package ru.binaryblitz.Chisto.entities

import com.google.gson.JsonObject
import ru.binaryblitz.Chisto.utils.AndroidUtilities
import ru.binaryblitz.Chisto.utils.DateUtils
import java.util.*

class MyOrder(obj: JsonObject) {
    val id: Int = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
    val laundryId: Int = AndroidUtilities.getIntFieldFromJson(obj.get("laundry_id"))
    val isPaid: Boolean = AndroidUtilities.getBooleanFieldFromJson(obj.get("paid"))
    val status: MyOrder.Status
    val createdAt: Date = DateUtils.parse(AndroidUtilities.getStringFieldFromJson(obj.get("created_at")))
    val price: Int = AndroidUtilities.getIntFieldFromJson(obj.get("total_price"))

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

    enum class Status {
        PROCESS,
        COMPLETED,
        CANCELED,
        CLEANING,
        DISPATCHED,
        CONFIRMED
    }

    init {
        status = getStatusFromJson(obj)
    }
}
