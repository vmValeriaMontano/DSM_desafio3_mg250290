package com.example.aprendeapp.controller

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

class RecursoController(
    private val api: ApiService = RetrofitClient.api
) {
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

    /** Búsqueda por ID (exacto), título o tipo (parcial), filtro por tipo y orden. */
    fun filtrar(
        lista: List<Recurso>,
        consulta: String,
        tipo: String?,
        orden: Orden
    ): List<Recurso> {
        val q = consulta.trim().lowercase()
        val filtrada = lista.filter { r ->
            val coincideTipo = tipo == null || r.tipo.equals(tipo, ignoreCase = true)
            val coincideBusqueda = q.isEmpty() ||
                    r.id == q ||
                    r.titulo.lowercase().contains(q) ||
                    r.tipo.lowercase().contains(q)
            coincideTipo && coincideBusqueda
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
}