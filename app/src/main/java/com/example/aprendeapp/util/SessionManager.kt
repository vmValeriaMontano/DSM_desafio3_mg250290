package com.example.aprendeapp.util

import android.content.Context
import com.example.aprendeapp.model.Roles
import com.example.aprendeapp.model.Usuario

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun guardar(usuario: Usuario) {
        prefs.edit()
            .putString("id", usuario.id)
            .putString("nombre", usuario.nombre)
            .putString("rol", usuario.rol)
            .apply()
    }

    fun estaLogueado() = prefs.getString("id", null) != null
    fun userId() = prefs.getString("id", "") ?: ""
    fun nombre() = prefs.getString("nombre", "") ?: ""
    fun rol() = prefs.getString("rol", Roles.ESTUDIANTE) ?: Roles.ESTUDIANTE
    fun esDocente() = rol() == Roles.DOCENTE
    fun cerrarSesion() = prefs.edit().clear().apply()
}