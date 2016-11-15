package ru.binaryblitz.Chisto.Model

class User(var id: Int, var name: String?, var lastname: String?, var phone: String?,
           var city: String?, var street: String?, var house: String?, var flat: String?) {

    fun asString(): String {
        return Integer.toString(id) + "entity" + name + "entity" + lastname + "entity" + phone +
                "entity" + city + "entity" + street + "entity" + house + "entity" + flat
    }

    companion object {
        fun fromString(str: String): User {
            val strings = str.split("entity".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            return User(Integer.parseInt(strings[0]), strings[1], strings[2], strings[3], strings[4], strings[5], strings[6], strings[7])
        }
    }
}
