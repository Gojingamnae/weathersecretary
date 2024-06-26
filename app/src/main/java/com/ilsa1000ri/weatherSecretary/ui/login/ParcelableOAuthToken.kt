package com.ilsa1000ri.weatherSecretary.ui.login

import android.os.Parcel
import android.os.Parcelable
import com.kakao.sdk.auth.model.OAuthToken

data class ParcelableOAuthToken(
    val accessToken: String?,
    val accessTokenExpiresAt: Long?,
    val refreshToken: String?,
    val refreshTokenExpiresAt: Long?,
    val idToken: String?,
    val scopes: List<String>?
) : Parcelable {
    constructor(token: OAuthToken) : this(
        token.accessToken,
        token.accessTokenExpiresAt?.time,
        token.refreshToken,
        token.refreshTokenExpiresAt?.time,
        token.idToken,
        token.scopes
    )

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.createStringArrayList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(accessToken)
        parcel.writeLong(accessTokenExpiresAt ?: -1)
        parcel.writeString(refreshToken)
        parcel.writeLong(refreshTokenExpiresAt ?: -1)
        parcel.writeString(idToken)
        parcel.writeStringList(scopes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableOAuthToken> {
        override fun createFromParcel(parcel: Parcel): ParcelableOAuthToken {
            return ParcelableOAuthToken(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableOAuthToken?> {
            return arrayOfNulls(size)
        }
    }
}
