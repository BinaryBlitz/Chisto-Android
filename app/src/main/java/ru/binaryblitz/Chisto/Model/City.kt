package ru.binaryblitz.Chisto.Model

class City(var id: Int, var name: String?, var latitude: Double, var longitude: Double) {

    fun asString(): String {
        return Integer.toString(id) + "entity" + name + "entity" + latitude + "entity" + longitude
    }

    companion object {
        fun fromString(str: String): City {
            val strings = str.split("entity".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            return City(Integer.parseInt(strings[0]), strings[1], strings[2].toDouble(), strings[3].toDouble())
        }
    }
}
