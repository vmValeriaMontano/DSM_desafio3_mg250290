package com.example.aprendeapp.controller

import com.example.aprendeapp.R
import com.example.aprendeapp.model.Usuario
import com.example.aprendeapp.network.ApiService
import com.example.aprendeapp.network.RetrofitClient
import com.example.aprendeapp.util.PasswordHasher
import com.example.aprendeapp.util.SessionManager
import retrofit2.HttpException
import java.io.IOException

sealed class Resultado {
    object Exito : Resultado()
    data class Error(val mensajeRes: Int) : Resultado()
}

//clase con enfoque de poder manejar o identificar quien esta logeado
class AuthController(
    private val session: SessionManager,
    private val api: ApiService = RetrofitClient.api
) {
    private suspend fun buscarPorEmail(email: String): Usuario? {
        val lista = try {
            api.buscarUsuarios(email)
        } catch (e: HttpException) {
            if (e.code() == 404) emptyList() else throw e
        }
        return lista.firstOrNull { it.email.equals(email, ignoreCase = true) }
    }

    suspend fun registrar(nombre: String, email: String, password: String, rol: String): Resultado {
        return try {
            if (buscarPorEmail(email) != null) return Resultado.Error(R.string.error_email_existe)
            val sal = PasswordHasher.generarSal()
            api.registrarUsuario(
                Usuario(
                    nombre = nombre, email = email.lowercase(),
                    passwordHash = PasswordHasher.hash(password, sal),
                    salt = sal, rol = rol
                )
            )
            Resultado.Exito
        } catch (e: IOException) {
            Resultado.Error(R.string.error_red)
        } catch (e: Exception) {
            Resultado.Error(R.string.error_generico)
        }
    }

    suspend fun login(email: String, password: String): Resultado {
        return try {
            val usuario = buscarPorEmail(email)
                ?: return Resultado.Error(R.string.error_credenciales)
            if (PasswordHasher.hash(password, usuario.salt) != usuario.passwordHash)
                return Resultado.Error(R.string.error_credenciales)
            session.guardar(usuario)
            Resultado.Exito
        } catch (e: IOException) {
            Resultado.Error(R.string.error_red)
        } catch (e: Exception) {
            Resultado.Error(R.string.error_generico)
        }
    }
}