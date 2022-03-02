package com.indepay.umps.pspsdk.transaction.history

import android.os.Bundle
import android.support.annotation.Keep
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.adapter.TxnHistoryAdapter
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
import com.indepay.umps.pspsdk.callbacks.OnTxnHistoryListInteractionListener
import com.indepay.umps.pspsdk.models.CommonResponse
import com.indepay.umps.pspsdk.models.Transaction
import com.indepay.umps.pspsdk.models.TxnHistoryResponse
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_txn_history.*
import org.jetbrains.anko.clearTop
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.singleTop
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Keep
class TxnHistoryActivity : SdkBaseActivity(), OnTxnHistoryListInteractionListener {

    private var pendingTxnOnly: Boolean = false
    private val txnHistoryList = ArrayList<Transaction>()
    private var recentList = ArrayList<Transaction>()
    private var sortingrecentList = ArrayList<Transaction>()

    private var headerTransaction: Transaction? = null

    @Keep
    companion object : SdkCommonMembers() {
        @Keep
        const val PENDING_TXN_ONLY = "pending_txn_only"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_txn_history)
        //FirebaseApp.initializeApp(this)
        if (!getBooleanData(this, IS_REGISTERED)) {
            if(getUserToken(this).isNullOrBlank()) {
                fetchAppToken { token ->
                    showRegistrationDialog(token)
                }
            }else{

                showRegistrationDialog(getUserToken(this))
            }
        }else {
//        showDialog()
            llProgressBar.visibility = View.VISIBLE
            fetchAppToken { token ->
                pendingTxnOnly = if (intent.hasExtra(PENDING_TXN_ONLY)) {
                    intent.getBooleanExtra(PENDING_TXN_ONLY, false)
                } else {
                    false
                }
                val layoutManager = android.support.v7.widget.LinearLayoutManager(this)
                val itemDecoration = android.support.v7.widget.DividerItemDecoration(this, layoutManager.orientation)
                rv_txn_history.layoutManager = layoutManager
                rv_txn_history.addItemDecoration(itemDecoration)
                rv_txn_history.adapter = TxnHistoryAdapter(txnHistoryList, this)

                if (pendingTxnOnly) {
                    txtTitle2.text = resources.getString(R.string.pending_transction)
                }

                getHistoryApi()

                pullToRefresh.setOnRefreshListener {
                    getHistoryApi()
                }
                pullToRefresh.setColorSchemeResources(R.color.colorBlue)
            }
            //  }
            back_arrowimage.setOnClickListener {
                onBackPressed()
            }
        }
    }

    override fun onResume() {
        super.onResume()

            fetchAppToken {token->
                pendingTxnOnly = if (intent.hasExtra(PENDING_TXN_ONLY)) {
                    intent.getBooleanExtra(PENDING_TXN_ONLY, false)
                } else {
                    false
                }
                val layoutManager = android.support.v7.widget.LinearLayoutManager(this)
                val itemDecoration = android.support.v7.widget.DividerItemDecoration(this, layoutManager.orientation)
                rv_txn_history.layoutManager = layoutManager
//                rv_txn_history.addItemDecoration(itemDecoration)
                rv_txn_history.adapter = TxnHistoryAdapter(txnHistoryList, this)

                if (pendingTxnOnly) {
                    txtTitle2.text = resources.getString(R.string.pending_transction)
                }

                getHistoryApi()

                pullToRefresh.setOnRefreshListener {
                    getHistoryApi()
                }
                pullToRefresh.setColorSchemeResources(R.color.colorBlue)

               // getHistoryApi()

            }


    }

    private fun getHistoryApi() {
        callApi(
                accessToken = retrieveAccessToken(this),
                custPSPId = getPspId(this),//"b0329a37-3485-4e3f-96ae-a96b26bfc6cb",  //getPspId(this),
                appName = getAppName(this),
                apiToCall = { sdkApiService ->
                    sdkApiService.getTransactionHistoryDetailsAsync(
                            appName = getAppName(this),
                            accessToken = getAccessToken(this),
                            custPSPId = getPspId(this),//"b0329a37-3485-4e3f-96ae-a96b26bfc6cb",//getPspId(this),
                            requestedLocale = getCurrentLocale(this),
                            includeWaitingForApprovalOnly = pendingTxnOnly
                    )
                },
                successCallback = { commonResponse ->
                    pullToRefresh.isRefreshing = false
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        onSuccessTxnDetails(commonResponse)
//                    }

                },
                errorCallback = { commonResponse ->
                    pullToRefresh.isRefreshing = false
                    commonErrorCallback(commonResponse)
                }
        )
    }

    private fun onSuccessTxnDetails(response: CommonResponse) {

//        hideDialog()
        llProgressBar.visibility = View.GONE
        if (response is TxnHistoryResponse) {
            if (response.transactionList.isNotEmpty()) {
                recentList.clear()
                sortingrecentList.clear()
                txnHistoryList.clear()
                var sortedList = response.transactionList.sortedWith(compareByDescending({ it.timestamp }))

                sortingrecentList = ArrayList(sortedList)
//                Log.d("Sudhir", "Sorting::" + sortedList)

                val date1:String
                val date2:String
//                Log.d("Current Month", dateFormat.format(date))

//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//                    val sdfHeadertimestamp = SimpleDateFormat("dd/MM/YYYY")
//                    val date1timestamp = sortedList.get(0).timestamp?.let { Date(it) }
//                    val date2timestamp = sortedList.get(sortedList.size - 1).timestamp?.let { Date(it) }
//                    date1 = sdfHeadertimestamp.format(date1timestamp)
//                    date2 = sdfHeadertimestamp.format(date2timestamp)
//
//                }else{
                    val sdfHeadertimestamp = SimpleDateFormat("YYYY-MM-dd")
                    val date1timestamp = sortedList.get(0).timestamp?.let { Date(it) }
                    val date2timestamp = sortedList.get(sortedList.size - 1).timestamp?.let { Date(it) }

                    date1 = sdfHeadertimestamp.format(date1timestamp)
                    date2 = sdfHeadertimestamp.format(date2timestamp)

//                }


             //   Log.d("Sudhir","date1:"+date1)

             //   Log.d("Sudhir","date2:"+date2)

              var months: List<String>  = DateUtil().getMonthsBetWeenDates(date2,date1) as List<String>

                var   allTransactions:  ArrayList<Transaction>  = ArrayList<Transaction>()

                        months = months.reversed()

                for(i in 0..months.size-1){
                //    Log.d("Sudhir","months:"+months.get(i))

                    var monthName = months.get(i);

                  var   monthlyTransactions:  List<Transaction>

                    monthlyTransactions= getTransactions(monthName, sortedList);


                    if(monthlyTransactions.size!=0)
                    {

                       var headerTransaction = Transaction()
                        headerTransaction!!.header = true
                        headerTransaction!!.name = monthName

                        allTransactions.add(headerTransaction)

                        allTransactions.addAll(monthlyTransactions)


                    }





                }


//                    Log.d("Sudhir", "Recent List size::" + recentList.size)
                    txnHistoryList.addAll(allTransactions)//response.transactionList
                    rv_txn_history.adapter.notifyDataSetChanged()
                } else {
                    img_pending_requests.visibility = View.VISIBLE
                }
            }
    }


    fun getTransactions(monthName:String, listoftransactions: List<Transaction>): List<Transaction> {

        var monthTransactionList : ArrayList<Transaction> = ArrayList<Transaction>()



        for (i in 0..listoftransactions.size - 1) {

            val sdfHeader = SimpleDateFormat("MMMM")
            val netDateHeader = listoftransactions.get(i).timestamp?.let { Date(it) }
            val dateHeader = sdfHeader.format(netDateHeader)

            if (monthName == dateHeader) {
                monthTransactionList.add(listoftransactions.get(i))
//                        Log.d("Sudhir"," Date Header in if:::"+dateHeader)
//                        Log.d("Sudhir"," Date  in if:::"+date)

            }




        }

        return monthTransactionList;
    }


    override fun onTxnHistoryListInteraction(historyItem: Transaction) {
        val historyDetailsJson = Gson().toJson(historyItem)
//        startActivity(intentFor<TxnDetailsActivity>(TxnDetailsActivity.HISTORY_DETAILS to historyDetailsJson)
//                .singleTop().clearTop()
//        )


        startActivity(intentFor<TxnDetailsActivityUpdated>(TxnDetailsActivityUpdated.HISTORY_DETAILS to historyDetailsJson)
                .singleTop().clearTop()
        )
    }

}
