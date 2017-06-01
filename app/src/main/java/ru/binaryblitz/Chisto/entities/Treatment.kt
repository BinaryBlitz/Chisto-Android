package ru.binaryblitz.Chisto.entities

data class Treatment(val id: Int, val name: String, val description: String, var price: Int, var select: Boolean, var laundryTreatmentId: Int) {

    fun copy(): Treatment {
        return Treatment(id, name, description, price, select, laundryTreatmentId)
    }
}
