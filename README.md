# SGS Supermercado — Backend

> API REST desarrollada con **Spring Boot 4** para la gestión de un supermercado. Incluye autenticación JWT con verificación de correo electrónico, gestión de productos y categorías, y control de acceso basado en roles.

---

## Tabla de Contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Requisitos Previos](#requisitos-previos)
- [Configuración](#configuración)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Endpoints de la API](#endpoints-de-la-api)
- [Seguridad](#seguridad)
- [Usuario Administrador por Defecto](#usuario-administrador-por-defecto)

---

## Características

- ✅ Registro de usuarios con verificación por correo electrónico (código de 6 dígitos, expira en 15 min)
- ✅ Autenticación stateless mediante **JWT** (expiración 24 horas)
- ✅ Control de acceso por roles: `ADMIN` y `CLIENTE`
- ✅ CRUD completo de **Productos** con soporte para eliminación lógica (soft delete)
- ✅ CRUD completo de **Categorías**
- ✅ Validación de datos en todos los endpoints con respuestas de error descriptivas
- ✅ CORS configurado para integración con el frontend Angular
- ✅ Creación automática de la base de datos y usuario administrador al iniciar

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.3 |
| Spring Security | 6.x |
| Spring Data JPA | 3.x |
| MySQL | 8.x |
| JJWT | 0.12.6 |
| Lombok | latest |
| Maven | 3.9.x |

---

## Requisitos Previos

- **JDK 21** o superior
- **MySQL 8** corriendo en `localhost:3306`
- **Maven 3.9+** (o usar el wrapper incluido `./mvnw`)
- Cuenta de **Gmail** con una [App Password](https://myaccount.google.com/apppasswords) configurada para el envío de correos

---

## Configuración

Edita el archivo `src/main/resources/application.properties` con tus credenciales:

```properties
# Base de Datos
spring.datasource.url=jdbc:mysql://localhost:3306/sgs_db?createDatabaseIfNotExist=true
spring.datasource.username=TU_USUARIO_MYSQL
spring.datasource.password=TU_PASSWORD_MYSQL

# JWT
jwt.secret=TU_SECRET_BASE64
jwt.expiration=86400000

# Correo (Gmail)
spring.mail.username=TU_CORREO@gmail.com
spring.mail.password=TU_APP_PASSWORD
```

> **Nota de seguridad:** No subas credenciales reales al repositorio. En producción, usa variables de entorno o un servicio de secrets.

---

## Instalación y Ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/sebastianagudelom/sgs-backend.git
cd sgs-backend

# 2. Compilar el proyecto
./mvnw clean compile

# 3. Ejecutar
./mvnw spring-boot:run
```

La API quedará disponible en: **`http://localhost:8080`**

---

## Estructura del Proyecto

```
src/main/java/com/uniquindio/backend/
├── config/
│   ├── CorsConfig.java           # Configuración CORS (permite localhost:4200)
│   ├── DataInitializer.java      # Crea el admin por defecto al iniciar
│   ├── GlobalExceptionHandler.java
│   └── JwtProperties.java        # @ConfigurationProperties para JWT
├── controller/
│   ├── AuthController.java       # /api/auth/**
│   ├── CategoriaController.java  # /api/categorias/**
│   └── ProductoController.java   # /api/productos/**
├── dto/
│   ├── AuthResponse.java
│   ├── CategoriaRequest.java / CategoriaResponse.java
│   ├── LoginRequest.java
│   ├── MensajeResponse.java
│   ├── ProductoRequest.java / ProductoResponse.java
│   ├── RegistroRequest.java
│   └── VerificacionRequest.java
├── model/
│   ├── Categoria.java
│   ├── Producto.java
│   ├── Rol.java                  # Enum: ADMIN, CLIENTE
│   └── Usuario.java
├── repository/
│   ├── CategoriaRepository.java
│   ├── ProductoRepository.java
│   └── UsuarioRepository.java
├── security/
│   ├── JwtAuthFilter.java        # Filtro de autenticación JWT
│   ├── JwtService.java           # Generación y validación de tokens
│   └── SecurityConfig.java       # Cadena de seguridad HTTP
└── service/
    ├── AuthService.java
    ├── CategoriaService.java
    ├── EmailService.java
    └── ProductoService.java
```

---

## Endpoints de la API

### Autenticación — `/api/auth`

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/registro` | Público | Registra un nuevo usuario |
| `POST` | `/api/auth/login` | Público | Inicia sesión, retorna JWT |
| `POST` | `/api/auth/verificar` | Público | Verifica cuenta con código |
| `POST` | `/api/auth/reenviar-codigo` | Público | Reenvía código de verificación |

### Productos — `/api/productos`

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/productos` | Público | Lista todos los productos activos |
| `GET` | `/api/productos/{id}` | Público | Obtiene un producto por ID |
| `GET` | `/api/productos/buscar?nombre=` | Público | Busca productos por nombre |
| `GET` | `/api/productos/categoria/{id}` | Público | Lista productos por categoría |
| `POST` | `/api/productos` | `ADMIN` | Crea un nuevo producto |
| `PUT` | `/api/productos/{id}` | `ADMIN` | Actualiza un producto |
| `DELETE` | `/api/productos/{id}` | `ADMIN` | Elimina lógicamente un producto |

### Categorías — `/api/categorias`

| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/categorias` | Público | Lista todas las categorías |
| `GET` | `/api/categorias/{id}` | Público | Obtiene una categoría por ID |
| `POST` | `/api/categorias` | `ADMIN` | Crea una nueva categoría |
| `PUT` | `/api/categorias/{id}` | `ADMIN` | Actualiza una categoría |
| `DELETE` | `/api/categorias/{id}` | `ADMIN` | Elimina una categoría |

---

## Seguridad

Los endpoints protegidos requieren el header:

```
Authorization: Bearer <token_jwt>
```

El token se obtiene al hacer login exitoso. Expira en **24 horas**.

El flujo de registro con verificación es:
1. `POST /api/auth/registro` → Se crea el usuario inactivo y se envía un código al correo
2. `POST /api/auth/verificar` → Se activa la cuenta con el código (válido 15 min)
3. `POST /api/auth/login` → Se obtiene el JWT

---

## Usuario Administrador por Defecto

Al iniciar la aplicación por primera vez se crea automáticamente:

| Campo | Valor |
|---|---|
| Email | `admin@sgssupermercado.com` |
| Contraseña | `Admin123` |
| Rol | `ADMIN` |

---

## Proyecto Universitario

Desarrollado para la asignatura **Software 3** — Universidad del Quindío.
