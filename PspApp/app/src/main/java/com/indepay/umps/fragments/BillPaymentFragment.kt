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
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.indepay.umps.R
import com.indepay.umps.activities.RegistrationActivity
import com.indepay.umps.utils.getStringData
import java.text.SimpleDateFormat
import java.util.*


private const val ARG_LOGO = "logo"

class BillPaymentFragment : Fragment() {

    private var param_logo: Int = 0
    private var listener: OnFragmentInteractionListener? = null

    /**
     * This method is a part of Base app and gets call for Bill Payment functionality,
     *
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param_logo = it.getInt(ARG_LOGO)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_bill_payment, container, false)

        val billerLogo = view.findViewById<ImageView>(R.id.img_biller_logo)
        billerLogo?.setImageResource(param_logo)

        val txtBillAmt = view.findViewById<TextView>(R.id.txt_bill_amt)
        val amt = (100..900).filter { it % 100 == 0 }.random()
        txtBillAmt.text = "Rp.$amt.000"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val txtBillDate = view.findViewById<TextView>(R.id.txt_bill_date)
        txtBillDate.text = dateFormat.format(calendar.time)

        calendar.add(Calendar.DATE, 30)
        val txtDueDate = view.findViewById<TextView>(R.id.txt_due_date)
        txtDueDate.text = dateFormat.format(calendar.time)

        val txtBillNo = view.findViewById<TextView>(R.id.txt_bill_no)
        txtBillNo.text = (154698..451236).random().toString()

        val txtConsumerNo = view.findViewById<TextView>(R.id.txt_consumer_no)
        txtConsumerNo.text = (789456..856974).random().toString()

        val txtAccName = view.findViewById<TextView>(R.id.txt_acc_name)
        txtAccName.text = getStringData(view.context, RegistrationActivity.USER_NAME)

        val btnSubmit = view.findViewById<Button>(R.id.btn_submit)
        btnSubmit.setOnClickListener {
            //To saparate Gas transaction Name "name= "GAS""
            listener?.onFragmentInteraction(amt.toString(), null, name= "GAS")
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        //To saparate Gas transaction Name "name: String"
        fun onFragmentInteraction(amount: String, remarks: String?, name: String)
    }

    companion object {

        @JvmStatic
        fun newInstance(logo: Int) =
                BillPaymentFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_LOGO, logo)
                    }
                }
    }
}
