package ru.binaryblitz.Chisto.entities

import android.text.Spannable

data class Category(val id: Int, val icon: String, val name: String, val description: Spannable, val color: Int, val featured: Boolean)
