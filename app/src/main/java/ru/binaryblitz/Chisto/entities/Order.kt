package ru.binaryblitz.Chisto.entities

import java.util.ArrayList

data class Order(val category: CategoryItem, var treatments: ArrayList<Treatment>?, var count: Int, var color: Int,
                 val decoration: Boolean, var decorationPrice: Int, var size: Double?, var hasItemsWithLongTreatment: Boolean)