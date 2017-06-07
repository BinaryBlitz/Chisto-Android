package ru.binaryblitz.Chisto.entities

import com.google.gson.annotations.SerializedName

data class Treatment(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("description")
        val description: String,
        var price: Int, var select: Boolean, var laundryTreatmentId: Int) {

    fun copy(): Treatment {
        return Treatment(id, name, description, price, select, laundryTreatmentId)
    }
}
