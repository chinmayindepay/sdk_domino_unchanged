/*
 * ******************************************************************************
 *  * Copyright, INDEPAY 2019 All rights reserved.
 *  *
 *  * The copyright in this work is vested in INDEPAY and the
 *  * information contained herein is confidential.  This
 *  * work (either in whole or in part) must not be modified,
 *  * reproduced, disclosed or disseminated to others or used
 *  * for purposes other than that for which it is supplied,
 *  * without the prior written permission of INDEPAY.  If this
 *  * work or any part hereof is furnished to a third party by
 *  * virtue of a contract with that party, use of this work by
 *  * such party shall be governed by the express contractual
 *  * terms between the INDEPAY which is a party to that contract
 *  * and the said party.
 *  *
 *  * Revision History
 *  * Date           Who        Description
 *  * 06-09-2019     Mayank D   Added file header
 *  *
 *  *****************************************************************************
 */

package com.indepay.umps.pspsdk.models

import android.os.Parcel
import android.os.Parcelable

class Communication(val phoneNumber: String?) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun equals(other: Any?): Boolean {
        return if (other is Communication) {
            other.phoneNumber.orEmpty().contains(this.phoneNumber.toString())
        } else false
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phoneNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        return phoneNumber?.hashCode() ?: 0
    }

    companion object CREATOR : Parcelable.Creator<Communication> {
        override fun createFromParcel(parcel: Parcel): Communication {
            return Communication(parcel)
        }

        override fun newArray(size: Int): Array<Communication?> {
            return arrayOfNulls(size)
        }
    }
}