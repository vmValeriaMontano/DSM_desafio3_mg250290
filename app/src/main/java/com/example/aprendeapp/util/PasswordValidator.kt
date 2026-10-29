package com.example.aprendeapp.util

import com.example.aprendeapp.R

object PasswordValidator {
    private const val ESPECIALES = "!@#$%^&*"

    //validaciones paraa que sea segura
    fun validar(password: String): List<Int> {
        val errores = mutableListOf<Int>()
        if (password.length < 12) errores.add(R.string.pass_error_longitud)
        if (password.none { it.isUpperCase() }) errores.add(R.string.pass_error_mayuscula)
        if (password.none { it.isLowerCase() }) errores.add(R.string.pass_error_minuscula)
        if (password.none { it.isDigit() }) errores.add(R.string.pass_error_numero)
        if (password.none { it in ESPECIALES }) errores.add(R.string.pass_error_especial)
        return errores
    }
}