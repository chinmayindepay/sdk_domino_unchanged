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

package com.indepay.umps.pspsdk.beneficiary.Contacts

import android.Manifest
import android.app.SearchManager
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.SearchView
import com.indepay.umps.pspsdk.R
import com.indepay.umps.pspsdk.baseActivity.SdkBaseActivity

import com.indepay.umps.pspsdk.models.AcquiringSource
import com.indepay.umps.pspsdk.models.Communication
import com.indepay.umps.pspsdk.models.Contact
import com.indepay.umps.pspsdk.models.TrackingRequest
import com.indepay.umps.pspsdk.transaction.payment.TxnPaymentActivity
import com.indepay.umps.pspsdk.utils.*
import kotlinx.android.synthetic.main.activity_bene_contacts.*
import org.jetbrains.anko.toast
import android.app.Activity
import android.content.Intent
import android.support.annotation.Keep
import com.indepay.umps.pspsdk.callbacks.OnContactListInteractionListener
import com.indepay.umps.pspsdk.setting.AddBeneficiaryActivity

import org.jetbrains.anko.*


@Keep
class BeneContactsActivity : SdkBaseActivity(), OnContactListInteractionListener, LoaderManager.LoaderCallbacks<Cursor>,
        ContactListAdapter.ExpandListener {


    private lateinit var txnType: String
    private val REQ_CODE_READ_CONTACTS = 100700
    private val CONTACT_LOADER_ID = 666
    private val PROJECTION = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Data.MIMETYPE)
    private val SELECTION = "(${ContactsContract.Data.MIMETYPE}=?)"

    private val SORT_BY = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"
    //  private val SELECTION_ARGS = arrayOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
    private val SELECTION_ARGS = arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
    private var comingFrom: Int = 0

    @Keep
    companion object : SdkCommonMembers() {
        @Keep
        const val TRANSACTION_TYPE = "txn_type"
        @Keep
        const val TXN_TYPE_COLLECT = "COLLECT"
        @Keep
        const val TXN_TYPE_PAY = "PAY"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bene_contacts)

        txnType = intent.getStringExtra(TRANSACTION_TYPE).toString()
        val layoutManager = WrapContentLinearLayoutManager(this@BeneContactsActivity)
        //val layoutManager = LinearLayoutManager(this@BeneContactsActivity)
        val itemDecoration = DividerItemDecoration(this@BeneContactsActivity, layoutManager.orientation)
        rv_contact_list.layoutManager = layoutManager
        rv_contact_list.addItemDecoration(itemDecoration)
        comingFrom = intent.getIntExtra(COMING_FROM, 0)
        loadContacts()
    }

    private fun loadContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                    REQ_CODE_READ_CONTACTS)
        } else {
            supportLoaderManager.initLoader(CONTACT_LOADER_ID, Bundle(), this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == REQ_CODE_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            } else {
                toast("Permission must be granted in order to display contacts information").show()

            }
        }
    }


    override fun onContactListInteraction(phoneNumber: String, contactName: String) {
        /*callApi(
            accessToken = retrieveAccessToken(this),
            custPSPId = getPspId(this),
            appName = getAppName(this),
            apiToCall = { sdkApiService ->
                sdkApiService.resolvePaByMobileNumberAsync(
                        mobile = phoneNo.replace("\\s+".toRegex(), ""),
                        accessToken = getAccessToken(this),
                        appName = getAppName(this),
                        custPSPId = getPspId(this)
                )
            },
            successCallback = { commonResponse -> onSuccessResolvePa(commonResponse) },
            errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
    )*/
        //Below is a temporary solution to resolve the token expiry issue
        var a:String
        loadAppTokenRequestDataSync()
        //saveStringData(this, "accessToken", a)
        //showError("server_error", getStringData(this, "accessToken"))

        if (comingFrom != 1) {
            callApi(
                    accessToken = getStringData(this, "accessToken"),//intent.getStringExtra(SdkBaseActivity.USER_TOKEN),//retrieveAccessToken(this),//
                    custPSPId = getPspId(this),
                    appName = getAppName(this),
                    apiToCall = { sdkApiService ->
                        sdkApiService.fetchAccessTokenAsync(
                                TrackingRequest(
                                        custPSPId = getPspId(this),
                                        accessToken = getStringData(this, "accessToken"),//intent.getStringExtra(SdkBaseActivity.USER_TOKEN),
                                        acquiringSource = AcquiringSource(
                                                appName = getAppName(this)
                                        )
                                )
                        )
                    },

                    successCallback = { commonResponse ->
                        /*startActivity(intentFor<AddBeneficiaryActivity>(
                                COMING_FROM to 1,
                                TxnPaymentActivity.MOBILE_NO to phoneNumber,
                                TxnPaymentActivity.TRANSACTION_TYPE to intent.getStringExtra(TRANSACTION_TYPE),
                                TxnPaymentActivity.PAYEE_NAME to getUserName(this),
                                TxnPaymentActivity.INITIATOR_ACCOUNT_ID to getAccountTokenId(this)).newTask().singleTop())
*/
                        startActivity<TxnPaymentActivity>(
                            //TxnPaymentActivity.PAYMENT_ADDRESS to response.pa,
                            TxnPaymentActivity.MOBILE_NO to phoneNumber,
                            TxnPaymentActivity.TRANSACTION_TYPE to intent.getStringExtra(TRANSACTION_TYPE),
                            TxnPaymentActivity.PAYEE_NAME to getUserName(this),
                            //TxnPaymentActivity.INITIATOR_PA_ACCOUNT_ID to getPaAccountId(this)
                            TxnPaymentActivity.INITIATOR_ACCOUNT_ID to getAccountTokenId(this)
                    )
                        //showError("","In successCallback")
                        finish()
                    },
                    errorCallback = { commonResponse -> commonErrorCallback(commonResponse) }
            )
        } else {
            val returnIntent = Intent()
            returnIntent.putExtra(RESULT, phoneNumber)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }


    }


    override fun onBackPressed() {
        if (comingFrom != 1) {
            super.onBackPressed()
        } else {
            val returnIntent = Intent()
            setResult(Activity.RESULT_CANCELED, returnIntent)
            finish()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                ContactsContract.Data.CONTENT_URI, // URI
                PROJECTION, // projection fields
                SELECTION, // the selection criteria
                SELECTION_ARGS, // the selection args
                SORT_BY // the sort order
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        val contactList = ArrayList<Contact>()

        var contactAdapter: ContactListAdapter? = null
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY))
                var phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val newContact = Contact(displayName)

                // As same contact appears with space between them
                // So trim them and adding only once
                phoneNumber = phoneNumber?.replace(" ", "")

                if (!contactList.contains(newContact)) {
                    newContact.communications.add(Communication(phoneNumber))
                    contactList.add(newContact)
                } else {
                    val existingContact = contactList[contactList.indexOf(newContact)]
                    var isContactAlreadyAdded = false

                    existingContact.communications.forEach {
                        if (it.phoneNumber.equals(phoneNumber)) {
                            isContactAlreadyAdded = true
                            return@forEach
                        }
                    }
                    if (!isContactAlreadyAdded) {
                        existingContact.communications.add(Communication(phoneNumber))
                    }
                }

            } while (cursor.moveToNext())

            contactAdapter = ContactListAdapter(context = this, contactList = contactList, listenerContact = this, expandCallback = this)
            rv_contact_list.adapter = contactAdapter
        }
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        suggestion_text.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        suggestion_text.setOnQueryTextListener(object : SearchView.OnQueryTextListener, android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                contactAdapter?.filter?.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                contactAdapter?.filter?.filter(newText)
                return false
            }

        })
        //suggestion_text.se
        // tAdapter(AutoCompleteAdapter(context = this, suggestionList = suggestionList, resourceId = R.layout.autocomplete_list_item))
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

    override fun onExpand(position: Int) {
        rv_contact_list.smoothScrollToPosition(position)
    }
}

