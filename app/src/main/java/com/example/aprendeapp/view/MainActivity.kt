package com.example.aprendeapp.view

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.FavoritosController
import com.example.aprendeapp.controller.Orden
import com.example.aprendeapp.controller.RecursoController
import com.example.aprendeapp.controller.Resultado
import com.example.aprendeapp.controller.ResultadoRecursos
import com.example.aprendeapp.databinding.ActivityMainBinding
import com.example.aprendeapp.databinding.DialogCalificarBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.util.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private lateinit var favoritos: FavoritosController
    private val controller = RecursoController()
    private lateinit var adapter: RecursoAdapter

    private var todos: List<Recurso> = emptyList()
    private var tipoSeleccionado: String? = null
    private var orden = Orden.TITULO_ASC
    private var soloFavoritos = false

    // Al volver del formulario (crear/editar) se recarga la lista
    private val formLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { if (it.resultCode == RESULT_OK) cargarRecursos(primeraVez = true) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        session = SessionManager(this)
        favoritos = FavoritosController(this, session.userId())

        configurarToolbar()
        configurarLista()
        configurarBusqueda()
        configurarOrden()
        cargarRecursos(primeraVez = true)
    }

    // ---------- Toolbar ----------
    private fun configurarToolbar() {
        val toolbar = binding.toolbar
        toolbar.subtitle = getString(R.string.toolbar_subtitulo, session.nombre(), session.rol())
        toolbar.inflateMenu(R.menu.menu_main)

        val menu = toolbar.menu
        menu.findItem(R.id.action_agregar).isVisible = session.esDocente()
        menu.findItem(R.id.action_favoritos).isVisible = !session.esDocente()

        val colorSobrePrimario = MaterialColors.getColor(
            toolbar, com.google.android.material.R.attr.colorOnPrimary
        )
        menu.findItem(R.id.action_agregar).icon?.mutate()?.setTint(colorSobrePrimario)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_agregar -> {
                    formLauncher.launch(Intent(this, FormRecursoActivity::class.java))
                    true
                }
                R.id.action_favoritos -> {
                    soloFavoritos = !soloFavoritos
                    item.isChecked = soloFavoritos
                    pintarIconoFavoritos(item)
                    aplicarFiltros()
                    true
                }
                R.id.action_logout -> { confirmarLogout(); true }
                else -> false
            }
        }
    }

    private fun pintarIconoFavoritos(item: MenuItem) {
        item.setIcon(
            if (item.isChecked) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    // ---------- Lista ----------
    private fun configurarLista() {
        adapter = RecursoAdapter(
            esDocente = session.esDocente(),
            esFavorito = { favoritos.esFavorito(it.id) },
            onAbrirEnlace = { abrirEnlace(it) },
            onFavorito = { alternarFavorito(it) },
            onCalificar = { mostrarDialogoCalificar(it) },
            onEditar = { editar(it) },
            onEliminar = { confirmarEliminar(it) }
        )
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
                    binding.rvRecursos.scheduleLayoutAnimation()
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
            todos,
            binding.etBuscar.text.toString(),
            tipoSeleccionado,
            orden,
            if (soloFavoritos) favoritos.ids() else null
        )
        adapter.submitList(resultado)
        binding.tvVacio.setText(
            if (soloFavoritos) R.string.lista_vacia_favoritos else R.string.lista_vacia
        )
        binding.tvVacio.visibility = if (resultado.isEmpty()) View.VISIBLE else View.GONE
    }

    // ---------- Acciones de estudiante ----------
    private fun alternarFavorito(r: Recurso) {
        val id = r.id ?: return
        val agregado = favoritos.alternar(id)
        Toast.makeText(
            this,
            if (agregado) R.string.msg_favorito_agregado else R.string.msg_favorito_quitado,
            Toast.LENGTH_SHORT
        ).show()
        if (soloFavoritos) aplicarFiltros()
        else adapter.notifyItemChanged(adapter.currentList.indexOf(r))
    }

    private fun mostrarDialogoCalificar(r: Recurso) {
        val id = r.id ?: return
        val vista = DialogCalificarBinding.inflate(layoutInflater)
        vista.ratingBar.rating = (favoritos.calificacionPrevia(id) ?: 0).toFloat()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_calificar_titulo)
            .setView(vista.root)
            .setPositiveButton(R.string.btn_enviar) { _, _ ->
                val estrellas = vista.ratingBar.rating.toInt()
                if (estrellas == 0) {
                    Toast.makeText(this, R.string.msg_elige_estrellas, Toast.LENGTH_SHORT).show()
                } else {
                    enviarCalificacion(id, estrellas)
                }
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun enviarCalificacion(id: String, estrellas: Int) {
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val res = controller.calificar(id, estrellas, favoritos.calificacionPrevia(id))) {
                is Resultado.Exito -> {
                    favoritos.guardarCalificacion(id, estrellas)
                    Toast.makeText(this@MainActivity, R.string.msg_gracias_calificar, Toast.LENGTH_SHORT).show()
                    cargarRecursos(primeraVez = true)
                }
                is Resultado.Error -> {
                    binding.progress.visibility = View.GONE
                    Toast.makeText(this@MainActivity, res.mensajeRes, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ---------- Acciones de docente ----------
    private fun editar(r: Recurso) {
        val intent = Intent(this, FormRecursoActivity::class.java)
            .putExtra(FormRecursoActivity.EXTRA_RECURSO, r)
        formLauncher.launch(intent)
    }

    private fun confirmarEliminar(r: Recurso) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_eliminar_titulo)
            .setMessage(getString(R.string.dialog_eliminar_mensaje, r.titulo))
            .setPositiveButton(R.string.btn_eliminar) { _, _ -> eliminar(r) }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun eliminar(r: Recurso) {
        val id = r.id ?: return
        binding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            when (val res = controller.eliminar(id)) {
                is Resultado.Exito -> {
                    Toast.makeText(this@MainActivity, R.string.msg_recurso_eliminado, Toast.LENGTH_SHORT).show()
                    cargarRecursos(primeraVez = true)
                }
                is Resultado.Error -> {
                    binding.progress.visibility = View.GONE
                    Toast.makeText(this@MainActivity, res.mensajeRes, Toast.LENGTH_LONG).show()
                }
            }
        }
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