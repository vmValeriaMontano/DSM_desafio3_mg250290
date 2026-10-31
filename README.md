# AprendeApp

- **Autor:** Valeria del Rosario Montano Gonzalez -MG250290

## Funcionalidades

### Registro e inicio de sesión
- Registro con nombre, correo, contraseña, confirmación y selección de rol (Estudiante o Docente).
- Validación de contraseña: mínimo 12 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial (`! @ # $ % ^ & *`). La pantalla muestra todos los requisitos que faltan.
- La contraseña nunca se guarda en texto plano: se almacena como hash SHA-256 con sal aleatoria.
- Correo único: no permite registrar dos cuentas con el mismo correo.
- La sesión persiste al cerrar la app. Cierre de sesión con confirmación.

### Roles
| Rol | Permisos |
|---|---|
| Estudiante | Ver, buscar, filtrar y ordenar recursos; marcar favoritos; calificar con 1 a 5 estrellas |
| Docente | Pantalla especial para crear, editar y eliminar recursos "POST, PUT, DELETE" |

### Gestión de recursos
- Lista en tarjetas con imagen, título, descripción corta, tipo, ID, promedio de calificación y enlace.
- Búsqueda por ID (exacta), título o tipo (parcial).
- Filtro por tipo mediante chips y ordenamiento por título (A-Z, Z-A) o por mejor calificados.
- Formulario con validación de campos para agregar y modificar (prellenado al editar).
- Confirmación antes de eliminar.
- Actualización por deslizamiento (pull to refresh).

### Persistencia de usuario
- **Favoritos:** se guardan localmente (SharedPreferences) por usuario, y se pueden ver solos con el botón de estrella de la barra.
- **Calificación:** se actualiza en el backend (`sumaCalificaciones` y `totalCalificaciones`), por lo que el promedio es el mismo para todos los usuarios. Si un usuario vuelve a calificar, su voto anterior se reemplaza.

### UI / UX y feedback visual
- Material Components, Glide para imágenes y RecyclerView con DiffUtil.
- Loaders (indicador circular y swipe refresh), toasts, diálogos de confirmación, errores por campo y animación de entrada de la lista.
- Todos los textos están centralizados en `strings.xml`.

## Arquitectura (MVC)

```
com.tunombre.aprendeapp
├── model/        Recurso.kt, Usuario.kt, Roles            -> datos
├── network/      ApiService.kt, RetrofitClient.kt          -> acceso a la API (parte del modelo)
├── controller/   AuthController, RecursoController,
│                 FavoritosController                        -> lógica de negocio
├── view/         Activities, adapter y layouts XML          -> interfaz
└── util/         PasswordValidator, PasswordHasher,
                  SessionManager
```

- **Modelo:** clases de datos y servicio Retrofit.
- **Vista:** `LoginActivity`, `RegisterActivity`, `MainActivity`, `FormRecursoActivity`, `RecursoAdapter` y los XML. Solo muestran datos y capturan eventos.
- **Controlador:** valida, filtra, ordena, llama a la API y devuelve resultados a la vista.

## API (MockAPI.io)

Dos recursos: `recursos` y `usuarios`.

| Método | Endpoint | Uso |
|---|---|---|
| GET | `/recursos` | Listar recursos |
| GET | `/recursos/{id}` | Obtener un recurso (antes de calificar) |
| POST | `/recursos` | Crear recurso (docente) |
| PUT | `/recursos/{id}` | Editar recurso (docente) y actualizar calificación |
| DELETE | `/recursos/{id}` | Eliminar recurso (docente) |
| GET | `/usuarios?email=` | Buscar usuario (login y registro) |
| POST | `/usuarios` | Registrar usuario |

Ejemplo de recurso:

```json
{
  "id": "1",
  "titulo": "Clean Code",
  "descripcion": "Libro sobre buenas prácticas de programación.",
  "tipo": "libro",
  "enlace": "https://ejemplo.com/clean-code",
  "imagen": "https://picsum.photos/400/300",
  "sumaCalificaciones": 0,
  "totalCalificaciones": 0
}
```

## Tecnologías

Kotlin, Android Studio, Retrofit + Gson + OkHttp, Coroutines, Glide, Material Components, RecyclerView, SwipeRefreshLayout, ViewBinding.

## Cómo ejecutar

1. Clonar el repositorio y abrirlo en Android Studio.
2. Crear un proyecto en [mockapi.io](https://mockapi.io) con los recursos `recursos` y `usuarios` (campos indicados arriba; `usuarios` lleva `nombre`, `email`, `passwordHash`, `salt`, `rol`).
3. Poner la URL base propia en `network/RetrofitClient.kt` (`BASE_URL`, termina en `/`).
4. Ejecutar en un emulador o dispositivo con Android 7.0 (API 24) o superior y conexión a internet.

## Limitaciones conocidas

- MockAPI no ofrece autenticación ni transacciones reales: el hash con sal protege la contraseña, pero la validación de roles ocurre en la app. En producción, el rol lo asignaría un administrador y la autorización se haría en el servidor.
- Los favoritos son locales: no se sincronizan entre dispositivos.
