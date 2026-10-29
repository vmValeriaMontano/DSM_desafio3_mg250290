package com.example.aprendeapp.model

data class Usuario(
    val id: String? = null,
    val nombre: String = "",
    val email: String = "",
    val passwordHash: String = "",
    val salt: String = "",
    val rol: String = Roles.ESTUDIANTE
)

object Roles {
    const val ESTUDIANTE = "estudiante"
    const val DOCENTE = "docente"
}