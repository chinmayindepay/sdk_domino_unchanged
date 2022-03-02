package com.indepay.umps.pspsdk.adapter

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat.getDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.R.drawable
import com.indepay.umps.pspsdk.R.drawable.*
import com.indepay.umps.pspsdk.models.CreditCardMappedAccount
import com.indepay.umps.pspsdk.models.MappedAccount
import com.indepay.umps.pspsdk.transaction.payment.PaymentAccountActivity
import kotlinx.android.synthetic.main.activity_payment_account.*
import kotlinx.android.synthetic.main.creditcard_payment_account_list_item.view.*
import kotlinx.android.synthetic.main.mapped_account_list_item.view.txt_card_no
//import kotlinx.android.synthetic.main.mapped_account_list_item.view.txt_expiry_no
import kotlinx.android.synthetic.main.payment_account_list_item.view.*
import kotlinx.android.synthetic.main.payment_account_list_item.view.img_bank_logo
//import kotlinx.android.synthetic.main.payment_account_list_item.view.txt_account_type
import kotlinx.android.synthetic.main.payment_account_list_item.view.txt_bank_name

data class CrediCardAccountListAdapter(
        private val creditcardaccountList: ArrayList<CreditCardMappedAccount>,
        private val accountList: ArrayList<MappedAccount>,
        private val listener: PaymentAccountActivity)
    : RecyclerView.Adapter<CrediCardAccountListAdapter.ViewHolder>() {

    protected var selectedCreditCardPosition:Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.creditcard_payment_account_list_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (position == 0) {
            holder.adnewcreditcardview.visibility = View.VISIBLE
            holder.mycreditdefault.visibility = View.GONE
            holder.mycreditdefault.useCompatPadding=true

            holder.itemView.setOnClickListener {
                holder.mycreditdefault.setForeground(null)
//                holder.adnewcreditcardview.setForeground(getDrawable(holder.adnewcreditcardview.context,R.drawable.selectorgrid))
                listener.onSetCreditCardAccount(position)

            }

        } else {
            holder.adnewcreditcardview.visibility = View.GONE
            holder.mycreditdefault.visibility = View.VISIBLE

            holder.accountItem = creditcardaccountList[position]
            holder.cardNo.text = holder.accountItem.maskedCardNumber


            //   val values = holder.accountItem.maskedAccountNumber
            //   val lstValues: List<String> = values!!.split("#").map { it -> it.trim() }
            //   lstValues.forEach { it ->
//            Log.d("Sudhir", "value=$it")
//        }
//        holder.accountNo.text="Account No: " +lstValues.get(0)
//        holder.cardNo.text = "Card No: "+lstValues.get(1)
//        holder.expiryNo.text = "Expiry No: "+lstValues.get(2)
//        Log.d("Sudhir", "value="+lstValues.get(0))
//        Log.d("Sudhir", "value="+lstValues.get(1))
//        Log.d("Sudhir", "value="+lstValues.get(2))

            holder.bankName.text = holder.accountItem.bankIssuer
//            holder.expiryNo.text = holder.accountItem.cardExpDate
//            holder.creditcardselected.isChecked = selectedCreditCardPosition == position

//            holder.creditcardselected.setOnClickListener {
//                holder.accountItem.isSelected = true
//                holder.creditcardselected.isChecked = true
//                itemCheckChanged(position)
//                listener.onSetCreditCardAccount(holder.accountItem)
//            }

            if (holder.accountItem.isSelected == true) {
                holder.checkboxcredit.visibility = View.VISIBLE
                holder.mycreditdefault.setForeground(getDrawable(holder.mycreditdefault.context,R.drawable.selectorgrid))
                holder.mycreditdefault.useCompatPadding = false
//                holder.adnewcreditcardview.setForeground(null)

            } else {
                holder.checkboxcredit.visibility = View.GONE
                holder.mycreditdefault.setForeground(null)
                holder.mycreditdefault.useCompatPadding = true
//                holder.adnewcreditcardview.setForeground(null)


            }

        holder.itemView.setOnClickListener {
            holder.accountItem.isSelected = true
            itemCheckChangedcredit(position)

            for (i in 0..creditcardaccountList.size - 1) {
                if (holder.accountItem.maskedCardNumber == creditcardaccountList.get(i).maskedCardNumber &&
                        holder.accountItem.token == creditcardaccountList.get(i).token) {

                    holder.accountItem.isSelected = true

                } else {
                    creditcardaccountList.get(i).isSelected = false

                }
            }
            for (i in 0..accountList.size - 1) {

                accountList.get(i).isSelected = false
            }
            listener.onSetCreditCardAccount(holder.accountItem)

        }
    }
    }

    override fun getItemCount(): Int {
        return creditcardaccountList.size
    }



    inner class ViewHolder(accountView: View) : RecyclerView.ViewHolder(accountView) {
//        val bankLogo = accountView.img_credicardbank_logo
        val cardNo = accountView.txt_creditcard_no
     //   val expiryNo = accountView.txt_expiry_no
        val bankName = accountView.txt_bank_name
        val checkboxcredit = accountView.checkbox_imge
      //  val accountType = accountView.txt_account_type
      //  val creditcardselected = accountView.rdo_credit_card
        lateinit var accountItem: CreditCardMappedAccount
        val adnewcreditcardview = accountView.txt_credit_card_add_New
        val mycreditdefault = accountView.my_default_list

    }

      internal fun itemCheckChangedcredit(position: Int) {
        selectedCreditCardPosition = position
        notifyDataSetChanged()
    }

}