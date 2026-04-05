# TEG - Táctica y Estrategia de la Guerra

<p align="center">
  <img src="./Backend/docs/assets/images/img.png" alt="TEG" width="300"/>
</p>

## Descripción del Proyecto

**TEG** es la implementación digital de un clásico juego de mesa de estrategia que desafía a los jugadores a conquistar el mundo mediante la planificación, negociación y toma de decisiones tácticas. Este proyecto es una aplicación web completa con arquitectura cliente-servidor desarrollada como **Trabajo Práctico Integrador (TPI)** de la asignatura Programación III de la Universidad Tecnológica Nacional — Facultad Regional Córdoba.

### Objetivos

- Proporcionar una experiencia de juego interactiva y responsiva para jugar TEG en línea
- Implementar la lógica completa del juego mediante una API REST robusta
- Permitir autenticación segura de jugadores y gestión de games multijugador
- Integrar inteligencia artificial con múltiples niveles de dificultad
- Mantener una arquitectura escalable y profesional

---

## Estructura del Proyecto

```
teg/
├── Backend/                 # API REST en Spring Boot 3.4.4
│   ├── src/
│   ├── docs/
│   ├── scripts/
│   ├── pom.xml
│   └── README.md           (referencias a Backend específicas)
├── Frontend/                # Aplicación Angular 19
│   ├── src/
│   ├── docs/
│   ├── package.json
│   ├── angular.json
│   └── README.md           (referencias a Frontend específicas)
└── README.md               (este archivo)
```

---

## Stack Tecnológico

### Backend
- **Framework**: Spring Boot 3.4.4
- **Lenguaje**: Java 17
- **Bases de Datos**: SQL Server (producción), H2 (testing)
- **Autenticación**: Spring Security + JWT
- **ORM**: Spring Data JPA + Hibernate
- **Mapeo de Objetos**: ModelMapper
- **Documentación API**: SpringDoc OpenAPI (Swagger)
- **Herramientas de Calidad**: Maven, Checkstyle, PMD
- **Gestión de Dependencias**: Maven

### Frontend
- **Framework**: Angular 19
- **Lenguaje**: TypeScript
- **Gestor de Dependencias**: npm
- **Node.js**: v18+

---

## Instalación y Configuración

### Prerequisites
- **Java 17+** y **Maven 3.6+** (para Backend)
- **Node.js v18+** y **npm v9+** (para Frontend)
- **SQL Server** (opcional; se puede usar H2 para desarrollo)
- **Angular CLI** (recomendado para desarrolladores)

### Backend

1. Navega a la carpeta Backend:
   ```bash
   cd Backend
   ```

2. Compila el proyecto:
   ```bash
   mvn clean compile
   ```

3. Ejecuta la aplicación:
   ```bash
   mvn spring-boot:run
   ```

   La API estará disponible en `http://localhost:8080`

4. **Documentación Swagger**: 
   ```
   http://localhost:8080/swagger-ui.html
   ```

### Frontend

1. Navega a la carpeta Frontend:
   ```bash
   cd Frontend
   ```

2. Instala dependencias:
   ```bash
   npm install
   ```

3. Inicia el servidor de desarrollo:
   ```bash
   npm start
   ```

   La aplicación estará disponible en `http://localhost:4200`

4. Para compilar para producción:
   ```bash
   npm run build
   ```

---

## Características Principales

### Autenticación y Seguridad
✅ Registro de usuarios  
✅ Login con JWT  
✅ Validación de tokens  
✅ Encriptación de contraseñas  
✅ Control de acceso basado en roles  

### Gestión de Partidas
✅ Creación de games multijugador (humanos y bots)  
✅ Sistema de turns con validaciones  
✅ Estados de game automáticos  
✅ Guardado y restauración de estado de game  
✅ Historial completo de eventos  

### Mecánicas de Juego
✅ Ocupación y control de países  
✅ Movimiento de ejércitos entre países conectados  
✅ Sistema de combate y batallas  
✅ Canje de tarjetas de país  
✅ Asignación y validación de objectives  
✅ Sistema de alianzas y pacts  
✅ Denuncias y reportes de jugadores  

### Comunicación
✅ Chat en tiempo real entre jugadores  
✅ Mensajes privados  
✅ Notificaciones de eventos  

---

## Arquitectura

### Backend - Estructura de Capas

```
src/main/java/ar/edu/utn/frc/tup/piii/
├── controllers/          # Controladores REST
├── services/            # Lógica de negocio
│   ├── impl/           # Implementaciones de servicios
│   └── interfaces/     # Interfaces de servicios
├── model/
│   ├── entities/       # Entidades JPA
│   ├── enums/          # Enumeraciones
│   └── repository/     # Repositorios JPA
├── dto/                # Data Transfer Objects
├── mappers/            # Mapeos entre DTOs y entidades
├── bot/                # Estrategias de inteligencia artificial
├── state/              # Patrón State para estados de game
├── exception/          # Manejo de excepciones personalizadas
├── configs/            # Configuración de la aplicación
└── util/               # Utilidades generales
```

### Frontend - Estructura de Componentes

```
src/app/
├── core/
│   ├── services/       # Servicios compartidos (API, auth, estado)
│   ├── models/         # Interfaces y modelos
│   └── guards/         # Guards de rutas
├── features/           # Pantallas y funcionalidades por dominio
│   ├── login/         # Autenticación
│   ├── lobby/         # Lista y gestión de games
│   ├── Juego/         # Tablero y lógica del juego
│   ├── cards/         # Gestión de cartas
│   └── victoria/      # Pantallas de resultados
├── shared/            # Componentes reutilizables
├── routes/            # Definición de rutas
└── assets/            # Mapas, imágenes y recursos gráficos
```

### Patrones de Diseño

#### State Pattern
Gestiona los diferentes estados de una game:
- **PreparacionState**: Fase de preparación inicial
- **PrimeraRondaState**: Primera ronda de juego
- **SegundaRondaState**: Segunda ronda de juego
- **HostilidadesState**: Fase de combates
- **FinalizadaState**: Partida finalizada

#### Strategy Pattern (Bots)
Diferentes niveles de dificultad de bots:
- **BotNovatoStrategy**: Comportamiento básico
- **BotBalanceadoStrategy**: Estrategia intermedia
- **BotExpertoStrategy**: Estrategia avanzada con algoritmo de Dijkstra

#### Memento Pattern
Guardado y restauración del estado de game para snapshots.

#### Otros Patrones
- **Factory Pattern**: Mappers y estrategias
- **Repository Pattern**: Acceso a datos con Spring Data JPA
- **DTO Pattern**: Transfer de datos entre capas

---

## Componentes Principales

### Backend - Controladores REST

| Recurso | Endpoints |
|---------|-----------|
| **Usuarios** | `POST /api/usuarios/login`, `POST /api/usuarios/register` |
| **Partidas** | `POST /api/games`, `GET /api/games`, `GET /api/games/{id}`, `PUT /api/games/{id}` |
| **Jugadores** | `GET /api/jugadores/{id}`, `POST /api/jugadores/{id}/acciones` |
| **Países** | `GET /api/games/{partidaId}/paises`, `PUT /api/games/{partidaId}/paises/{paisId}` |
| **Turnos** | `GET /api/turns/{id}`, `POST /api/turns/{id}/acciones`, `PUT /api/turns/{id}/finalizar` |
| **Chat** | `GET /api/chat/{partidaId}`, `POST /api/chat/{partidaId}` |
| **Tarjetas** | Gestión de tarjetas de país |
| **Objetivos** | Gestión de objectives de conquista |
| **Historial** | Registro de eventos de la game |

### Entidades Principales

- **Usuario**: Información de usuarios registrados
- **Partida**: Información de una game en curso
- **Turno**: Turnos dentro de una game
- **PaisPartida**: Estado de un país en una game específica
- **JugadorHumano / JugadorBot**: Tipos de jugadores
- **TarjetaJugador**: Tarjetas en poder de un jugador
- **Objetivo**: Objetivos de conquista asignados
- **Pacto**: Alianzas entre jugadores
- **Mensaje**: Chat entre jugadores
- **HistorialEvento**: Registro de eventos del juego

---

## Pruebas y Calidad

### Backend
Ejecutar tests unitarios e integración:
```bash
cd Backend
mvn test
```

La configuración incluye:
- Checkstyle para análisis estático
- PMD para detección de problemas
- Tests unitarios para servicios y controladores

## Documentación

### Backend
- **Swagger/OpenAPI**: Accesible en `http://localhost:8080/swagger-ui.html` cuando el servidor está en ejecución
- **JavaDoc**: `Backend/docs/java_doc/apidocs/index.html`
- **Diagramas UML**: `Backend/docs/app_doc/diagrams/`
- **Documentación General**: `Backend/docs/app_doc/`

### Frontend
- **Mockups y Diagramas**: `Frontend/docs/`

### General
- **Reglamento del Juego**: `Backend/docs/assets/TEG.pdf` o `Frontend/docs/TEG.pdf`

---

## Configuración de Entorno

### Backend - application.properties

Ubicado en `Backend/src/main/resources/application.properties`

Configuraciones principales:
- Puerto del servidor (por defecto 8080)
- Datasource (H2 para desarrollo o SQL Server para producción)
- Configuración JPA/Hibernate
- JWT secret y expiración
- Logging

### Frontend - Configuración de URL del Backend

Edita `Frontend/src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

Para producción, actualiza `environment.prod.ts` con la URL del servidor en producción.

**Importante**: Asegúrate de que el backend esté configurado para aceptar CORS desde el origen del frontend.

---

## Buenas Prácticas

### Backend
- Utilizar DTOs para transferencia de datos
- Mantener la lógica de negocio en servicios
- Validar entrada de datos en controladores
- Usar excepciones personalizadas para manejo de errores
- Documentar APIs con JavaDoc y anotaciones OpenAPI

### Frontend
- Mantener modelos de dominio en `core/models`
- Centralizar llamadas HTTP en servicios
- Usar guards para proteger rutas autenticadas
- Componentes pequeños con responsabilidad única
- Agregar tests unitarios para servicios y componentes críticos

---

## Contribución

1. Haz un fork/branch desde el repositorio principal
2. Implementa cambios en una rama con nombre claro (`feat/`, `fix/`, `chore/`)
3. Para Backend: Ejecuta `mvn test` y valida con `mvn clean compile`
4. Para Frontend: Ejecuta `npm install`
5. Describe los cambios en un Pull Request y referencia issues si existen

---

## Acerca del Proyecto

Este proyecto es parte del **Trabajo Práctico Integrador (TPI)** realizado durante el año 2025, de la asignatura **Programación III** de la carrera "Tecnicatura Universitaria en Programación" de la **Universidad Tecnológica Nacional - Facultad Regional Córdoba (UTN-FRC)**.

### Equipo de Desarrollo

- Belatti Mariano
- Camacho Gabriela
- Ceballos Ismael
- Chachagua Franco
- Cotaimich Santiago

**Año**: 2025

---

## Recursos Adicionales

- [Documentación del Backend](./Backend/docs/app_doc/)
- [Documentación del Frontend](./Frontend/docs/)
- [Reglamento Completo del Juego](./Backend/docs/assets/TEG.pdf)
- [Script de Base de Datos](./Backend/scripts/)

---

**Última actualización**: Marzo 2026

