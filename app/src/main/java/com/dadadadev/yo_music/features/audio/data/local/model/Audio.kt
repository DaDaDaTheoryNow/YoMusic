package com.dadadadev.yo_music.features.audio.data.local.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Audio(
    val uri: Uri?,
    val displayName: String,
    val id: Long,
    val artist: String,
    val data: String,
    val duration: Long,
    val title: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        @Suppress("DEPRECATION")
        parcel.readParcelable(Uri::class.java.classLoader),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeString(displayName)
        parcel.writeLong(id)
        parcel.writeString(artist)
        parcel.writeString(data)
        parcel.writeLong(duration)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Audio> {
        override fun createFromParcel(parcel: Parcel): Audio {
            return Audio(parcel)
        }

        override fun newArray(size: Int): Array<Audio?> {
            return arrayOfNulls(size)
        }
    }
}