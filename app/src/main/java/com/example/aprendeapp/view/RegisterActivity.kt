package com.example.aprendeapp.view

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.controller.Resultado
import com.example.aprendeapp.databinding.ActivityRegisterBinding
import com.example.aprendeapp.model.Roles
import com.example.aprendeapp.util.PasswordValidator
import com.example.aprendeapp.util.SessionManager
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: AuthController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = AuthController(SessionManager(this))

        binding.btnRegistro.setOnClickListener { intentarRegistro() }
        binding.tvIrLogin.setOnClickListener { finish() }
    }

    private fun intentarRegistro() {
        val nombre = binding.etNombre.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        val confirmar = binding.etConfirmar.text.toString()
        val rol = if (binding.rbDocente.isChecked) Roles.DOCENTE else Roles.ESTUDIANTE

        listOf(binding.tilNombre, binding.tilEmail, binding.tilPassword, binding.tilConfirmar)
            .forEach { it.error = null }

        var valido = true
        if (nombre.isEmpty()) { binding.tilNombre.error = getString(R.string.error_nombre_requerido); valido = false }
        if (email.isEmpty()) { binding.tilEmail.error = getString(R.string.error_email_requerido); valido = false }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalido); valido = false
        }
        val erroresPass = PasswordValidator.validar(pass)
        if (erroresPass.isNotEmpty()) {
            binding.tilPassword.error = erroresPass.joinToString("\n") { getString(it) }
            valido = false
        }
        if (pass != confirmar) { binding.tilConfirmar.error = getString(R.string.error_pass_no_coincide); valido = false }
        if (!valido) return

        mostrarCarga(true)
        lifecycleScope.launch {
            when (val r = auth.registrar(nombre, email, pass, rol)) {
                is Resultado.Exito -> {
                    Toast.makeText(this@RegisterActivity, R.string.msg_registro_exitoso, Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resultado.Error -> {
                    mostrarCarga(false)
                    Toast.makeText(this@RegisterActivity, r.mensajeRes, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarCarga(visible: Boolean) {
        binding.progress.visibility = if (visible) View.VISIBLE else View.GONE
        binding.btnRegistro.isEnabled = !visible
    }
}