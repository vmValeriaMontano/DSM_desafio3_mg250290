package com.example.aprendeapp.controller

import android.content.Context

class FavoritosController(context: Context, private val userId: String) {
    private val prefs = context.getSharedPreferences("datos_usuario", Context.MODE_PRIVATE)
    private val claveFavoritos = "fav_$userId"

    fun ids(): Set<String> = prefs.getStringSet(claveFavoritos, emptySet()) ?: emptySet()

    fun esFavorito(id: String?): Boolean = id != null && id in ids()

    // Devuelve true si quedó agregado, false si se quitó
    fun alternar(id: String): Boolean {
        val set = ids().toMutableSet()
        val agregado = if (set.contains(id)) { set.remove(id); false } else { set.add(id); true }
        prefs.edit().putStringSet(claveFavoritos, set).apply()
        return agregado
    }

    fun calificacionPrevia(id: String): Int? {
        val v = prefs.getInt("rating_${userId}_$id", 0)
        return if (v == 0) null else v
    }

    fun guardarCalificacion(id: String, estrellas: Int) {
        prefs.edit().putInt("rating_${userId}_$id", estrellas).apply()
    }
}