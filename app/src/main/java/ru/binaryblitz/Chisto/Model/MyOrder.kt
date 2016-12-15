package ru.binaryblitz.Chisto.Model

import com.google.gson.JsonObject
import ru.binaryblitz.Chisto.Utils.AndroidUtilities
import java.text.SimpleDateFormat
import java.util.*

class MyOrder {
    val id: Int
    val laundryId: Int
    val isPaid: Boolean
    val status: MyOrder.Status
    val createAt: Date
    val house: String
    val street: String
    val flat: String
    val phone: String
    val note: String
    val cost: Int

    constructor(obj: JsonObject) {
        id = AndroidUtilities.getIntFieldFromJson(obj.get("id"))
        laundryId = AndroidUtilities.getIntFieldFromJson(obj.get("laundry_id"))
        isPaid = AndroidUtilities.getBooleanFieldFromJson(obj.get("paid"))
        status = getStatusFromJson(obj)
        createAt = getDateFromJson(obj)!!
        house = AndroidUtilities.getStringFieldFromJson(obj.get("house"))
        street = AndroidUtilities.getStringFieldFromJson(obj.get("street"))
        flat = AndroidUtilities.getStringFieldFromJson(obj.get("flat"))
        phone = AndroidUtilities.getStringFieldFromJson(obj.get("contact_number"))
        note = AndroidUtilities.getStringFieldFromJson(obj.get("notes"))
        cost = AndroidUtilities.getIntFieldFromJson(obj.get("total_price"))
    }

    private fun getStatusFromJson(obj: JsonObject): MyOrder.Status {
        return if (obj.get("status").asString == "processing") MyOrder.Status.PROCESS
        else if (obj.get("status").asString == "completed") MyOrder.Status.COMPLETED else MyOrder.Status.CANCELED
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
        CANCELED
    }
}
