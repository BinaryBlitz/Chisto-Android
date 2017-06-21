package ru.binaryblitz.Chisto.entities

import com.google.gson.annotations.SerializedName


data class Category(@SerializedName("id")
                       var id: Int,
                    @SerializedName("name")
                       var name: String,
                    @SerializedName("description")
                       var description: String,
                    @SerializedName("icon_url")
                       var iconUrl: String,
                    @SerializedName("color")
                       var color: String,
                    @SerializedName("featured")
                       var featured: Boolean,
                    @SerializedName("items_count")
                       var itemsCount: Int,
                    @SerializedName("items_preview")
                       var itemsPreview: List<String>
)

