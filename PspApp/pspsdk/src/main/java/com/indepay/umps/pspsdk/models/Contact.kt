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

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup


class Contact(
        var displayName: String? = null,
        var communications: ArrayList<Communication> = ArrayList()) :
        ExpandableGroup<Communication>(displayName, communications) {

    var isSelected: Boolean = false
    var selectedCommunication: String? = null

    constructor(displayName: String?) : this(displayName, ArrayList<Communication>())

    override fun equals(other: Any?): Boolean {
        return if (other is Contact) {
            other.displayName.orEmpty().contains(this.displayName.orEmpty())
        } else false
    }

    override fun hashCode(): Int {
        var result = displayName?.hashCode() ?: 0
        result = 31 * result + communications.hashCode()
        result = 31 * result + isSelected.hashCode()
        result = 31 * result + (selectedCommunication?.hashCode() ?: 0)
        return result
    }
}