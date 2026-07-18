🐾 PetCare: Sistema de Gestión Veterinaria y Expediente Clínico Multicapa

Este proyecto es una solución de software empresarial de arquitectura multicapa diseñada para
optimizar los procesos operativos, el agendamiento de citas y el control de historiales médicos
en una clínica veterinaria. El sistema mitiga por completo los fallos de entorno mediante 
contenedores aislados, implementa validaciones estrictas de datos a nivel de negocio y ofrece 
herramientas de trazabilidad como auditorías en logs y reportes interactivos listos para el usuario.

🚀 Características Principales

Expediente Clínico e Historial Médico ($1:N$): Registro modular y cronológico de consultas que almacena constantes biológicas (temperatura), síntomas, diagnósticos y tratamientos vinculados a cada mascota.
Agendamiento de Citas Inteligente: Control estricto de concurrencia y validación en el backend para evitar cruces de horarios entre veterinarios y mascotas.
Seguridad y Control de Acceso por Roles (RBAC): Autenticación robusta que segmenta las vistas y peticiones HTTP según el rol del usuario (ADMIN, VETERINARIO, CLIENTE).
Persistencia y Volúmenes Concurrentes: Almacenamiento seguro en MySQL donde los datos y archivos multimedia locales se asocian a volúmenes de Docker para garantizar la persistencia atómica ante reinicios del contenedor.
Trazabilidad y Auditoría: Emisión automatizada de recetas médicas en formato PDF de alta fidelidad (utilizando renderizado canvas) y registro de eventos críticos mediante logs estructurados en el servidor.

🧠 Arquitectura y Paradigmas Tecnológicos Aplicados

El proyecto está diseñado bajo un enfoque Full-Stack desacoplado utilizando los siguientes paradigmas técnicos de forma justificada:
Arquitectura Multicapa (Backend - Spring Boot): Separación estricta de responsabilidades mediante capas de Modelos/Entidades (JPA), Repositorios (Spring Data JPA), Servicios (Lógica de Negocio) y RestControllers (API REST). Facilita la escalabilidad del backend sin afectar la interfaz.
Paradigma Orientado a Componentes y Reactivo (Frontend - Angular): Implementación de componentes autónomos y modulares (Standalone Components) que manejan su propio ciclo de vida e interactúan mediante programación reactiva (RxJS) para procesar flujos de datos asíncronos en tiempo real.
Paradigma Declarativo y Relacional (BDD): Uso de restricciones de integridad, claves foráneas y reglas de unicidad directas en MySQL para blindar la base de datos contra la duplicidad de registros sensibles (como correos o teléfonos clonados).

📂 Estructura del Proyecto (Ecosistema Full-Stack)PlaintextvetMapache/
├── vetMapache_backend/       # Proyecto Spring Boot (Java 21 / Maven)
│   ├── src/main/java/com/example/veterinaria/
│   │   ├── DTO/              # Objetos de Transferencia de Datos (CitaDTO, MascotaDTO)
│   │   ├── Model/            # Entidades JPA (Usuario, Cliente, Cita, HistorialMedico)
│   │   ├── Repository/       # Interfaces de persistencia (HistorialMedicoRepository)
│   │   ├── Service/          # Capa de servicios y lógica de negocio (AuthService)
│   │   └── RestController/   # Endpoints de la API REST (DiagnosticoRestController)
│   ├── src/main/resources/
│   │   ├── static/uploads/   # Almacenamiento local persistente de imágenes
│   │   └── application.properties
│   └── pom.xml               # Gestor de dependencias Maven (Lombok, Spring Security)
│
├── vetMapache_frontend/      # Proyecto Angular (TypeScript)
│   ├── src/app/
│   │   ├── auth/             # Componentes y servicios de Login/Registro
│   │   ├── shared/sidebar/   # Componentes globales de navegación
│   │   └── citas/            # Gestión de citas, modales e historial médico
│   │       ├── citas.ts      # Controlador de la vista (Manejo de PDF y lógica)
│   │       ├── citas.html    # Interfaz de usuario (Formularios y modales)
│   │       ├── historial.service.ts # Consumo de la API de expedientes
│   │       └── historial.model.ts   # Modelos e interfaces TypeScript
│
├── docker-compose.yml        # Orquestador del ecosistema (Backend, Frontend, MySQL)
└── README.md                 # Documentación técnica del sistema

🛠️ Instrucciones de Despliegue Rápido

El entorno completo está automatizado con Docker. No necesitas instalar Java, Node.js ni MySQL localmente para correr el proyecto.
Clona el repositorio en tu máquina local.
Asegúrate de tener Docker Desktop encendido.
Abre una terminal en la raíz del proyecto y ejecuta el siguiente comando para compilar e iniciar todos los servicios limpiamente:
Bashdocker compose up --build
El sistema estará disponible en las siguientes direcciones de tu entorno local:Frontend (Angular): http://localhost:4200Backend (Spring Boot): http://localhost:8080
