package ru.binaryblitz.Chisto.Model

data class Treatment(val id: Int, val name: String, val description: String, var cost: Int, var select: Boolean) {

    fun copy(): Treatment {
        return Treatment(id, name, description, cost, select)
    }
}
