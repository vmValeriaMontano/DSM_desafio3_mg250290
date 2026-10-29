package com.example.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.controller.Resultado
import com.example.aprendeapp.databinding.ActivityLoginBinding
import com.example.aprendeapp.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: AuthController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = SessionManager(this)
        if (session.estaLogueado()) { irAMain(); return }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = AuthController(session)

        binding.btnLogin.setOnClickListener { intentarLogin() }
        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun intentarLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        if (email.isEmpty()) { binding.tilEmail.error = getString(R.string.error_email_requerido); return }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_email_invalido); return
        }
        if (pass.isEmpty()) { binding.tilPassword.error = getString(R.string.error_password_requerida); return }

        mostrarCarga(true)
        lifecycleScope.launch {
            when (val r = auth.login(email, pass)) {
                is Resultado.Exito -> irAMain()
                is Resultado.Error -> {
                    mostrarCarga(false)
                    Toast.makeText(this@LoginActivity, r.mensajeRes, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarCarga(visible: Boolean) {
        binding.progress.visibility = if (visible) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !visible
    }

    private fun irAMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}