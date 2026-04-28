# 🗂️ Foro Hub API

API REST para gestión de foros de discusión, desarrollada con **Spring Boot 3** y **Java 17**. Permite a los usuarios registrarse, autenticarse y gestionar tópicos con un sistema de seguridad basado en JWT con refresh token rotation.

---

## 📋 Tabla de contenidos

- [Demo en producción](#-demo-en-producción)
- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Arquitectura del proyecto](#-arquitectura-del-proyecto)
- [Requisitos previos](#-requisitos-previos)
- [Configuración](#-configuración)
- [Ejecutar el proyecto](#-ejecutar-el-proyecto)
- [Endpoints](#-endpoints)
- [Autenticación](#-autenticación)
- [Documentación con Swagger](#-documentación-con-swagger)
- [Testing con clientes REST](#-testing-con-clientes-rest)

---

## 🌐 Demo en producción

La API está desplegada y disponible en:

🔗 **https://forohubgateway.vercel.app**

Puedes explorar y probar todos los endpoints directamente desde el Swagger UI sin necesidad de configurar nada localmente.

> ⚠️ El servidor está alojado en Koyeb con plan gratuito — si no ha recibido requests recientemente, puede tardar unos segundos en despertar, por favor espere.
 
---

## ✨ Características

- Registro e inicio de sesión de usuarios con contraseñas encriptadas con **BCrypt**
- Autenticación stateless mediante **JWT**
- **Refresh token rotation** — cada renovación invalida el token anterior
- Refresh token almacenado en **cookie HttpOnly** (protección XSS)
- CRUD completo de tópicos con paginación
- Validación para evitar tópicos duplicados (mismo título y mensaje)
- Solo el autor puede editar su tópico
- Migraciones de base de datos con **Flyway**
- Documentación interactiva con **Swagger UI / OpenAPI 3**
- Manejo centralizado de errores con respuestas estandarizadas

---

## 🛠️ Tecnologías

| Tecnología | Versión | Uso |
|---|---------|---|
| Java | 17      | Lenguaje principal |
| Spring Boot | 3.5.12  | Framework base |
| Spring Security | 6.x     | Autenticación y autorización |
| Spring Data JPA | 3.x     | Persistencia de datos |
| MySQL | 8.x     | Base de datos |
| Flyway | —       | Migraciones de BD |
| Auth0 Java JWT | 4.5.1   | Generación y validación de tokens |
| Lombok | —       | Reducción de boilerplate |
| SpringDoc OpenAPI | 2.8.16  | Documentación Swagger |
 
---

## 📁 Arquitectura del proyecto

```
src/main/java/api/rest/forohub/
│
├── ForohubApplication
│
├── domain/
│   ├── auth/
│   │   ├── AuthController.java            # Endpoints de autenticación
│   │   ├── AuthService.java               # Lógica de login, register, refresh, logout
│   │   ├── CookieService.java             # Creación y eliminación de cookies
│   │   ├── RefreshToken.java              # Entidad refresh token
│   │   ├── RefreshTokenRepository.java
│   │   └── dto/                           
│   │       ├── DatosAccessToken
│   │       ├── DatosLogin
│   │       ├── DatosRegister
│   │       └── DatosTokenJWT
│   │
│   ├── topico/
│   │   ├── TopicoController.java          # CRUD de topicos
│   │   ├── Topico.java                    # Entidad tópico
│   │   ├── TopicoRepository.java
│   │   ├── Status.java                    # Enum: ABIERTO, CERRADO
│   │   ├── dto/
│   │   │   ├── DatosActualizacionTopico
│   │   │   ├── DatosDetalleTopico
│   │   │   ├── DatosListaTopico
│   │   │   └── DatoaRegistroTopico                        
│   │   └── validacion/
│   │       └── ValidadorTopicoDuplicado.java
│   │
│   └── usuario/
│       ├── Usuario.java                   # Entidad usuario (implementa UserDetails)
│       └── UsuarioRepository.java
│
└── infra/
    ├── exceptions/
    │   ├── GestorDeErrores.java           # @RestControllerAdvice global
    │   ├── TokenInvalidoException.java
    │   ├── RecursoDuplicadoException.java
    │   └── AccesoDenegadoException.java
    │
    ├── security/
    │   ├── SecurityConfig.java            # Configuración Spring Security
    │   ├── SecurityFilter.java            # Filtro JWT por request
    │   ├── TokenService.java              # Generación y validación de JWT
    │   └── UserDetailsServiceImpl.java
    │
    └── springdoc/
        └── SpringDocConfig.java           # Configuración Swagger/OpenAPI
```
 
---

## ⚙️ Requisitos previos

- Java 17+
- Maven 3.8+
- MySQL 8.x corriendo localmente

---

## 🔧 Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/JuanLloclla/forohub.git
cd forohud
```

### 2. Crear la base de datos en MySQL

```sql
CREATE DATABASE foro_hub;
```

### 3. Configurar las variables de entorno

El proyecto usa perfiles de Spring (`dev` y `prod`). Para desarrollo, configura las siguientes variables de entorno o edita `application-dev.properties`:

```properties
# Conexión a base de datos
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/foro_hub
SPRING_DATASOURCE_USERNAME=tu_usuario
SPRING_DATASOURCE_PASSWORD=tu_contraseña
 
# Clave secreta para JWT (mínimo 256 bits recomendado)
JWT_SECRET=tu_clave_secreta_muy_larga_y_aleatoria
```

> 💡 **Generar una clave segura:**
> ```bash
> openssl rand -base64 32
> ```

### 4. Seleccionar perfil activo

Crear una variable de entorno SPRING_PROFILES_ACTIVE para usar el perfil de dev o prod:
```properties
SPRING_PROFILES_ACTIVE=dev
```

Las migraciones de Flyway se ejecutan automáticamente al iniciar la aplicación y crearán las tablas necesarias.
 
---

## 🚀 Ejecutar el proyecto

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`
 
---

## 📡 Endpoints

### 🔐 Autenticación — `/auth`

| Método | Endpoint | Descripción | Auth requerida |
|---|---|---|---|
| `POST` | `/auth/register` | Registrar nuevo usuario | ❌ |
| `POST` | `/auth/login` | Iniciar sesión | ❌ |
| `POST` | `/auth/refresh` | Renovar access token | ❌ (usa cookie) |
| `POST` | `/auth/logout` | Cerrar sesión | ❌ (usa cookie) |

**Body de `/auth/register` y `/auth/login`:**
```json
{
  "login": "usuario",
  "contrasena": "mipassword123"
}
```

**Respuesta exitosa:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```
> El `refreshToken` se establece automáticamente como cookie HttpOnly — no aparece en el body.
 
---

### 📝 Tópicos — `/topicos`

> Todos los endpoints requieren el header `Authorization: Bearer <accessToken>`

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/topicos` | Crear nuevo tópico |
| `GET` | `/topicos` | Listar tópicos (paginado) |
| `GET` | `/topicos/{id}` | Obtener tópico por ID |
| `PUT` | `/topicos/{id}` | Actualizar tópico (solo el autor) |
| `DELETE` | `/topicos/{id}` | Eliminar tópico |

**Body de `POST /topicos`:**
```json
{
  "titulo": "¿Cómo funciona Spring Security?",
  "mensaje": "Tengo dudas sobre la configuración del filtro JWT",
  "curso": "Spring Boot"
}
```

**Parámetros de paginación en `GET /topicos`:**
```
GET /topicos?page=0&size=10&sort=fecha,desc
```
 
---

## 🔒 Autenticación

La API implementa un sistema de doble token:

```
┌─────────────────────────────────────────────────┐
│              Flujo de autenticación              │
│                                                 │
│  1. Login/Register                              │
│     → accessToken  (body, 2 horas)              │
│     → refreshToken (cookie HttpOnly, 30 días)   │
│                                                 │
│  2. Requests autenticados                       │
│     → Header: Authorization: Bearer <token>     │
│                                                 │
│  3. Access token expirado → POST /auth/refresh  │
│     → Cookie se envía automáticamente           │
│     → Nuevo accessToken en body                 │
│     → Refresh token rotation (el viejo          │
│       se invalida, se genera uno nuevo)         │
│                                                 │
│  4. Logout → POST /auth/logout                  │
│     → Refresh token eliminado de la BD          │
│     → Cookie eliminada del navegador            │
└─────────────────────────────────────────────────┘
```

### ¿Por qué cookie HttpOnly para el refresh token?

- **Protección XSS:** JavaScript no puede leer la cookie, por lo que scripts maliciosos no pueden robar el token
- **Automático:** el navegador la envía solo en cada request a `/auth/*`
- **SameSite=None:** configurado para soportar frontend y backend en dominios distintos

---

## 📖 Documentación con Swagger

Una vez iniciada la aplicación, la documentación interactiva está disponible en:

```
http://localhost:8080/swagger-ui.html
```

### Cómo autenticarse en Swagger

1. Ejecuta `POST /auth/login` con tus credenciales
2. Copia el valor de `accessToken` de la respuesta
3. Haz clic en el botón **Authorize 🔒** en la parte superior derecha
4. Ingresa el token y confirma
5. Ahora puedes usar todos los endpoints protegidos directamente desde Swagger

> ⚠️ Para que `/auth/refresh` y `/auth/logout` funcionen desde Swagger, asegúrate de que `springdoc.swagger-ui.with-credentials=true` esté en tu `application.properties` y que CORS esté configurado correctamente.

---

## 🧪 Testing con clientes REST

Si prefieres probar la API con Postman o Insomnia, encontrarás las colecciones listas para importar en la carpeta `/docs`:

```
docs/
├── ForoHub-API-REST.postman_collection.json
└── ForoHud-API-REST-Insomnia_2026-03-21.yaml
```

### Importar en Postman

1. Abre Postman → **Import**
2. Selecciona `docs/ForoHub-API-REST.postman_collection.json`
3. Ve a **Environments** → crea un entorno nuevo con la variable `accessToken` (valor vacío por ahora)

### Importar en Insomnia

1. Abre Insomnia → **Import** → From File
2. Selecciona `docs/ForoHud-API-REST-Insomnia_2026-03-21.yaml`
3. Ve a **Environments** → crea una variable `accessToken` (valor vacío por ahora)

### Flujo de uso

Una vez importada la colección, el flujo es el siguiente:

1. Ejecuta `POST /auth/login` o `POST /auth/register`
2. Copia el valor de `accessToken` de la respuesta
3. Pégalo en la variable de entorno `accessToken`:
    - **Postman:** se referencia como `{{accessToken}}` en el header `Authorization: Bearer {{accessToken}}`
    - **Insomnia:** se referencia como `_.accessToken` en el header `Authorization: Bearer _.accessToken`
4. El `refreshToken` **no necesitas manejarlo** — ambas herramientas lo guardan y envían automáticamente por cookie en las peticiones a `/auth/refresh` y `/auth/logout`