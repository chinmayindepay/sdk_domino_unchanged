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

import android.app.ProgressDialog
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.indepay.umps.spl.R
import com.indepay.umps.spl.utils.getCurrentLocale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.longToast
import java.util.*

open class SplBaseActivity : AppCompatActivity() {

    lateinit var dialog: ProgressDialog
    protected val uiScope = CoroutineScope(Dispatchers.Main)
    protected val bgDispatcher: CoroutineDispatcher = Dispatchers.IO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLocale(this)
        dialog = indeterminateProgressDialog (message = getString(R.string.spl_request_loading_message), title = getString(R.string.spl_request_loading_title))
        {
            isIndeterminate = true
            setCancelable(false)
        }
        if(dialog.isShowing){
            dialog.cancel()
        }

    }

    internal fun showDialog() {
        Log.e("showDialog","entered in")
        if(null != dialog && !dialog.isShowing){
            dialog.show()


        }
        Log.e("showDialog","moved out")
    }

    fun hideDialog(){
        if(null != dialog && dialog.isShowing){
            dialog.cancel()
        }
    }

    internal fun setLocale (activity: AppCompatActivity) {
        val config = activity.resources.configuration;

        val lang = getCurrentLocale(this);

        val locale: Locale
        //If user has selected some other language update that
        if (lang != null && !lang.isEmpty()) {
            locale = Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            config.setLayoutDirection(locale);
            config.locale = locale;
            activity.resources.updateConfiguration(config, activity.resources.getDisplayMetrics());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                activity.createConfigurationContext(config);
            } else {
                activity.resources.updateConfiguration(config, activity.getResources().getDisplayMetrics());
            }
        }
        else locale =  Locale("en");//else keep the default language
        Locale.setDefault(locale);
        config.locale = locale;
        config.setLayoutDirection(locale);
        activity.resources.updateConfiguration(config, activity.resources.getDisplayMetrics());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.createConfigurationContext(config);
        } else {
            activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
        }
    }


    fun showError(errorCode: String? = null, errorReason: String? = null){

        val builder = StringBuilder(
                getString(R.string.spl_server_error)
        )
        if(null != errorCode){
            builder.append(" | ")
            builder.append(errorCode)
        }
        if(null != errorReason){
            builder.append(" | ")
            builder.append(errorReason)
        }
        longToast(builder.toString()).show()
    }

    fun getErrorMessage(errorCode: String): String {

        return try {
            getString(resources.getIdentifier(errorCode.trim(), "string", packageName))
        } catch (e: Resources.NotFoundException) {
            ""
        }

    }
}
