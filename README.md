# BiblioVirtual 📚

## 📚 Tabla de Contenidos
1. [ℹ️ Vista General](#-vista-general)
2. [🛠️ Tecnologías Utilizadas](#-tecnologías-utilizadas)
   - [🖥️ Backend](#-backend)
   - [🎨 Frontend](#-frontend)
3. [✨ Características Principales](#-características-principales)
   - [👤 Para Usuarios](#-para-usuarios)
   - [👑 Para Administradores](#-para-administradores)
4. [🚀 Instalación](#-instalación)
5. [📁 Estructura del Proyecto](#-estructura-del-proyecto)
6. [🖼️ Arquitectura Frontend](#-arquitectura-frontend)
7. [🔐 Seguridad](#-seguridad)
8. [⚙️ Configuración](#configuración)
9. [👨‍💻 Desarrollo](#desarrollo)

## ℹ️ Vista General

BiblioVirtual es una plataforma digital innovadora que ofrece una experiencia completa de gestión bibliotecaria. Diseñada tanto para usuarios como para administradores, permite la catalogación, préstamo y seguimiento de recursos bibliográficos de manera eficiente y moderna.

Esta aplicación surge como respuesta a la necesidad de modernizar las bibliotecas tradicionales, facilitando el acceso a materiales educativos y recreativos a través de una interfaz intuitiva y responsive. Nuestro sistema integra todas las funcionalidades esenciales de una biblioteca física, potenciadas con las ventajas del entorno digital.

BiblioVirtual no solo gestiona libros, sino que también permite administrar diversos formatos como revistas, tesis, recursos audiovisuales y documentos digitales, convirtiéndola en una solución completa para instituciones educativas, bibliotecas públicas y privadas.

## 🛠️ Tecnologías Utilizadas

### 🖥️ Backend
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-green)
![Spring Security](https://img.shields.io/badge/Spring%20Security-Latest-green)
![Lombok](https://img.shields.io/badge/Lombok-Latest-blue)
![MapStruct](https://img.shields.io/badge/MapStruct-1.5.5.Final-blue)

### 🎨 Frontend
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Latest-green)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3.5-purple)
![Bootstrap Icons](https://img.shields.io/badge/Bootstrap%20Icons-1.11.1-purple)
![Font Awesome](https://img.shields.io/badge/Font%20Awesome-6.5.2-blue)
![jQuery](https://img.shields.io/badge/jQuery-3.7.0-blue)
![DataTables](https://img.shields.io/badge/DataTables-1.13.4-blue)

## ✨ Características Principales

### 👤 Para Usuarios
- Registro y gestión de perfiles personalizados
- Catálogo de obras con búsqueda avanzada
- Sistema de préstamos y reservas
- Planes de suscripción con beneficios
- Interfaz responsive para acceso desde cualquier dispositivo
### 👑 Para Administradores
- Panel de administración completo
- Gestión de usuarios y lectores
- Control de obras y contenidos
- Administración de préstamos
- Reportes y estadísticas
- Gestión de planes y suscripciones

## 🚀 Instalación

### Requisitos Previos
- Java JDK 17 o superior
- Maven 3.6 o superior

### Pasos para ejecutar localmente

```bash
# 1. Clonar el repositorio
git clone https://github.com/Omar-Cc/biblioteca.git
cd biblioteca

# 2. Compilar el proyecto
mvn clean install

# 3. Ejecutar la aplicación
mvn spring-boot:run
```

## 📁 Estructura del Proyecto

```bash
src/
├── main/
│   ├── java/com/biblioteca/
│   │   ├── config/         # ⚙️ Configuraciones de la aplicación
│   │   ├── controller/     # 🌐 Controladores MVC
│   │   │   ├── admin/      # Panel de administración
│   │   │   ├── cuenta/     # Sección de cuenta de usuario
│   │   │   └── publico/    # Sección pública
│   │   ├── dto/            # 📦 DTOs (Data Transfer Objects)
│   │   ├── enums/          # 🔢 Enumeraciones
│   │   ├── mapper/         # 🔄 Transformaciones con MapStruct
│   │   ├── models/         # 🧩 Entidades y modelos
│   │   └── service/
│   │       └── impl/       # 💼 Lógica de negocio (implementaciones de servicio)
│   └── resources/
│       ├── data/           # 🗂️ Datos JSON de la aplicación
│       ├── static/         # 🖼️ Recursos estáticos (JS, CSS, imágenes)
│       ├── templates/      # 📝 Plantillas Thymeleaf
│       └── application.properties # ⚙️ Configuración general
```

## 🖼️ Arquitectura Frontend
La interfaz de usuario está construida con Thymeleaf y Bootstrap, siguiendo una estructura modular:

### Layouts Base
- base-layout.html: Estructura principal para usuarios
- admin-layout.html: Diseño para el panel de administración
- cuenta-layout.html: Layout para sección de cuenta de usuario

### Características Frontend
- Tablas interactivas con paginación y búsqueda
- Sistema de notificaciones con auto-cierre
- Interfaces responsivas para múltiples dispositivos
- Sidebar colapsable en panel de administración
- Componentes reutilizables mediante fragmentos Thymeleaf

## 🔐 Seguridad
El proyecto implementa Spring Security para la autenticación y autorización de usuarios, con funcionalidades como:

- Autenticación y autorización con Spring Security
- Manejo personalizado de sesiones
- Protección contra CSRF
- Gestión de roles y permisos (ADMIN, USER, LECTOR)
- Validación y políticas de contraseñas

## ⚙️ Configuración
La aplicación utiliza archivos JSON ubicados en `src/main/resources/data/` principalmente para la carga inicial de datos (seed data) al iniciar la aplicación.

## 👨‍💻 Desarrollo
Para el desarrollo se recomienda utilizar los siguientes comandos:

Ejecutar en modo desarrollo con recarga automática: ```mvn spring-boot:run```
