package ru.binaryblitz.Chisto.entities

import java.util.*

data class Laundry(
        val id: Int,
        val icon: String,
        val name: String,
        val description: String,
        val rating: Float,
        val collectionDate: Date?,
        val deliveryDate: Date?,
        val deliveryDateOpensAt: Date?,
        val deliveryDateClosesAt: Date?,
        var orderPrice: Int = 0,
        val index: Int?,
        val decorationMultipliers: ArrayList<android.support.v4.util.Pair<Int, Double>>?,
        val deliveryFee: Int = 0,
        val freeDeliveryFrom: Int = 0,
        var isPassingMinimumPrice: Boolean,
        var minimumOrderPrice: Int,
        val collectionDateOpensAt: Date?,
        val collectionDateClosesAt: Date?
)
