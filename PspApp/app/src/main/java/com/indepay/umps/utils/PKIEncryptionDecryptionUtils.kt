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

package com.indepay.umps.utils

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.*
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object PKIEncryptionDecryptionUtils {


    private lateinit var kpg: KeyPairGenerator
    private lateinit var kf: KeyFactory

    init {
        try {
            kpg = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(2048)
            kf = KeyFactory.getInstance("RSA")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

    }

    fun encryptAndEncodeByAes(data: ByteArray, aesKey: ByteArray): ByteArray {
        try {
            val secretKey = SecretKeySpec(aesKey, "AES")
            val cipher = Cipher.getInstance("AES/CTR/PKCS5Padding", "BC")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
            return Base64.getUrlEncoder().encode(cipher.doFinal(data))
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

    }

    fun decodeAndDecryptByAes(data: ByteArray?, aesKey: ByteArray?): ByteArray {
        try {
            val secretKey = SecretKeySpec(aesKey, "AES")
            val cipher = Cipher.getInstance("AES/CTR/PKCS5Padding", "BC")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))


            return cipher.doFinal(Base64.getUrlDecoder().decode(data))
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

    }

    fun generateAes(): ByteArray {
        val aesKey: KeyGenerator
        try {
            aesKey = KeyGenerator.getInstance("AES")
            aesKey.init(256)
            val secretKey = aesKey.generateKey()
            return secretKey.encoded
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

    }

    fun encrypt(input: ByteArray?, key: Key?): ByteArray? {
        if (input != null && key != null) {
            try {
                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC")
                cipher.init(Cipher.ENCRYPT_MODE, key)
                return cipher.doFinal(input)
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException(e)
            }

        }
        return null
    }

    fun decrypt(input: ByteArray?, key: Key?): ByteArray? {
        if (input != null && key != null) {
            try {
                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC")
                cipher.init(Cipher.DECRYPT_MODE, key)
                return cipher.doFinal(input)
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException(e)
            }

        }
        return null
    }

    fun encryptAndEncode(input: ByteArray, key: Key?): ByteArray? {
        val encrypted = encrypt(input, key)

        return if (encrypted != null) {
            Base64.getUrlEncoder().encode(encrypted)
        } else null

    }

    fun decodeAndDecrypt(input: ByteArray?, key: Key?): ByteArray? {
        if (input != null && key != null) {
            val decoded = Base64.getUrlDecoder().decode(input)

            if (decoded != null) {
                return decrypt(decoded, key)
            }
        }
        return null
    }

    fun encryptAndEncode(input: String?, key: Key?): String? {
        if (input != null && key != null) {
            val temp = encryptAndEncode(input.toByteArray(StandardCharsets.UTF_8), key)

            if (temp != null) {
                return String(temp, StandardCharsets.UTF_8)
            }
        }
        return null
    }

    fun encryptAndEncode(input: String?, key: ByteArray?): String {
        if (input != null && key != null) {
            val temp = encryptAndEncode(input.toByteArray(StandardCharsets.UTF_8), convertPublicKey(key))

            if (temp != null) {
                return String(temp, StandardCharsets.UTF_8)
            }
        }
        return ""
    }

    fun encryptAndEncodeByPrivateKey(input: ByteArray?, key: ByteArray?): String {
        if (input != null && key != null) {
            val temp = encryptAndEncode(input, convertToPrivateKey(key))

            if (temp != null) {
                return String(temp, StandardCharsets.UTF_8)
            }
        }
        return ""
    }

    fun decodeAndDecrypt(input: String?, key: Key?): String? {
        if (input != null && key != null) {
            val temp = decodeAndDecrypt(input.toByteArray(StandardCharsets.UTF_8), key)

            if (temp != null) {
                return String(temp, StandardCharsets.UTF_8)
            }
        }
        return null
    }

    fun decodeAndDecrypt(input: String?, key: ByteArray?): ByteArray? {
        if (input != null && key != null) {
            val k = convertToPrivateKey(key)
            if (null != k) {
                return decodeAndDecrypt(input.toByteArray(StandardCharsets.UTF_8), k)
            }
        }
        return null
    }

    fun generateKeyPair(): KeyPair {
        return kpg.generateKeyPair()
    }

    fun convertPublicKey(key: ByteArray?): PublicKey? {
        if (null == key) {
            return null
        }
        return try {
            kf.generatePublic(X509EncodedKeySpec(key))
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
            null
        }

    }

    fun convertToPrivateKey(key: ByteArray?): PrivateKey? {
        if (null == key) {
            return null
        }
        return try {
            kf.generatePrivate(PKCS8EncodedKeySpec(key))
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
            null
        }

    }

    fun extractPublicKeyFromFile(crtCerFile: ByteArray?): ByteArray? {
        if (null == crtCerFile) {
            return null
        }
        val f: CertificateFactory
        try {
            val `is` = ByteArrayInputStream(crtCerFile)
            f = CertificateFactory.getInstance("X.509")
            val certificate = f.generateCertificate(`is`) as X509Certificate
            val pk = certificate.publicKey
            if (null != pk) {
                return pk.encoded
            }
        } catch (e: CertificateException) {
            e.printStackTrace()
        }

        return null
    }

    fun extractPrivateKeyFromFile(keyFile: ByteArray, alias: String, password: String): ByteArray {
        try {
            val keystore = KeyStore.getInstance("PKCS12")

            val `is` = ByteArrayInputStream(keyFile)
            val caInput = BufferedInputStream(`is`)

            keystore.load(caInput, password.toCharArray())
            val key = keystore.getKey(alias, password.toCharArray()) as PrivateKey
            return key.encoded
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e)
        }

    }


}