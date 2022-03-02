package com.indepay.umps.spl.models

import com.google.gson.annotations.SerializedName
import com.indepay.umps.spl.models.DeviceInfo
import com.indepay.umps.spl.models.PaymentInstrument

data class OtpFetchRetrievalPayload (
        @SerializedName("action") val action: String? ,
        @SerializedName("paymentInstrument") val paymentInstrument: PaymentInstrument? = PaymentInstrument(),
        @SerializedName("deviceInfo") val deviceInfo: DeviceInfo
)