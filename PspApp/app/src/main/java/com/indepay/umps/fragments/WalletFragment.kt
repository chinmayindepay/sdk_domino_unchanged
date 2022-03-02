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

package com.indepay.umps.fragments

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.indepay.umps.R
import com.indepay.umps.pspsdk.utils.orNull
import com.indepay.umps.utils.getWalletBalance
import kotlinx.android.synthetic.main.fragment_wallet.*

class WalletFragment : Fragment() {

    private var listener: OnWalletFragmentInteractionListener? = null

    /**
     * This method is a part of Base app and gets call for Wallet top-up functionality,
     *
     */


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_wallet, container, false)

        val txtWalletBal = view.findViewById<TextView>(R.id.txt_wallet_bal)
        var balAmt = getWalletBalance(view.context)
        if (balAmt.isNullOrEmpty()) {
            balAmt = "0"
        }
        val balAmtTxt: String = (balAmt.toDouble().toLong()).toString()
        txtWalletBal.text = "Rp.$balAmtTxt"

        val btnSubmit = view.findViewById<Button>(R.id.button_proceed2)
        btnSubmit.setOnClickListener {
            if (isValidate()) {
                listener?.onWalletFragmentInteraction(edt_txn_amount2.text.toString(), edt_remarks2.text.toString().orNull(),name = "myWallet")
            }
            //To saparate Wallet transaction Name "name = "myWallet""

        }
        return view
    }



    fun enableSubmitIfReady() {

        val isReady = edt_txn_amount2.getText().toString().isNotEmpty()
        button_proceed2.isEnabled = isReady
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnWalletFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnWalletFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnWalletFragmentInteractionListener {
        //To saparate Wallet transaction Name "name: String"
        fun onWalletFragmentInteraction(amount: String, remarks: String?, name: String)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
                WalletFragment().apply {
                    /*arguments = Bundle().apply {
                        //putInt(ARG_LOGO, logo)

                    }*/
                }
    }

    private fun isValidate(): Boolean {
        when {
            TextUtils.isEmpty(edt_txn_amount2.text.toString()) -> {
                Snackbar.make(txt_wallet_bal, "Please enter amount.", Snackbar.LENGTH_SHORT).show()
                return false
            }else -> return true
        }
    }


}
