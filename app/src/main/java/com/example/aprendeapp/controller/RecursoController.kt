package com.example.aprendeapp.controller

import android.util.Patterns
import com.example.aprendeapp.R
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.network.ApiService
import com.example.aprendeapp.network.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

enum class Orden { TITULO_ASC, TITULO_DESC, MEJOR_CALIFICADOS }

sealed class ResultadoRecursos {
    data class Exito(val recursos: List<Recurso>) : ResultadoRecursos()
    data class Error(val mensajeRes: Int) : ResultadoRecursos()
}

data class ErroresForm(
    val titulo: Int? = null,
    val descripcion: Int? = null,
    val tipo: Int? = null,
    val enlace: Int? = null,
    val imagen: Int? = null
) {
    val hayErrores: Boolean
        get() = listOf(titulo, descripcion, tipo, enlace, imagen).any { it != null }
}

class RecursoController(
    private val api: ApiService = RetrofitClient.api
) {
    // ---------- Leer ----------
    suspend fun obtenerTodos(): ResultadoRecursos {
        return try {
            ResultadoRecursos.Exito(api.getRecursos())
        } catch (e: HttpException) {
            if (e.code() == 404) ResultadoRecursos.Exito(emptyList())
            else ResultadoRecursos.Error(R.string.error_generico)
        } catch (e: IOException) {
            ResultadoRecursos.Error(R.string.error_red)
        } catch (e: Exception) {
            ResultadoRecursos.Error(R.string.error_generico)
        }
    }

    /** Búsqueda por ID título o tipo | filtro por tipo, favoritos y orden. */
    fun filtrar(
        lista: List<Recurso>,
        consulta: String,
        tipo: String?,
        orden: Orden,
        soloIds: Set<String>? = null
    ): List<Recurso> {
        val q = consulta.trim().lowercase()
        val filtrada = lista.filter { r ->
            val coincideTipo = tipo == null || r.tipo.equals(tipo, ignoreCase = true)
            val coincideBusqueda = q.isEmpty() ||
                    r.id == q ||
                    r.titulo.lowercase().contains(q) ||
                    r.tipo.lowercase().contains(q)
            val coincideFavorito = soloIds == null || (r.id != null && r.id in soloIds)
            coincideTipo && coincideBusqueda && coincideFavorito
        }
        return when (orden) {
            Orden.TITULO_ASC -> filtrada.sortedBy { it.titulo.lowercase() }
            Orden.TITULO_DESC -> filtrada.sortedByDescending { it.titulo.lowercase() }
            Orden.MEJOR_CALIFICADOS -> filtrada.sortedByDescending { it.promedio }
        }
    }

    fun tiposDisponibles(lista: List<Recurso>): List<String> =
        lista.map { it.tipo.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .sorted()

    // ---------- Validación del formulario ----------
    fun validar(
        titulo: String, descripcion: String, tipo: String, enlace: String, imagen: String
    ): ErroresForm {
        fun urlValida(s: String) =
            (s.startsWith("http://") || s.startsWith("https://")) &&
                    Patterns.WEB_URL.matcher(s).matches()

        return ErroresForm(
            titulo = if (titulo.length < 3) R.string.error_titulo else null,
            descripcion = if (descripcion.length < 10) R.string.error_descripcion else null,
            tipo = if (tipo.isEmpty()) R.string.error_tipo else null,
            enlace = if (!urlValida(enlace)) R.string.error_enlace else null,
            imagen = if (!urlValida(imagen)) R.string.error_imagen else null
        )
    }

    // ---------- POST, PUT, DELETE ----------
    private suspend fun ejecutar(bloque: suspend () -> Unit): Resultado = try {
        bloque()
        Resultado.Exito
    } catch (e: IOException) {
        Resultado.Error(R.string.error_red)
    } catch (e: Exception) {
        Resultado.Error(R.string.error_generico)
    }

    suspend fun crear(recurso: Recurso): Resultado =
        ejecutar { api.crearRecurso(recurso) }

    suspend fun actualizar(id: String, recurso: Recurso): Resultado =
        ejecutar { api.actualizarRecurso(id, recurso) }

    suspend fun eliminar(id: String): Resultado = ejecutar {
        val resp = api.eliminarRecurso(id)
        if (!resp.isSuccessful) throw HttpException(resp)
    }

     //con la suma y el total actualizados

    suspend fun calificar(id: String, estrellas: Int, votoAnterior: Int?): Resultado = ejecutar {
        val actual = api.getRecurso(id)
        val nuevo = if (votoAnterior == null) {
            actual.copy(
                sumaCalificaciones = actual.sumaCalificaciones + estrellas,
                totalCalificaciones = actual.totalCalificaciones + 1
            )
        } else {
            actual.copy(sumaCalificaciones = actual.sumaCalificaciones - votoAnterior + estrellas)
        }
        api.actualizarRecurso(id, nuevo)
    }
}