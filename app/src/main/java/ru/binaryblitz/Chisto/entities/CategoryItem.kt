package ru.binaryblitz.Chisto.entities

import android.os.Parcel
import android.os.Parcelable
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
        val isLongTreatment: Boolean) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<CategoryItem> = object : Parcelable.Creator<CategoryItem> {
            override fun createFromParcel(source: Parcel): CategoryItem = CategoryItem(source)
            override fun newArray(size: Int): Array<CategoryItem?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readInt(),
            source.readString(),
            source.readString(),
            source.readString(),
            1 == source.readInt(),
            source.readInt(),
            1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeString(icon)
        dest.writeString(name)
        dest.writeString(description)
        dest.writeInt((if (userArea) 1 else 0))
        dest.writeInt(color)
        dest.writeInt((if (isLongTreatment) 1 else 0))
    }
}

