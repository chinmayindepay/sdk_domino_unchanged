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

package com.indepay.umps.spl.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.indepay.umps.spl.models.*
import com.indepay.umps.spl.utils.PKIEncryptionDecryptionUtils.generateAes
import java.lang.System.currentTimeMillis
import java.nio.charset.StandardCharsets
import java.security.PublicKey
import java.util.*


object SplMessageUtils {

    private fun retrieveAesKey(): ByteArray {
        return PKIEncryptionDecryptionUtils.generateAes()
    }

    //TODO: Take transaction type && psp app id
    fun createEncryptionKeyRetrievalRequest(txnId: String, symmetricKey: ByteArray, bic: String, appId: String, txnType: TransactionType,
                                            context: Context, resetCredentialCall: Boolean,
                                            mobileNumber: String, activity: Activity): EncryptionKeyRetrievalRequest {
     //   Log.d("COnnect New Acount","Sudhir loadRetrieveKeysData::Start  ")

        val payload = CredentialKeysRetrievalPayload(
                paymentInstrument = PaymentInstrument(
                        paymentInstrumentType = PaymentInstrumentType.ACCOUNT,
                        bic = bic
                ),
                startDateTime = null,
                resetCredentialCall = resetCredentialCall,
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = getDeviceId(context),
                        imei1 = getImei1(context,activity),
                        imei2 = getImei2(context,activity),
                        mobileNo = mobileNumber,
                        pspIdentifier = if (getPspId(activity).isNotEmpty()) getPspId(activity) else null
                )
        )
   //     Log.d("COnnect New Acount","Sudhir loadRetrieveKeysData end ")

        return EncryptionKeyRetrievalRequest(
                commonRequest = createCommonRequest(txnId, symmetricKey, getSplKey(activity), txnType, getSplId(activity), getPspId(activity)),
                credentialKeysRetrievalPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8),
                credentialKeysRetrievalPayload = null
        )
    }

    //Retrieve key after initiate transaction
    fun createAFterInitiateAccountEncryptionKeyRetrievalRequest(txnId: String, symmetricKey: ByteArray, bic: String, appId: String, txnType: TransactionType,
                                            context: Context, resetCredentialCall: Boolean,
                                            mobileNumber: String, activity: Activity): EncryptionKeyRetrievalRequest {


        val payload = CredentialKeysRetrievalPayload(
                paymentInstrument = PaymentInstrument(
                        paymentInstrumentType = PaymentInstrumentType.ACCOUNT,
                        bic = bic
                ),
                startDateTime = currentTimeMillis(),
                resetCredentialCall = resetCredentialCall,
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = getDeviceId(context),
                        imei1 = getImei1(context,activity),
                        imei2 = getImei2(context,activity),
                        mobileNo = mobileNumber,
                        pspIdentifier = if (getPspId(activity).isNotEmpty()) getPspId(activity) else null
                )
        )
        return EncryptionKeyRetrievalRequest(
                commonRequest = createCommonRequest(txnId, symmetricKey, getSplKey(activity), txnType, getSplId(activity), getPspId(activity)),
                credentialKeysRetrievalPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8),
                credentialKeysRetrievalPayload = null
        )
    }


    //Retrieve key after authorize
    fun createAfterauthorizeAccountEncryptionKeyRetrievalRequest(txnId: String, symmetricKey: ByteArray, bic: String, appId: String, txnType: TransactionType,
                                                                context: Context, resetCredentialCall: Boolean,
                                                                mobileNumber: String, activity: Activity): EncryptionKeyRetrievalRequest {

        val payload = CredentialKeysRetrievalPayload(
                startDateTime = currentTimeMillis(),
                paymentInstrument = PaymentInstrument(
                        paymentInstrumentType = PaymentInstrumentType.ACCOUNT,
                        bic = bic
                ),
                resetCredentialCall = resetCredentialCall,
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = getDeviceId(context),
                        imei1 = getImei1(context,activity),
                        imei2 = getImei2(context,activity),
                        mobileNo = mobileNumber,
                        pspIdentifier = if (getPspId(activity).isNotEmpty()) getPspId(activity) else null
                )
        )
        return EncryptionKeyRetrievalRequest(
                commonRequest = createCommonRequest(txnId, symmetricKey, getSplKey(activity), txnType, getSplId(activity), getPspId(activity)),
                credentialKeysRetrievalPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8),
                credentialKeysRetrievalPayload = null
        )
    }



    //Fetch OTP
    fun createFetchOtpEncryptionKeyRetrievalRequest(txnId: String, symmetricKey: ByteArray,sessionKey: String, bic: String, appId: String, txnType: TransactionType,
                                                                 context: Context, action: String,
                                                                 mobileNumber: String, activity: Activity): EncryotionOtpFetchRetrievalRequest {

        val payload = OtpFetchRetrievalPayload(
                paymentInstrument = PaymentInstrument(
                        paymentInstrumentType = PaymentInstrumentType.ACCOUNT,
                        bic = bic
                ),
                action = action,
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = getDeviceId(context),
                        imei1 = getImei1(context,activity),
                        imei2 = getImei2(context,activity),
                        mobileNo = mobileNumber,
                        pspIdentifier = if (getPspId(activity).isNotEmpty()) getPspId(activity) else null
                )
        )
        Log.d("FetchOTP Payload","Fetch Payload::"+payload)
        return EncryotionOtpFetchRetrievalRequest(
                commonRequest = createCommonRequest(txnId, symmetricKey, getSplKey(activity), txnType, getSplId(activity), getPspId(activity)),
                fetchOtpCodePayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8),
                OtpFetchRetrievalPayload = null
        )
    }


    //Refresh OTP
    fun createRefreshOtpEncryptionRequest(txnId: String, symmetricKey: ByteArray, referenceId: String, bic: String, appId: String, txnType: TransactionType,
                                          context: Context, action: String,
                                          mobileNumber: String, activity: Activity): EncryptionRefreshOtpRequest {

        val payload = RefreshOtpPayload(
                bic = bic,
                action = action,
                referenceId = referenceId,
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = getDeviceId(context),
                        imei1 = getImei1(context,activity),
                        imei2 = getImei2(context,activity),
                        mobileNo = mobileNumber,
                        pspIdentifier = if (getPspId(activity).isNotEmpty()) getPspId(activity) else null
                )
        )
        Log.d("Refresh OTP Payload","Refresh Otp Payload::"+payload)
        Log.d("Refresh OTP Payload","Refresh Otp Payload::"+symmetricKey.toString())
        Log.d("Refresh OTP Payload","Refresh Otp Payload::"+ getSplKey(activity))
        return EncryptionRefreshOtpRequest(
                commonRequest = createCommonRequest(txnId, symmetricKey, getSplKey(activity), txnType, getSplId(activity), getPspId(activity)),
                refreshOtpApiPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8),
                refreshOtpPayload = null
        )
    }


    //Validate OTP
    fun createValidateOtpRetrievalRequest(txnId: String, bankKey: String, ki: String, sessionKey: String, referenceId: String,otp:String, symmetricKey: ByteArray, bic: String, appId: String, txnType: TransactionType,
                                                                 context: Context, action: String,
                                                                 mobileNumber: String, activity: Activity): EncryptionValidateOtpRetrievalRequest {

        val payload = ValidateOtpRetrievalPayload(

                action = action,
                bic = bic,
                referenceId = referenceId,
                otp = FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + otp + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = getDeviceId(context),
                        imei1 = getImei1(context,activity),
                        imei2 = getImei2(context,activity),
                        mobileNo = mobileNumber,
                        pspIdentifier = if (getPspId(activity).isNotEmpty()) getPspId(activity) else null
                )
        )
        return EncryptionValidateOtpRetrievalRequest(
                commonRequest = createCommonRequest(txnId, symmetricKey, PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(sessionKey)), txnType, getSplId(activity), getPspId(activity)),
                validateOtpApiPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8),
                validateOtpRetrievalPayload = null
        )
    }


    private fun createCommonRequest(txnId: String, symmetricKey: ByteArray, splKeyString: String?, transactionType: TransactionType?, splId: String, pspId: String): CommonRequest {
        return createCommonRequest(txnId, symmetricKey, PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(splKeyString)), transactionType, splId, pspId)
    }

    private fun createCommonRequest(txnId: String, symmetricKey: ByteArray, pubKey: PublicKey?, transactionType: TransactionType?, splId: String, pspId: String): CommonRequest {
        val encSymmetricKey = PKIEncryptionDecryptionUtils.encryptAndEncode(symmetricKey, pubKey)
        encSymmetricKey?.let {
            return CommonRequest(
                    msgId = UUID.randomUUID().toString(),
                    splIdentifier = splId,
                    pspIdentifier = pspId,
                    txnId = txnId,
                    transactionType = transactionType,
                    symmetricKey = String(encSymmetricKey, StandardCharsets.UTF_8)
            )
        }
        return CommonRequest()
    }

    internal fun getHttpHeader(): Map<String, String> {
        val headers = HashMap<String, String>(2)
        headers["Content-Type"] = "application/json"
        headers["Accept"] = "application/json"
        return headers
    }

    //input txn type
    internal fun createSetCredentialRequest(txnId: String, symmetricKey: ByteArray, ki: String, bankKey: String, sessionKey: String,
                                            appId: String, deviceId: String, imei1: String, imei2: String, mobileNumber: String, splId: String, pspId: String,
                                            cardDigits: String, cardExpiry: String, cardPin: String, mpin: String, bic: String,credType: CredType,cardOtp:String): ResetCredentialRequest {

        val payload = ResetCredentialPayload(

                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = deviceId,
                        imei1 = imei1,
                        imei2 = imei2,
                        mobileNo = mobileNumber,
                        pspIdentifier = pspId
                ),
                paymentInstrument = PaymentInstrument(
                        bic = bic
                ),
                resetPINCred = ResetPINCred(
                        cardPin = FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + cardPin + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        cardDigits = FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + cardDigits + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        newMpin = FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + mpin + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        cardExpiry = FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + cardExpiry + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        credType = credType,
                        cardOtp =FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + cardOtp + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey))
                )
        )
        return ResetCredentialRequest(
                commonRequest = createCommonRequest(txnId, symmetricKey, PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(sessionKey)), TransactionType.SET_PIN, splId, pspId),
                resetCredentialPayload = null,
                resetCredentialPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8)
        )

    }

    //Registering Account
     fun createRegisterAccountRequest(txnId: String, symmetricKey: ByteArray, ki: String, bankKey: String, sessionKey: String,
                                            appId: String, deviceId: String, imei1: String, imei2: String, mobileNumber: String, splId: String, pspId: String,
                                              accountNumber:String,cardDigits: String, cardExpiryMM: String,cardExpiryYY: String, cardPin: String, fullName: String, bic: String): RegisterCardDetailRequest {


        val payload = RegisterAccountDetailsPayload(
                startDateTime = currentTimeMillis(),
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = deviceId,
                        imei1 = imei1,
                        imei2 = imei2,
                        mobileNo = mobileNumber,
                        pspIdentifier = pspId
                ),
                accountInfo = AccountInfo(
                        bic = if(bic.isEmpty()) null else bic,
                        accountNo  = if(accountNumber.isEmpty()) null else FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + accountNumber + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey))
                        ),
                card = RegisterAccountDetails(

                        fullName = if(fullName.isEmpty()) null else FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + fullName + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        cvv = if(cardPin.isEmpty()) null else FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + cardPin + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        number = if(cardDigits.isEmpty()) null else FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + cardDigits + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        expiryMonth = if(cardExpiryMM.isEmpty()) null else FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + cardExpiryMM + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        expiryYear = if(cardExpiryYY.isEmpty()) null else FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + cardExpiryYY + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey))
                        ),
                bic = bic

        )
        Log.e("payload_chinmay",Gson().toJson(payload));
        return RegisterCardDetailRequest(

                commonRequest = createCommonRequest(txnId, symmetricKey, PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(sessionKey)),
                        TransactionType.REGISTER_CARD_ACC_DETAIL, splId, pspId),
                registerAccountDetailsPayload = null,

                registerCardDetailPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8)

        )

    }

    //COnfirm Registering Account
    fun ConfirmRegisterAccountRequest(txnId: String, symmetricKey: ByteArray, registeredName: String, sessionKey: String,
                                              appId: String, deviceId: String, imei1: String, imei2: String, mobileNumber: String, splId: String, pspId: String,
                                               bic: String, otp: String, ki: String, bankKey: String): ConfirmRegisterCardDetailRequest {

        val payload = ConfirmAccountDetailsPayload(
              //  startDateTime = currentTimeMillis(),
                bic = bic,
                accepted = true,
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = deviceId,
                        imei1 = imei1,
                        imei2 = imei2,
                        mobileNo = mobileNumber,
                        pspIdentifier = pspId
                ),
                authorizePINCred = AuthorizePINCred(
                    CredType.MPIN,
                    FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + otp + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey))
                )
        )

        return ConfirmRegisterCardDetailRequest(
                commonRequest = createCommonRequest(txnId, symmetricKey, PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(sessionKey)), TransactionType.REGISTER_CARD_ACC_DETAIL, splId, pspId),
                confirmAccountDetailsPayload = null,
                confirmAccountRegPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8)
        )

    }



    internal fun createChangeCredentialRequest(txnId: String, symmetricKey: ByteArray, ki: String, bankKey: String, sessionKey: String,
                                               appId: String, deviceId: String, imei1: String, imei2: String, mobileNumber: String, splId: String, pspId: String,
                                               oldMpin: String, newMpin: String): ResetCredentialRequest {

        val payload = ResetCredentialPayload(
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = deviceId,
                        imei1 = imei1,
                        imei2 = imei2,
                        mobileNo = mobileNumber,
                        pspIdentifier = pspId
                ),
                resetPINCred = ResetPINCred(
                        newMpin = FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + newMpin + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        oldMpin = FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + oldMpin + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey)),
                        credType = CredType.MPIN
                )
        )

        return ResetCredentialRequest(
                commonRequest = createCommonRequest(txnId, symmetricKey, PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(sessionKey)), TransactionType.CHANGE_PIN, splId, pspId),
                resetCredentialPayload = payload,
                resetCredentialPayloadEnc = PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey).toString()
        )

    }

    private fun createUserRegistrationRequestPayload(splIdentifier: String,
                                                     appId: String, deviceId: String, imei1: String, imei2: String, activity: Activity): SplRegistrationRequestPayload {
        return SplRegistrationRequestPayload(
                splIdentifier = splIdentifier,
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = deviceId,
                        imei1 = imei1,
                        imei2 = imei2,
                        pspIdentifier = getPspId(activity)
                )
        )

    }

    internal fun createSplRegistrationRequest(sessionKey: String, txnId: String, splIdentifier: String,
                                              appId: String, deviceId: String, imei1: String, imei2:
                                              String, pspOrgId: String, activity: Activity): SplRegistrationRequest {

        println("createSplRegReq1")
        println("createSplRegReq2")
        println("createSplRegReq3")
        //Log.e("createSplRegReq1", "txnId: " + txnId + ", splId: " + splIdentifier)
        //Log.e("createSplRegReq2", "appId: " + appId + ", deviceId: " + deviceId)
        //Log.e("createSplRegReq3", "orgId: " + pspOrgId + ", imei1: " + imei1 + ", imei2: " + imei2)

        val payload = createUserRegistrationRequestPayload(splIdentifier, appId, deviceId, imei1, imei2, activity)
        println("createSplRegReqPL")
        //Log.e("createSplRegReqPL", Gson().toJson(payload))
        val randomAesKey = generateAes()
     //   Log.d("Sudhir","randomAesKey::"+randomAesKey)

        return SplRegistrationRequest(
                pspOrgId = pspOrgId,
                txnId = txnId,
                msgId = UUID.randomUUID().toString(),
                splRegistrationRequestEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8), randomAesKey), StandardCharsets.UTF_8),
                symmetricKey = String(PKIEncryptionDecryptionUtils.encryptAndEncode(randomAesKey, PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getDecoder().decode(sessionKey)))!!),
                splRegistrationRequestPayload = null
        )
    }

    internal fun createCredentialSubmissionRequest(txnId: String, transactionType: TransactionType, ki: String, bankKey: String,
                                                   sessionKey: String, pspId: String, mobileNumber: String,
                                                   mpin: String, activity: Activity, appId: String): CredentialSubmissionRequest {

        val symmetricKey = retrieveAesKey()

        val payload = CredentialSubmissionPayload(
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = getDeviceId(activity),
                        imei1 = getImei1(activity,activity),
                        imei2 = getImei2(activity,activity),
                        mobileNo = mobileNumber,
                        pspIdentifier = pspId),
                authorizePINCred = AuthorizePINCred(
                        credType = CredType.MPIN,
                        credValue = FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + mpin + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey))
                )
        )

        return CredentialSubmissionRequest(
                commonRequest = createCommonRequest(
                        txnId = txnId,
                        symmetricKey = symmetricKey,
                        pubKey = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(sessionKey)),
                        transactionType = transactionType,
                        splId = getSplId(activity),
                        pspId = getPspId(activity)
                ),
                credentialSubmissionPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8),
                credentialSubmissionPayload = null

        )

    }

    //After Initiate Transaction Adding/Registering Account
    fun createCredentialSubmissionAfterInititingAccountRequest(txnId: String, transactionType: TransactionType, ki: String, bankKey: String,
                                          sessionKey: String, pspId: String, mobileNumber: String,
                                          mpin: String, activity: Activity, appId: String): CredentialSubmissionRequest {

        val symmetricKey = retrieveAesKey()

        val payload = CredentialSubmissionPayload(
                deviceInfo = DeviceInfo(
                        appId = appId,
                        deviceId = getDeviceId(activity),
                        imei1 = getImei1(activity,activity),
                        imei2 = getImei2(activity,activity),
                        mobileNo = mobileNumber,
                        pspIdentifier = pspId),
                authorizePINCred = AuthorizePINCred(
                        credType = CredType.MPIN,
                        credValue = FinancialMsgTranslatorUtils.retrieveMaskedAccountNo(txnId + "|" + mpin + "|" + Math.random(), ki, Base64.getUrlDecoder().decode(bankKey))
                )
        )

        return CredentialSubmissionRequest(
                commonRequest = createCommonRequest(
                        txnId = txnId,
                        symmetricKey = symmetricKey,
                        pubKey = PKIEncryptionDecryptionUtils.convertPublicKey(Base64.getUrlDecoder().decode(sessionKey)),
                        transactionType = transactionType,
                        splId = getSplId(activity),
                        pspId = getPspId(activity)
                ),
                credentialSubmissionPayloadEnc = String(PKIEncryptionDecryptionUtils.encryptAndEncodeByAes(data = (Gson().toJson(payload).toByteArray(StandardCharsets.UTF_8)), aesKey = symmetricKey), StandardCharsets.UTF_8),
                credentialSubmissionPayload = null

        )

    }


   /* internal fun createHashSha256(base: String?): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(base?.toByteArray())
            val hexString = StringBuffer()

            for (i in hash.indices) {
                val hex = Integer.toHexString(0xff and hash[i].toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }

            return hexString.toString()
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }

    }*/

}