package com.example.aprendeapp.view

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.RecursoController
import com.example.aprendeapp.controller.Resultado
import com.example.aprendeapp.databinding.ActivityFormRecursoBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.util.SessionManager
import kotlinx.coroutines.launch

class FormRecursoActivity : AppCompatActivity() {
    companion object { const val EXTRA_RECURSO = "recurso" }

    private lateinit var binding: ActivityFormRecursoBinding
    private val controller = RecursoController()
    private var editando: Recurso? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Protección de rol: solo el docente puede entrar a esta pantalla
        if (!SessionManager(this).esDocente()) { finish(); return }

        binding = ActivityFormRecursoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editando = intent.getSerializableExtra(EXTRA_RECURSO) as? Recurso

        binding.toolbar.setTitle(
            if (editando == null) R.string.titulo_nuevo_recurso else R.string.titulo_editar_recurso
        )
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.acTipo.setAdapter(
            ArrayAdapter(
                this, android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.tipos_sugeridos)
            )
        )

        // Formulario prellenado al editar
        editando?.let {
            binding.etTitulo.setText(it.titulo)
            binding.etDescripcion.setText(it.descripcion)
            binding.acTipo.setText(it.tipo, false)
            binding.etEnlace.setText(it.enlace)
            binding.etImagen.setText(it.imagen)
        }

        binding.btnGuardar.setOnClickListener { guardar() }
    }

    private fun guardar() {
        val titulo = binding.etTitulo.text.toString().trim()
        val descripcion = binding.etDescripcion.text.toString().trim()
        val tipo = binding.acTipo.text.toString().trim().lowercase()
        val enlace = binding.etEnlace.text.toString().trim()
        val imagen = binding.etImagen.text.toString().trim()

        val e = controller.validar(titulo, descripcion, tipo, enlace, imagen)
        binding.tilTitulo.error = e.titulo?.let { getString(it) }
        binding.tilDescripcion.error = e.descripcion?.let { getString(it) }
        binding.tilTipo.error = e.tipo?.let { getString(it) }
        binding.tilEnlace.error = e.enlace?.let { getString(it) }
        binding.tilImagen.error = e.imagen?.let { getString(it) }
        if (e.hayErrores) return

        val original = editando
        // copy() conserva id y calificaciones al editar
        val recurso = (original ?: Recurso()).copy(
            titulo = titulo, descripcion = descripcion,
            tipo = tipo, enlace = enlace, imagen = imagen
        )

        mostrarCarga(true)
        lifecycleScope.launch {
            val resultado = if (original?.id == null) controller.crear(recurso)
            else controller.actualizar(original.id, recurso)

            when (resultado) {
                is Resultado.Exito -> {
                    Toast.makeText(
                        this@FormRecursoActivity,
                        if (original == null) R.string.msg_recurso_creado
                        else R.string.msg_recurso_actualizado,
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is Resultado.Error -> {
                    mostrarCarga(false)
                    Toast.makeText(this@FormRecursoActivity, resultado.mensajeRes, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarCarga(visible: Boolean) {
        binding.progress.visibility = if (visible) View.VISIBLE else View.GONE
        binding.btnGuardar.isEnabled = !visible
    }
}