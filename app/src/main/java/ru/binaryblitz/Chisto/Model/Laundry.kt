package ru.binaryblitz.Chisto.Model

import java.util.*

class Laundry(val id: Int, val icon: String, val name: String, val desc: String, val type: Type, val rating: Float,
              val collectionDate: Date?, val deliveryDate: Date?, val deliveryDateOpensAt: Date?, val deliveryDateClosesAt: Date?,
              val deliveryCost: Int?, val orderCost: Int?) {
    enum class Type {
        PREMIUM,
        ECONOMY,
        FAST,
        EMPTY
    }
}
