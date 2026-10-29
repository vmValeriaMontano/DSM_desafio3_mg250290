package com.example.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aprendeapp.R
import com.example.aprendeapp.databinding.ActivityMainBinding
import com.example.aprendeapp.util.SessionManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val session = SessionManager(this)

        binding.tvBienvenida.text = getString(R.string.welcome_message)
        binding.tvRol.text = getString(R.string.main_rol, session.rol())
        binding.btnLogout.setOnClickListener {
            session.cerrarSesion()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}