package com.indepay.umps.activities

import android.os.Bundle
import com.indepay.umps.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.beneficiary.Contacts.BeneContactsActivity
import com.indepay.umps.pspsdk.paycollectstep.BeneficiaryAddActivity
import kotlinx.android.synthetic.main.pay_collect_activity.*


import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.singleTop


class PayCollectActivity : SdkBaseActivity() {
    private var comingFrom: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pay_collect_activity)
        comingFrom = intent.getStringExtra(SdkBaseActivity.COMING_FROM).toString()
        setData()

    }

    private fun setData() {
        when {
            comingFrom.equals(BeneContactsActivity.TXN_TYPE_PAY, ignoreCase = true) -> {
                fromAccount.setText("PAY TO CONTACT")
                fromBeneficiary.setText("PAY TO BENEFICIARY")

            }
            comingFrom.equals(BeneContactsActivity.TXN_TYPE_COLLECT, ignoreCase = true) -> {
                fromAccount.setText("COLLECT FROM CONTACT")
                fromBeneficiary.setText("COLLECT FROM BENEFICIARY")

            }
        }
        fromAccount.setOnClickListener {
            if (comingFrom.equals(BeneContactsActivity.TXN_TYPE_PAY, ignoreCase = true)) {
                startActivity(intentFor<BeneContactsActivity>(BeneContactsActivity.TRANSACTION_TYPE to BeneContactsActivity.TXN_TYPE_PAY, BeneContactsActivity.USER_TOKEN to  intent.getStringExtra(SdkBaseActivity.USER_TOKEN)).newTask().singleTop())

            } else if (comingFrom.equals(BeneContactsActivity.TXN_TYPE_COLLECT, ignoreCase = true)) {

                startActivity(intentFor<BeneContactsActivity>(BeneContactsActivity.TRANSACTION_TYPE to BeneContactsActivity.TXN_TYPE_COLLECT, BeneContactsActivity.USER_TOKEN to intent.getStringExtra(SdkBaseActivity.USER_TOKEN)).newTask().singleTop())
            }
        }
        fromBeneficiary.setOnClickListener {
            if (comingFrom.equals(BeneContactsActivity.TXN_TYPE_PAY, ignoreCase = true)) {
                startActivity(intentFor<BeneficiaryAddActivity>(BeneContactsActivity.TRANSACTION_TYPE to BeneContactsActivity.TXN_TYPE_PAY, BeneContactsActivity.USER_TOKEN to  intent.getStringExtra(SdkBaseActivity.USER_TOKEN)).newTask().singleTop())

            } else if (comingFrom.equals(BeneContactsActivity.TXN_TYPE_COLLECT, ignoreCase = true)) {
                startActivity(intentFor<BeneficiaryAddActivity>(BeneContactsActivity.TRANSACTION_TYPE to BeneContactsActivity.TXN_TYPE_COLLECT, BeneContactsActivity.USER_TOKEN to  intent.getStringExtra(SdkBaseActivity.USER_TOKEN)).newTask().singleTop())
            }
        }

    }

}