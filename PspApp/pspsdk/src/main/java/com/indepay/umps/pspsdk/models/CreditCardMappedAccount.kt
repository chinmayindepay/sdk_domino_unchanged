package com.indepay.umps.pspsdk.models

import com.google.gson.annotations.SerializedName

data class CreditCardMappedAccount (
        @SerializedName("token") val token: String?=null,
        @SerializedName("callbackResponsePk") val callbackResponsePk: CreditCardId? = null,
         @SerializedName("amount") val amount: String? = null,
         @SerializedName("bankIssuer") val bankIssuer: String? = null,
         @SerializedName("cardBrand") val cardBrand: String? = null,
         @SerializedName("cardExpDate") val cardExpDate: String? = null,
         @SerializedName("cardHolderName") val cardHolderName: String? = null,
         @SerializedName("maskedCardNumber") val maskedCardNumber: String? = null,
         @SerializedName("status") val status: String? = null,
         @SerializedName("isSelected") var isSelected: Boolean = false,
         @SerializedName("addnew") var addnew: String? = null



)

//{
//    "token": "615e68b5e7ad88c31c999eb04d597e5808897b49097b652f517ce13b7600dd66",
//    "maskedCardNumber": "352812******9012",
//    "callbackResponsePk": {
//    "mcPaymentCardId": "192530451f2e4d2cb149d354c67d3b09",
//    "customerId": 39
//        },
//    "amount": 0,
//    "bankIssuer": "SAISON, JAPAN",
//    "cardBrand": "JCB",
//    "cardExpDate": "2105",
//    "cardHolderName": "Test",
//    "status": "success"
//}