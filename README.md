# BiblioVirtual 📚

## 📚 Tabla de Contenidos
1. [ℹ️ Vista General](#-vista-general)
2. [🛠️ Tecnologías Utilizadas](#-tecnologías-utilizadas)
   - [🖥️ Backend](#-backend)
   - [🎨 Frontend](#-frontend)
   - [🗄️ Base de Datos](#-base-de-datos)
3. [✨ Características Principales](#-características-principales)
   - [👤 Para Usuarios](#-para-usuarios)
   - [👑 Para Administradores](#-para-administradores)
4. [🚀 Instalación](#-instalación)
5. [📁 Estructura del Proyecto](#-estructura-del-proyecto)
6. [📦 Dependencias Principales](#-dependencias-principales)
7. [🖼️ Arquitectura Frontend](#-arquitectura-frontend)
8. [🔐 Seguridad](#-seguridad)
9. [⚙️ Configuración](#configuración)
10. [👨‍💻 Desarrollo](#desarrollo)

## ℹ️ Vista General

BiblioVirtual es una plataforma digital innovadora que ofrece una experiencia completa de gestión bibliotecaria con funcionalidades comerciales avanzadas. Diseñada tanto para usuarios como para administradores, permite la catalogación, préstamo, venta y suscripción de recursos bibliográficos de manera eficiente y moderna.

Esta aplicación surge como respuesta a la necesidad de modernizar las bibliotecas tradicionales, facilitando el acceso a materiales educativos y recreativos a través de una interfaz intuitiva y responsive. Nuestro sistema integra todas las funcionalidades esenciales de una biblioteca física, potenciadas con las ventajas del entorno digital y capacidades de comercio electrónico.

BiblioVirtual no solo gestiona libros, sino que también permite administrar diversos formatos como revistas, tesis, recursos audiovisuales y documentos digitales, incluyendo un sistema completo de ventas, carrito de compras y planes de suscripción.

## 🛠️ Tecnologías Utilizadas

### 🖥️ Backend
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6-green)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-Latest-green)
![Spring Boot Actuator](https://img.shields.io/badge/Spring%20Boot%20Actuator-Latest-green)
![Lombok](https://img.shields.io/badge/Lombok-Latest-blue)
![MapStruct](https://img.shields.io/badge/MapStruct-1.5.5.Final-blue)
![Spring Boot Mail](https://img.shields.io/badge/Spring%20Boot%20Mail-Latest-blue)
![Quartz Scheduler](https://img.shields.io/badge/Quartz%20Scheduler-Latest-blue)

### 🎨 Frontend
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Latest-green)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3.5-purple)
![Bootstrap Icons](https://img.shields.io/badge/Bootstrap%20Icons-1.11.1-purple)
![Font Awesome](https://img.shields.io/badge/Font%20Awesome-6.5.2-blue)
![jQuery](https://img.shields.io/badge/jQuery-3.7.0-blue)
![DataTables](https://img.shields.io/badge/DataTables-1.13.4-blue)

### 🗄️ Base de Datos
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

### 📄 Documentación y Herramientas
![Swagger/OpenAPI](https://img.shields.io/badge/Swagger/OpenAPI-3.0-green)
![iText PDF](https://img.shields.io/badge/iText%20PDF-Latest-red)
![Apache PDFBox](https://img.shields.io/badge/Apache%20PDFBox-3.0.1-red)

## ✨ Características Principales

### 👤 Para Usuarios
- **Autenticación y Perfiles**: Registro y gestión de perfiles personalizados con validaciones
- **Catálogo Avanzado**: Búsqueda y filtrado de obras con múltiples criterios
- **Sistema de Préstamos**: Gestión completa de préstamos y devoluciones
- **E-commerce**: Carrito de compras y proceso de checkout
- **Suscripciones**: Planes de membresía con beneficios exclusivos
- **Dashboard Personal**: Panel de control con actividades y estadísticas
- **Generación de PDFs**: Reportes personalizados y comprobantes

### 👑 Para Administradores
- **Panel de Control Completo**: Dashboard con métricas y estadísticas en tiempo real
- **Gestión de Usuarios**: CRUD completo de usuarios con diferentes roles
- **Administración de Contenido**: Gestión de libros, categorías y autores
- **Control Comercial**: Gestión de ventas, suscripciones y precios
- **Sistema de Auditoría**: Registro completo de actividades del sistema
- **Reportes Avanzados**: Generación de reportes PDF con datos comerciales
- **Configuración del Sistema**: Administración de configuraciones globales

## 🚀 Instalación

### Requisitos Previos
- Java JDK 17 o superior
- Maven 3.6 o superior
- MySQL 8.0 o superior
- Git

### Configuración de Base de Datos
```sql
CREATE DATABASE biblioteca_db;
CREATE USER 'biblioteca_user'@'localhost' IDENTIFIED BY 'tu_password';
GRANT ALL PRIVILEGES ON biblioteca_db.* TO 'biblioteca_user'@'localhost';
FLUSH PRIVILEGES;
```

### Pasos para ejecutar localmente

```bash
# 1. Clonar el repositorio
git clone https://github.com/Omar-Cc/biblioteca.git
cd biblioteca

# 2. Configurar application.properties
# Editar src/main/resources/application.properties con tus credenciales de BD

# 3. Compilar el proyecto
mvn clean install

# 4. Ejecutar la aplicación
mvn spring-boot:run

# 5. Acceder a la aplicación
# http://localhost:8080
```

## 📁 Estructura del Proyecto

```bash
src/
└── main/
    ├── java/
    │   └── com/
    │       └── biblioteca/
    │           ├── annotations/             # Anotaciones personalizadas para validaciones y metadatos
    │           ├── aspects/                 # Aspectos de programación orientada a aspectos (AOP)
    │           ├── config/                  # Configuraciones de Spring Boot y beans
    │           ├── controller/              # Controladores REST y MVC
    │           │   ├── admin/               # Endpoints para funcionalidades de administración
    │           │   ├── cuenta/              # Endpoints para gestión de cuentas de usuario
    │           │   └── publico/             # Endpoints públicos (catálogo, registro, etc.)
    │           ├── dto/                     # Objetos de transferencia de datos
    │           │   ├── comercial/           # DTOs para operaciones comerciales (ventas, suscripciones)
    │           │   ├── dashboard/           # DTOs para métricas y datos del dashboard
    │           │   └── validacion/          # DTOs específicos para validaciones
    │           │       ├── perfiles/        # Validaciones específicas de perfiles
    │           │       ├── prestamos/       # Validaciones específicas de préstamos
    │           │       └── suscripciones/   # Validaciones específicas de suscripciones
    │           ├── enums/                   # Enumeraciones del dominio (estados, tipos, roles)
    │           ├── events/                  # Eventos del sistema para comunicación asíncrona
    │           ├── exceptions/              # Excepciones personalizadas y manejo de errores
    │           ├── mapper/                  # Mappers para conversión entre entidades y DTOs
    │           ├── middleware/              # Middleware para interceptores y filtros
    │           ├── models/                  # Entidades del dominio y modelos de datos
    │           │   ├── acceso/              # Entidades relacionadas con autenticación y autorización
    │           │   ├── actividades/         # Entidades para auditoría y registro de actividades
    │           │   ├── comercial/           # Entidades comerciales (ventas, pagos, suscripciones)
    │           │   └── contenido/           # Entidades de contenido (libros, categorías, autores)
    │           ├── repositories/            # Interfaces de acceso a datos (JPA)
    │           │   ├── actividades/         # Repositorios para auditoría y actividades
    │           │   ├── comercial/           # Repositorios para operaciones comerciales
    │           │   └── contenido/           # Repositorios para gestión de contenido
    │           ├── scheduler/               # Tareas programadas y jobs automatizados
    │           └── service/                 # Servicios de lógica de negocio
    │               └── impl/                # Implementaciones concretas de los servicios
    └── resources/
        ├── data/                           # Archivos de datos iniciales (JSON, SQL) para la BD
        ├── static/                         # Recursos estáticos del frontend
        │   ├── css/                        # Hojas de estilo CSS
        │   ├── image/                      # Imágenes estáticas (logos, iconos, etc.)
        │   └── js/                         # Scripts JavaScript del frontend
        └── templates/                      # Plantillas Thymeleaf para las vistas
            ├── admin/                      # Vistas del panel de administración
            ├── auth/                       # Vistas de autenticación (login, registro, recuperación)
            ├── email/                      # Plantillas para correos electrónicos automatizados
            ├── error/                      # Páginas de error personalizadas (404, 500, etc.)
            ├── fragmentos/                 # Componentes reutilizables de Thymeleaf
            │   ├── componentes/            # Componentes específicos (modales, formularios)
            │   └── layouts/                # Layouts base y estructura común
            ├── lectores/                   # Vistas específicas para el rol de lector
            ├── mi-cuenta/                  # Vistas del área personal del usuario
            ├── pdf/                        # Plantillas para generación de reportes PDF
            ├── public/                     # Vistas públicas accesibles sin autenticación
            │   ├── carrito/                # Vistas del carrito de compras
            │   ├── catalogo/               # Vistas del catálogo de libros
            │   ├── checkout/               # Vistas del proceso de compra
            │   └── planes/                 # Vistas de planes de suscripción
            └── usuarios/                   # Vistas de gestión de usuarios (CRUD)
```

## 📦 Dependencias Principales

### **Dependencias Core Activamente Usadas** ✅
- **Spring Boot Web Starter** - Framework MVC y REST
- **Spring Boot Data JPA** - Persistencia y ORM
- **Spring Boot Thymeleaf** - Motor de plantillas
- **Spring Boot Security** - Autenticación y autorización
- **Spring Boot Validation** - Validaciones de entrada
- **MySQL Connector** - Driver de base de datos
- **Spring Boot DevTools** - Desarrollo y hot reload
- **Lombok** - Reducción de código boilerplate
- **MapStruct** - Mapeo entre entidades y DTOs
- **Spring Boot Actuator** - Monitoreo y métricas
- **Thymeleaf Spring Security** - Integración de seguridad en vistas
- **iText PDF** - Generación de reportes PDF
- **Apache PDFBox** - Manipulación de documentos PDF

### **Dependencias Configuradas para Funcionalidades Avanzadas** ⚠️
- **Spring Boot Mail** - Envío de correos electrónicos
- **Spring Boot Quartz** - Tareas programadas avanzadas
- **Spring Retry** - Reintentos automáticos
- **Google Guava** - Utilidades para cache y colecciones
- **SpringDoc OpenAPI** - Documentación automática de API (Swagger)

## 🖼️ Arquitectura Frontend
La interfaz de usuario está construida con Thymeleaf y Bootstrap, siguiendo una estructura modular:

### Layouts Base
- **base-layout.html**: Estructura principal para usuarios públicos
- **admin-layout.html**: Diseño especializado para el panel de administración
- **cuenta-layout.html**: Layout para la sección de cuenta de usuario
- **auth-layout.html**: Layout para procesos de autenticación

### Características Frontend
- **Tablas Interactivas**: DataTables con paginación, búsqueda y ordenamiento
- **Sistema de Notificaciones**: Alertas con auto-cierre y diferentes tipos
- **Interfaces Responsivas**: Diseño adaptativo para múltiples dispositivos
- **Sidebar Dinámico**: Navegación colapsable en panel de administración
- **Componentes Reutilizables**: Fragmentos Thymeleaf modulares
- **Carrito de Compras**: Funcionalidad completa de e-commerce
- **Generación de PDFs**: Reportes en tiempo real

## 🔐 Seguridad
El proyecto implementa Spring Security 6 con las siguientes características:

- **Autenticación Multifactor**: Sistema robusto de login con validaciones
- **Autorización por Roles**: Control granular (ADMIN, USER, LECTOR)
- **Protección CSRF**: Seguridad contra ataques de falsificación
- **Gestión de Sesiones**: Control de sesiones activas y timeout
- **Validación de Entrada**: Sanitización de datos con Bean Validation
- **Políticas de Contraseñas**: Requisitos de seguridad configurables
- **Auditoría de Seguridad**: Registro de eventos de seguridad

## ⚙️ Configuración

### Variables de Entorno Recomendadas
```properties
# Base de datos
MYSQL_HOST=localhost:3306
MYSQL_DATABASE=biblioteca_db
MYSQL_USERNAME=biblioteca_user
MYSQL_PASSWORD=tu_password_seguro

# Configuración de aplicación
SERVER_PORT=8080
LOGGING_LEVEL=INFO

# Email (opcional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu_email@gmail.com
MAIL_PASSWORD=tu_app_password
```

### Configuración de Datos Iniciales
La aplicación utiliza archivos JSON ubicados en `src/main/resources/data/` para la carga inicial de datos (seed data) incluyendo:
- Usuarios por defecto
- Categorías de libros
- Libros de ejemplo
- Planes de suscripción
- Configuraciones del sistema

## 👨‍💻 Desarrollo

### Comandos de Desarrollo
```bash
# Ejecutar en modo desarrollo con recarga automática
mvn spring-boot:run

# Ejecutar tests
mvn test

# Limpiar y compilar
mvn clean compile

# Generar documentación API
# Acceder a: http://localhost:8080/swagger-ui.html

# Monitoreo de aplicación
# Acceder a: http://localhost:8080/actuator
```

### URLs Importantes
- **Aplicación Principal**: http://localhost:8080
- **Panel de Administración**: http://localhost:8080/admin
- **Documentación API**: http://localhost:8080/swagger-ui.html
- **Métricas de la Aplicación**: http://localhost:8080/actuator
- **Health Check**: http://localhost:8080/actuator/health

### Usuarios por Defecto
```
Administrador:
- Email: admin@biblioteca.com
- Password: admin123

Usuario Estándar:
- Email: usuario@biblioteca.com  
- Password: user123
```