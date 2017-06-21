package ru.binaryblitz.Chisto.entities

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList

data class Order(val category: CategoryItem, var treatments: ArrayList<Treatment>?, var count: Int,
                 var color: Int,
                 val decoration: Boolean, var decorationPrice: Int, var size: Double?,
                 var hasItemsWithLongTreatment: Boolean) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Order> = object : Parcelable.Creator<Order> {
            override fun createFromParcel(source: Parcel): Order = Order(source)
            override fun newArray(size: Int): Array<Order?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readParcelable<CategoryItem>(CategoryItem::class.java.classLoader),
            ArrayList<Treatment>().apply { source.readList(this, Treatment::class.java.classLoader) },
            source.readInt(),
            source.readInt(),
            1 == source.readInt(),
            source.readInt(),
            source.readValue(Double::class.java.classLoader) as Double?,
            1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(category, 0)
        dest.writeList(treatments)
        dest.writeInt(count)
        dest.writeInt(color)
        dest.writeInt((if (decoration) 1 else 0))
        dest.writeInt(decorationPrice)
        dest.writeValue(size)
        dest.writeInt((if (hasItemsWithLongTreatment) 1 else 0))
    }
}
