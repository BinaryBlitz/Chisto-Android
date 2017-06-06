package ru.binaryblitz.Chisto.entities

import com.google.gson.annotations.SerializedName

data class CategoryItem(
        @SerializedName("id")
        val id: Int,
        @SerializedName("icon_url")
        val icon: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("use_area")
        val userArea: Boolean,
        val color: Int,
        @SerializedName("long_treatment")
        val isLongTreatment: Boolean)

