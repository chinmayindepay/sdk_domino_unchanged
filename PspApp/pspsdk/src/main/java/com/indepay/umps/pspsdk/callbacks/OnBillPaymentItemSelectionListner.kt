package com.indepay.umps.pspsdk.callbacks

import com.indepay.umps.pspsdk.models.BillPaymentPartnerProductResponse
import com.indepay.umps.pspsdk.models.EcosystemBankResponse
import com.indepay.umps.pspsdk.models.PartnerProductData

interface OnBillPaymentItemSelectionListner {
    fun onBillPaymentPartnerPrepaidListItemClick(billItem: String)
    fun onBillPaymentPartnerPostpaidListItemClick(billItem: String)
}