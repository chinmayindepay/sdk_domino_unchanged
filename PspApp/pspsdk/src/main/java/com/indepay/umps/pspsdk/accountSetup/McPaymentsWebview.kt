package com.indepay.umps.pspsdk.accountSetup

import android.app.Activity
import android.os.Bundle
import android.support.annotation.Keep
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.utils.SdkCommonMembers
import kotlinx.android.synthetic.main.activity_mcpayments_webview.*

class McPaymentsWebview : AppCompatActivity() {
    private val webView: WebView? = null
    private lateinit var mcPaymentsPaymenturl:String

    @Keep
    companion object : SdkCommonMembers() {
        @Keep const val SEAMLESSURL = "Seamless_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mcpayments_webview)
        llProgressBar.visibility = View.VISIBLE
        mcPaymentsPaymenturl = intent.getStringExtra(McPaymentsWebview.SEAMLESSURL).toString()

        val webView = findViewById<WebView>(R.id.webView)
//        webView.webViewClient =  WebViewClient()
//        webView.loadUrl(mcPaymentsPaymenturl)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

//        llProgressBar.visibility = View.GONE

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String) {
                super.onPageFinished(view, url)
                if (url.contains("success-url")) {
                    //call intent to navigate to activity
                    this@McPaymentsWebview.finish()
                }
            }
        }
        webView.loadUrl(mcPaymentsPaymenturl)
        llProgressBar.visibility = View.GONE

    }



    override fun onBackPressed() {
//        if (webView!!.canGoBack()) {
//            webView.goBack()
//        }
//        else {
            super.onBackPressed()
//        }
    }
}