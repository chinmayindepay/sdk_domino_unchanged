//package com.indepay.umps.pspsdk.billPayment
//
//import android.os.Bundle
//import android.support.design.widget.TabLayout
//import android.support.v4.view.ViewPager
//import android.util.Log
//import com.indepay.umps.pspsdk.R
//import com.indepay.umps.pspsdk.adapter.MyAdapter
//import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity
//import kotlinx.android.synthetic.main.bill_payment_bank_selection.*
//import org.jetbrains.anko.intentFor
//import org.jetbrains.anko.newTask
//import org.jetbrains.anko.singleTop
//
//class BankBillPaymentSelection: SdkBaseActivity() {
//
//    var tabLayout: TabLayout? = null
//    var viewPager: ViewPager? = null
//
//    companion object {
//        const val BANK_BIC = "bank_bic"
//        const val BANK_NAME = "bank_name"
//        const val DEBIT_CARD_NO = "debit_card_no"
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.bill_payment_bank_selection)
//
//        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
//        viewPager = findViewById<ViewPager>(R.id.viewPager)
//
//        tabLayout!!.addTab(tabLayout!!.newTab().setText("Pulsa"))
//        tabLayout!!.addTab(tabLayout!!.newTab().setText("Packet Data"))
//        tabLayout!!.tabGravity = TabLayout.GRAVITY_FILL
//
//        val adapter = MyAdapter(this, supportFragmentManager, bundle,tabLayout!!.tabCount)
//        viewPager!!.adapter = adapter
//
//        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
//
//        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                viewPager!!.currentItem = tab.position
//            }
//            override fun onTabUnselected(tab: TabLayout.Tab) {
//
//            }
//            override fun onTabReselected(tab: TabLayout.Tab) {
//
//            }
//        })
//
//        btn_bank_next.setOnClickListener { v->
//
//            Log.d("Sudhir","Next Clicked")
//            startActivity(intentFor<BillNumberFilled>().newTask().singleTop())
//
//        }
//
//    }
//
//    }