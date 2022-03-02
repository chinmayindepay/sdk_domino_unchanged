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

package com.indepay.umps.spl.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.Keep
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SimpleExpandableListAdapter
import android.widget.Toast
import com.indepay.umps.spl.R
import com.indepay.umps.spl.models.TransactionType
import com.indepay.umps.spl.pinpad.PinpadView
import kotlinx.android.synthetic.main.activity_pinpad.*
import kotlinx.android.synthetic.main.dialog_expanded_transaction_details.view.*
import org.jetbrains.anko.alert

class PinPadActivity : AppCompatActivity(), PinpadView.Callback, View.OnClickListener {

    private var context :Context? = null
    private var isConfirmationRequired: Boolean = false
    private lateinit var expandableListAdapter: SimpleExpandableListAdapter

    @Keep
    companion object {
        const val IS_REQUIRED_CONFIRMATION = "confirmation"   // true or false
        const val MPIN = "mpin"
        const val AMOUNT = "amount"
        const val PAYEE_NAME = "payee_name"
        const val BANK_NAME = "bank_name"
        const val ACCOUNT_NO = "account"
        const val NOTE = "note"
        const val TXN_ID = "txn_id"
        const val TXN_TYPE = "TXN_TYPE"
    }

    /**
     * This method is a part of SPL, to authenticate the transaction details.
     *              and Calling for Pin Pad activity.
     * @param
     * @return
     */

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pinpad)
        view_pinpad.viewProvider = txt_pin
        txt_pin.setOnClickListener{
            txt_confirm_pin.isActivated = false
            txt_pin.isActivated = true
            view_pinpad.viewProvider = txt_pin
        }
        txt_confirm_pin.setOnClickListener{
            txt_pin.isActivated = false
            txt_confirm_pin.isActivated = true
            view_pinpad.viewProvider = txt_confirm_pin
        }

        toggle_txt_pin.setOnClickListener {
            txt_pin.isTextHidden = !txt_pin.isTextHidden
            txt_pin.invalidate()
            if (txt_pin.isTextHidden) {
                toggle_txt_pin.setImageResource(R.drawable.ic_visibility_off)
            }else{
                toggle_txt_pin.setImageResource(R.drawable.ic_visibility_on)
            }
        }

        toggle_txt_confirm_pin.setOnClickListener {
            txt_confirm_pin.isTextHidden = !txt_confirm_pin.isTextHidden
            txt_confirm_pin.invalidate()
            if(txt_confirm_pin.isTextHidden) {
                toggle_txt_confirm_pin.setImageResource(R.drawable.ic_visibility_off)
            }else{
                toggle_txt_confirm_pin.setImageResource(R.drawable.ic_visibility_on)
            }
        }

        val transactionType = TransactionType.fromValue(intent.getStringExtra(TXN_TYPE).toString())
        var allowExpandedDetails: Boolean = false
        when (transactionType){
            TransactionType.BALANCE_ENQUIRY -> {
                header_name.text = "Account"
                header_value.text = intent.getStringExtra(ACCOUNT_NO)
                allowExpandedDetails = true
            }
            TransactionType.FINANCIAL_TXN -> {
                header_name.text = intent.getStringExtra(PAYEE_NAME)
                header_value.text = intent.getStringExtra(AMOUNT)
                allowExpandedDetails = true
            }
            TransactionType.CHANGE_PIN -> {
                ll_additional_details.visibility = View.GONE
            }
            TransactionType.SET_PIN -> {
                ll_additional_details.visibility = View.GONE
            }
        }
        if(allowExpandedDetails) {
            iv_expand_details.setOnClickListener {
                val expandedDetailsView = layoutInflater.inflate(R.layout.dialog_expanded_transaction_details, null)
                expandedDetailsView.tv_payee_name.text = intent.getStringExtra(PAYEE_NAME)
                expandedDetailsView.tv_amount_name.text = intent.getStringExtra(AMOUNT)
                expandedDetailsView.tv_account_no_name.text = intent.getStringExtra(ACCOUNT_NO)
                expandedDetailsView.tv_bic_name.text = intent.getStringExtra(BANK_NAME)
                expandedDetailsView.tv_note_name.text = intent.getStringExtra(NOTE)
                expandedDetailsView.tv_txn_id_name.text = intent.getStringExtra(TXN_ID)
                alert {
                    customView = expandedDetailsView
                }.show()
            }
        }

        view_pinpad.callback = this
        context = this
        isConfirmationRequired = intent.getBooleanExtra(IS_REQUIRED_CONFIRMATION, false)
        if (!isConfirmationRequired) {
            lbl_confirm_pin.visibility = View.GONE
            txt_confirm_pin.visibility = View.GONE
            toggle_txt_confirm_pin.visibility = View.GONE
        }
        //capturePin()

    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            txt_pin.id -> {

            }
            txt_confirm_pin.id -> {

            }
        }
    }

    override fun onPressSubmit(passcode: String) {

        if (isConfirmationRequired) {
            when {
                txt_pin.text.isEmpty().or(txt_pin.text.isBlank()) -> Toast.makeText(this, resources.getString(R.string.spl_pin_cannot_blank), Toast.LENGTH_SHORT).show()
                txt_confirm_pin.text.isEmpty().or(txt_confirm_pin.text.isBlank()) -> Toast.makeText(this, resources.getString(R.string.spl_confirm_pin), Toast.LENGTH_SHORT).show()
                txt_pin.text.trim().toString() == txt_confirm_pin.text.trim().toString() -> {
                    val returnIntent = Intent()
                    returnIntent.putExtra(MPIN, txt_pin.text.toString())
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
                else -> view_pinpad.fail()
            }
        } else {
            when {
                txt_pin.text.isEmpty().or(txt_pin.text.isBlank()) -> Toast.makeText(this, resources.getString(R.string.spl_pin_cannot_blank), Toast.LENGTH_SHORT).show()

                else -> {
                    val returnIntent = Intent()
                    returnIntent.putExtra(MPIN, txt_pin.text.toString())
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }

            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        setResult(Activity.RESULT_CANCELED)
        finish()
    }

}