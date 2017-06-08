package ru.binaryblitz.Chisto.entities

import com.google.gson.annotations.SerializedName

data class Treatment(
        @SerializedName("id")
        val id: Int,
        @SerializedName("description")
        val name: String,
        @SerializedName("name")
        val description: String,
        var price: Int,
        var select: Boolean,
        var laundryTreatmentId: Int) {

    fun copy(): Treatment {
        return Treatment(id, name, description, price, select, laundryTreatmentId)
    }
}
