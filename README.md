# BiblioVirtual 📚
Vista General
BiblioVirtual es una plataforma digital innovadora que ofrece una experiencia completa de biblioteca virtual. Permite a los usuarios acceder a una amplia colección de libros digitales, revistas, artículos académicos y otros formatos, disponibles para préstamo o adquisición desde cualquier dispositivo.

Backend:

<img alt="Java 17" src="https://img.shields.io/badge/Java-17-orange">
<img alt="Spring Boot" src="https://img.shields.io/badge/Spring Boot-3.4.5-green">
<img alt="Spring Security" src="https://img.shields.io/badge/Spring Security-Latest-green">
<img alt="Lombok" src="https://img.shields.io/badge/Lombok-Latest-blue">
<img alt="MapStruct" src="https://img.shields.io/badge/MapStruct-Latest-blue">
Frontend:

<img alt="Thymeleaf" src="https://img.shields.io/badge/Thymeleaf-Latest-green">
<img alt="Bootstrap" src="https://img.shields.io/badge/Bootstrap-5.3.5-purple">
<img alt="Bootstrap Icons" src="https://img.shields.io/badge/Bootstrap Icons-1.11.1-purple">
<img alt="Font Awesome" src="https://img.shields.io/badge/Font Awesome-6.5.2-blue">
<img alt="jQuery" src="https://img.shields.io/badge/jQuery-3.7.0-blue">
<img alt="DataTables" src="https://img.shields.io/badge/DataTables-1.13.4-blue">

✨ Características Principales
Para Usuarios
Registro y gestión de perfiles personalizados
Catálogo de obras con búsqueda avanzada
Sistema de préstamos y reservas
Planes de suscripción con beneficios
Interfaz responsive para acceso desde cualquier dispositivo
Para Administradores
Panel de administración completo
Gestión de usuarios y lectores
Control de obras y contenidos
Administración de préstamos
Reportes y estadísticas
Gestión de planes y suscripciones

# 🚀 Instalación
Requisitos Previos
Java JDK 17 o superior
Maven 3.6 o superior
Pasos
# Clonar el repositorio
git clone https://github.com/Omar-Cc/biblioteca.git
cd biblioteca

# Compilar el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run

# ESTRUCTURA DEL PROYECTO
<pre>
src/
├── main/
│   ├── java/com/biblioteca/
│   │   ├── config/        # Configuraciones de la aplicación
│   │   ├── controller/    # Controladores MVC
│   │   │   ├── admin/     # Controladores para área de administración
│   │   │   ├── cuenta/    # Controladores para cuenta de usuario
│   │   │   └── publico/   # Controladores para área pública
│   │   ├── dto/           # Objetos de transferencia de datos
│   │   ├── enums/         # Enumeraciones
│   │   ├── mapper/        # Mappers para transformar objetos (MapStruct)
│   │   ├── models/        # Entidades y modelos
│   │   └── service/       # Servicios de negocio
│   │       └── impl/      # Implementaciones de servicios
│   └── resources/
│       ├── data/          # Datos JSON de la aplicación
│       ├── static/        # Recursos estáticos (JS, CSS, imágenes)
│       ├── templates/     # Plantillas Thymeleaf
│       └── application.properties # Configuración de la aplicación
</pre>

🎨 Arquitectura Frontend
La interfaz de usuario está construida con Thymeleaf y Bootstrap, siguiendo una estructura modular:

Layouts Base
base-layout.html: Estructura principal para usuarios
admin-layout.html: Diseño para el panel de administración
cuenta-layout.html: Layout para sección de cuenta de usuario

Características Frontend
Tablas interactivas con paginación y búsqueda
Sistema de notificaciones con auto-cierre
Interfaces responsivas para múltiples dispositivos
Sidebar colapsable en panel de administración
Componentes reutilizables mediante fragmentos Thymeleaf

🔒 Seguridad
Autenticación y autorización con Spring Security
Manejo personalizado de sesiones
Protección contra CSRF
Diferentes niveles de acceso según rol (ADMIN, USER, LECTOR)
Configuración de políticas de contraseñas

CONFIGURACIÓN
La aplicación utiliza archivos JSON para almacenar datos. Estos se encuentran en: src/main/resources/data/

SEGURIDAD
El proyecto implementa Spring Security para la autenticación y autorización de usuarios, con funcionalidades como:

Manejo personalizado de inicio y cierre de sesión
Validación de sesiones
Gestión de roles y permisos

DESARROLLO
Para el desarrollo se recomienda utilizar los siguientes comandos:

Ejecutar en modo desarrollo con recarga automática: mvn spring-boot:run
