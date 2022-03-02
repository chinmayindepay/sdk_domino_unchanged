
package com.indepay.umps.pspsdk.adapter

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.accountSetup.ManageAccountActivity
import com.indepay.umps.pspsdk.models.CreditCardMappedAccount
import com.indepay.umps.pspsdk.models.MappedAccount
import com.indepay.umps.pspsdk.utils.getPspBaseUrlForBankLogo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.mapped_account_list_item.view.*
import kotlinx.android.synthetic.main.mapped_account_list_item.view.img_bank_logo
import kotlinx.android.synthetic.main.mapped_account_list_item.view.txt_bank_name
import kotlinx.android.synthetic.main.mapped_account_list_item.view.txt_card_no

class MappedAccountListAdapter(
        private val accountList: ArrayList<MappedAccount>,
        private val creditcardaccountList: ArrayList<CreditCardMappedAccount>,
        private val listener: ManageAccountActivity,
        private val picassoInstance: Picasso,
        private val accessToken: String,
        private val appName: String,
        private val custPspId: String?)
    : RecyclerView.Adapter<MappedAccountListAdapter.ViewHolder>() {

    protected var selectedPosition:Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mapped_account_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position ==0){
            holder.adnewcardview.visibility =View.VISIBLE
            holder.mydebitdefault.visibility = View.GONE
            holder.itemView.setOnClickListener {
                holder.mydebitdefault.setForeground(null)
                holder.mydebitdefault.useCompatPadding=true
//                    holder.adnewcardview.setForeground(ContextCompat.getDrawable(holder.adnewcardview.context, R.drawable.selectorgrid))

                for (i in 0..accountList.size - 1) {

                    accountList.get(i).isSelected = false

                }
                for (i in 0..creditcardaccountList.size - 1) {

                    creditcardaccountList.get(i).isSelected = false
                }
                listener.onSetDefaultAccount(position)
            }
        }else {
            holder.adnewcardview.visibility = View.GONE
            holder.mydebitdefault.visibility = View.VISIBLE

            holder.accountItem = accountList[position]
            picassoInstance.load(getPspBaseUrlForBankLogo(holder.accountItem.bic.orEmpty(), accessToken, appName, custPspId)).placeholder(R.drawable.ic_bank_place_holder).into(holder.bankLogo)
            //  holder.accountNo.text = holder.accountItem.maskedAccountNumber
            val values = holder.accountItem.maskedAccountNumber
            val lstValues: List<String> = values!!.split("#").map { it -> it.trim() }
            lstValues.forEach { it ->
//            Log.d("Sudhir", "value=$it")
            }
//        holder.accountNo.text="Account No: " +lstValues.get(0)
            holder.cardNo.text = lstValues.get(1)
//        holder.expiryNo.text = "Expiry No: "+lstValues.get(2)
            holder.bankName.text = holder.accountItem.bankName
//        holder.accountType.text = holder.accountItem.accountType

//        if ((selectedPosition == -1)) {
//            holder.setDefault.isChecked = holder.accountItem.isDefault
//        }else {
//            holder.setDefault.isChecked = selectedPosition == position
//        }

            if (holder.accountItem.isSelected == true) {
                holder.checkbox.visibility = View.VISIBLE
                holder.mydebitdefault.setForeground(ContextCompat.getDrawable(holder.mydebitdefault.context, R.drawable.selectorgrid))
//                    holder.adnewcardview.setForeground(null)
                holder.mydebitdefault.useCompatPadding = false
            } else {
                holder.checkbox.visibility = View.GONE
                holder.mydebitdefault.setForeground(null)
                holder.mydebitdefault.useCompatPadding = true
//                    holder.adnewcardview.setForeground(null)

            }


//        holder.setDefault.setOnClickListener {
//            holder.accountItem.isSelected =true
//            itemCheckChanged(position)
//            listener.onSetDefaultAccount(holder.accountItem)
//        }
            holder.itemView.setOnClickListener {
                holder.accountItem.isSelected = true

                itemCheckChanged(position)
           //     Log.d("Sudhir", "accountList size payment adapter::" + accountList.size)
           //     Log.d("Sudhir", "CreditaccountList size payment adapter::" + creditcardaccountList.size)
                for (i in 0..accountList.size - 1) {
                    if (holder.accountItem.maskedAccountNumber == accountList.get(i).maskedAccountNumber) {
                        holder.accountItem.isSelected = true
                    } else {
                        accountList.get(i).isSelected = false
                    }
                }
                for (i in 0..creditcardaccountList.size - 1) {

                    creditcardaccountList.get(i).isSelected = false
                }
                listener.onSetDefaultAccount(holder.accountItem)


            }
        }
    }



    override fun getItemCount(): Int {
        return accountList.size
    }

    inner class ViewHolder(val accountView: View) : RecyclerView.ViewHolder(accountView) {
        val bankLogo = accountView.img_bank_logo
        //        val accountNo = accountView.txt_account_no
        val cardNo = accountView.txt_card_no
        //        val expiryNo = accountView.txt_expiry_no
        val bankName = accountView.txt_bank_name
        val checkbox = accountView.checkbox__imge
        val adnewcardview = accountView.add_new_cardview
        val mydebitdefault = accountView.my_debit_default
        //        val accountType = accountView.txt_account_type
        // val setDefault = accountView.rdo_set_default       //Radio button
        lateinit var accountItem: MappedAccount


    }

    internal fun itemCheckChanged(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

}
