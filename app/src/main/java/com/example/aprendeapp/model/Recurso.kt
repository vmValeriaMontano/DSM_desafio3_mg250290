package com.example.aprendeapp.model

import java.io.Serializable

data class Recurso(
    val id: String? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val enlace: String = "",
    val imagen: String = "",
    val sumaCalificaciones: Int = 0,
    val totalCalificaciones: Int = 0
) : Serializable {
    val promedio: Float
        get() = if (totalCalificaciones == 0) 0f
        else sumaCalificaciones.toFloat() / totalCalificaciones
}