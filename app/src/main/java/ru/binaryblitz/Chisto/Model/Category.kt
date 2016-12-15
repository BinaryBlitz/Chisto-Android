package ru.binaryblitz.Chisto.Model

import android.text.Spannable

data class Category(val id: Int, val icon: String, val name: String, val desc: Spannable, val color: Int, val featured: Boolean)
