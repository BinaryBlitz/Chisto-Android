package ru.binaryblitz.Chisto

import android.content.Context


class ResourceManager(private val context: Context) {
    fun getString(id: Int): String = context.getString(id)
}
