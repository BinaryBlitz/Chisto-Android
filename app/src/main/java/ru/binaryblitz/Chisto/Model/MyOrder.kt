package ru.binaryblitz.Chisto.Model

import java.util.Date

class MyOrder(var id: Int, var laundryId: Int, var isPaid: Boolean, var status: MyOrder.Status?, var createAt: Date?, var house: String?,
              var street: String?, var flat: String?, var phone: String?, var note: String?) {

    enum class Status {
        PROCESS,
        COMPLETED,
        CANCELED
    }
}
