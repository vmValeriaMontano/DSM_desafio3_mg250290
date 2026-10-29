package com.example.aprendeapp.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {
    //haseho de contraseñas y mas seguridad
    fun generarSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun hash(password: String, sal: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest((sal + password).toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}