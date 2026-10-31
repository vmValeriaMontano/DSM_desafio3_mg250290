package com.example.aprendeapp.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.Orden
import com.example.aprendeapp.controller.RecursoController
import com.example.aprendeapp.controller.ResultadoRecursos
import com.example.aprendeapp.databinding.ActivityMainBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.util.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private val controller = RecursoController()
    private lateinit var adapter: RecursoAdapter

    private var todos: List<Recurso> = emptyList()
    private var tipoSeleccionado: String? = null
    private var orden = Orden.TITULO_ASC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)

        configurarToolbar()
        configurarLista()
        configurarBusqueda()
        configurarOrden()
        cargarRecursos(primeraVez = true)
    }

    private fun configurarToolbar() {
        binding.toolbar.subtitle = getString(
            R.string.toolbar_subtitulo, session.nombre(), session.rol()
        )
        binding.toolbar.inflateMenu(R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_logout) { confirmarLogout(); true } else false
        }
    }

    private fun configurarLista() {
        adapter = RecursoAdapter { abrirEnlace(it) }
        binding.rvRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvRecursos.adapter = adapter
        binding.swipe.setOnRefreshListener { cargarRecursos(primeraVez = false) }
    }

    private fun configurarBusqueda() {
        binding.etBuscar.doAfterTextChanged { aplicarFiltros() }
    }

    private fun configurarOrden() {
        val opciones = resources.getStringArray(R.array.opciones_orden)
        binding.acOrden.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, opciones)
        )
        binding.acOrden.setText(opciones[0], false)
        binding.acOrden.setOnItemClickListener { _, _, pos, _ ->
            orden = Orden.values()[pos]
            aplicarFiltros()
        }
    }

    private fun cargarRecursos(primeraVez: Boolean) {
        if (primeraVez) binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val r = controller.obtenerTodos()) {
                is ResultadoRecursos.Exito -> {
                    todos = r.recursos
                    construirChips()
                    aplicarFiltros()
                }
                is ResultadoRecursos.Error ->
                    Toast.makeText(this@MainActivity, r.mensajeRes, Toast.LENGTH_LONG).show()
            }
            binding.progress.visibility = View.GONE
            binding.swipe.isRefreshing = false
        }
    }

    private fun construirChips() {
        val grupo = binding.chipGroupTipos
        grupo.setOnCheckedStateChangeListener(null)
        grupo.removeAllViews()

        fun crearChip(texto: String, tipo: String?): Chip =
            Chip(this).apply {
                id = View.generateViewId()
                text = texto
                tag = tipo
                isCheckable = true
                isChecked = (tipo == tipoSeleccionado)
            }

        grupo.addView(crearChip(getString(R.string.chip_todos), null))
        controller.tiposDisponibles(todos).forEach { grupo.addView(crearChip(it, it)) }

        // Si el tipo seleccionado ya no existe, volver a "Todos"
        if (grupo.checkedChipId == View.NO_ID) {
            tipoSeleccionado = null
            (grupo.getChildAt(0) as Chip).isChecked = true
        }

        grupo.setOnCheckedStateChangeListener { group, ids ->
            val chip = ids.firstOrNull()?.let { group.findViewById<Chip>(it) }
            tipoSeleccionado = chip?.tag as String?
            aplicarFiltros()
        }
    }

    private fun aplicarFiltros() {
        val resultado = controller.filtrar(
            todos, binding.etBuscar.text.toString(), tipoSeleccionado, orden
        )
        adapter.submitList(resultado)
        binding.tvVacio.visibility = if (resultado.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun abrirEnlace(r: Recurso) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(r.enlace)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_abrir_enlace, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.error_abrir_enlace, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_logout_titulo)
            .setMessage(R.string.dialog_logout_mensaje)
            .setPositiveButton(R.string.btn_si) { _, _ ->
                session.cerrarSesion()
                Toast.makeText(this, R.string.msg_sesion_cerrada, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }
}