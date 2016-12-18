package ru.binaryblitz.Chisto.Model

import java.util.ArrayList

data class Order(val category: CategoryItem, var treatments: ArrayList<Treatment>?,
                 var count: Int, var color: Int, val decoration: Boolean, var decorationCost: Int, var size: Int?)
