package ru.binaryblitz.Chisto.Model

class Laundry(val id: Int, val icon: String, val name: String, val desc: String, val type: Type) {
    enum class Type {
        PREMIUM,
        ECONOMY,
        FAST,
        EMPTY
    }
}
