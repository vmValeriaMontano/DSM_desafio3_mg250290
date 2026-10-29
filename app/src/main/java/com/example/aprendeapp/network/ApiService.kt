package com.example.aprendeapp.network


import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.model.Usuario
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // CRUD
    @GET("recursos") suspend fun getRecursos(): List<Recurso>
    @GET("recursos/{id}") suspend fun getRecurso(@Path("id") id: String): Recurso
    @POST("recursos") suspend fun crearRecurso(@Body recurso: Recurso): Recurso
    @PUT("recursos/{id}") suspend fun actualizarRecurso(
        @Path("id") id: String, @Body recurso: Recurso
    ): Recurso
    @DELETE("recursos/{id}") suspend fun eliminarRecurso(@Path("id") id: String): Response<Unit>

    // Usuarios
    @GET("usuarios") suspend fun buscarUsuarios(@Query("email") email: String): List<Usuario>
    @POST("usuarios") suspend fun registrarUsuario(@Body usuario: Usuario): Usuario
}