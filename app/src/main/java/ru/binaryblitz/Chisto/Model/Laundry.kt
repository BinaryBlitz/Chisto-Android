package ru.binaryblitz.Chisto.Model

import java.util.*

data class Laundry(val id: Int, val icon: String, val name: String, val description: String, val rating: Float, val collectionDate: Date?,
                   val deliveryDate: Date?, val deliveryDateOpensAt: Date?, val deliveryDateClosesAt: Date?, var orderPrice: Int?,
                   val index: Int?, val decorationMultipliers: ArrayList<android.support.v4.util.Pair<Int, Double>>?, val deliveryFee: Int?,
                   val freeDeliveryFrom: Int?, var isPassingMinimumPrice: Boolean, var minimumOrderPrice: Int)
