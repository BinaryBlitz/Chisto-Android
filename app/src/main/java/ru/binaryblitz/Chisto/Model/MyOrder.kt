package ru.binaryblitz.Chisto.Model

import com.google.gson.JsonObject
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import ru.binaryblitz.Chisto.Utils.DateUtils
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
        createAt = DateUtils.parse(AndroidUtilities.getStringFieldFromJson(obj.get("created_at")))
        price = AndroidUtilities.getIntFieldFromJson(obj.get("total_price"))
    }
}
