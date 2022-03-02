////initialize the buildconfig class
//
//package com.indepay.umps.pspsdk
//import android.support.v7.app.AppCompatActivity
//import android.os.Bundle
//import java.io.*
//import java.util.Properties;
//import com.indepay.umps.pspsdk.BuildConfig.*
//import android.R.attr.name
//
//
//
//
//
//
//
//class InitActivity : AppCompatActivity() {
//
//    val propertiesPath = "../../Mili.properties";
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_init)
//
//
//
//        val file = File(propertiesPath.toString())
//
//        val prop = Properties()
//
//        FileInputStream(file).use {
//            prop.load(it) }
//
//        prop.stringPropertyNames()
//            .associateWith {prop.getProperty(it)}
//
//        prop.getProperty("USE_PUBLIC_IP", "")
//        prop.getProperty("PUBLIC_IP_PSP", "")
//        prop.getProperty("PUBLIC_IP_CORE", "")
//        prop.getProperty("ENABLE_CRASH_REPORT","")
//        //BuildConfig.PSP_ORG_ID
//        prop.getProperty("PSP_ORG_ID"," ")
//
//        prop.getProperty("APP_NAME","")
//
//        prop.getProperty("MERCHANT_ID_WALLET","")
//
//        BuildConfig. =prop.getProperty("MERCHANT_ID","")
//
//        prop.getProperty("MERCHANT_KEY","")
//        prop.getProperty("MERCHANT_KI", "")
//
//        prop.getProperty("INITIATOR_PA","")
//        prop.getProperty("PA_HANDLE","")
//
//        prop.getProperty("PA_HANDLE","")
//
//        prop.getProperty("INDEPAY_MOBILE","")
//
//
//
//        prop.getProperty("","")
//
//        prop.getProperty("REFERENCE_URL","")
//
//        prop.getProperty("USE_SANDBOX","")
//
//        prop.getProperty("PSP_BASE_URL","")
//
//        prop.getProperty("CORE_BASE_URL","")
//
//        prop.getProperty("NOTI_BASE_URL_GET","")
//
//        prop.getProperty("SANDBOX_IP","")
//
//        prop.getProperty("SANDBOX_IP","")
//
//        prop.getProperty("SANDBOX_IP_NOTI","")
//
//
//
//
//    }}
//
//
//
