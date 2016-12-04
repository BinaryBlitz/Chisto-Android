package ru.binaryblitz.Chisto.Model

import java.util.*

data class Laundry(val id: Int, val icon: String, val name: String, val desc: String, val rating: Float, val collectionDate: Date?,
              val deliveryDate: Date?, val deliveryDateOpensAt: Date?, val deliveryDateClosesAt: Date?, val deliveryCost: Int?,
              val orderCost: Int?)
