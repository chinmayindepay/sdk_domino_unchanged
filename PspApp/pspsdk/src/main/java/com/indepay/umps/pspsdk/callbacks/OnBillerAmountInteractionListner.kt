package com.indepay.umps.pspsdk.callbacks

import com.indepay.umps.pspsdk.models.PartnerProductData

interface OnBillerAmountInteractionListner {

    fun onBilleramountListItemClick(billItem: PartnerProductData)

}