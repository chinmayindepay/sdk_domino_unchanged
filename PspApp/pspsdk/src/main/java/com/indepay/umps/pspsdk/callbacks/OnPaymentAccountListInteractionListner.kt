package com.indepay.umps.pspsdk.callbacks

import com.indepay.umps.pspsdk.models.CreditCardMappedAccount
import com.indepay.umps.pspsdk.models.MappedAccount

interface OnPaymentAccountListInteractionListner {

    fun onSetDefaultAccount(mappedAccount: MappedAccount)
    fun onSetDefaultAccount(position:Int)
    fun onSetCreditCardAccount(creditcardmappedAccount: CreditCardMappedAccount)
    fun onSetCreditCardAccount(position:Int)

//    fun onItemView(creditcardmappedAccount: CreditCardMappedAccount)
}