1. Jugadores y Participación
IJugador (Interfaz)
Define el contrato que deben cumplir todos los jugadores (humanos o bots). Incluye métodos clave como:
- realizarTurno(), atacar(), reagrupar(), desplazarEjercito(), verObjetivo(), solicitarTarjeta(), canjearTarjetas()
Esto permite una lógica polimórfica: el sistema trata igual a cualquier jugador, sin importar si es humano o bot.

JugadorBase (Clase abstracta)
Contiene atributos comunes a todos los jugadores:
id, nombre, ejercitosDisponibles, territorios
Hereda de IJugador y es extendida por todas las clases concretas de jugadores.

JugadorHumano
Representa un jugador controlado por una persona real. Es una subclase concreta de JugadorBase.

JugadorBot, JugadorBotNovato, JugadorBotBalanceado, JugadorBotExperto
JugadorBot es una clase abstracta intermedia con lógica genérica de IA.
Las subclases concretas (Novato, Balanceado, Experto) definen diferentes niveles de dificultad y comportamiento estratégico.
Están asociadas a NivelBot para identificar su tipo.

NivelBot
Contiene los valores posibles para los bots: Novato, Balanceado, Experto.

2. Gestión de Usuarios y Roles
Usuario y Rol
Separa la información del sistema (credenciales, roles) de los jugadores dentro del juego.
Un Usuario puede tener un Rol (por ejemplo, "admin", "jugador").
Relación:
Usuario se vincula con JugadorHumano si decide participar en una game.

3. Gestión de Partidas
Partida
Es el núcleo del juego. Coordina y almacena toda la información relacionada a una sesión. Contiene:
Estado de la game (EstadoPartida), Tipo de comunicación (TipoComunicacion), Objetivo común
Métodos para iniciar, continuar, guardar, cargar y controlar las fases del juego.

JugadorPartida
Asocia un JugadorBase con una Partida.
Define el color del jugador para esa game.
Indica si es su turn y qué objective secreto tiene.
Permite reutilizar jugadores en distintas games sin duplicar objetos.

Color
Define colores disponibles por jugador. Se asigna en JugadorPartida.

4. Turnos
Turno
Representa un turn de un jugador en una game:
Indica su faseActual (colocación, ataque, reagrupación)
Tiene un límite de tiempo y cantidad de ejércitos disponibles.
Métodos: finalizarTurno(), reiniciar()
Se vincula a:
JugadorPartida: quién está jugando
Partida: a cuál pertenece

5. Objetivos
Objetivo
Define una meta a cumplir (destruir color, conquistar regiones, etc.).
Los jugadores tienen uno secreto (JugadorPartida) y uno común (Partida).

6. Mapa del Mundo
Pais, Continente
Pais representa un territorio conquistable. Pertenece a un Continente.
Los continentes otorgan bonus por ocupación.

ConexionPais
Representa los vínculos entre países, para validar ataques y reagrupaciones.

PaisPartida
Estado dinámico del país dentro de una game:
A qué jugador pertenece
Cuántos ejércitos hay en él

7. Sistema de Cartas
TarjetaPais, Simbolo
Cada país tiene una tarjeta asociada con un símbolo (Globo, Cañón, Galeón).

TarjetaJugador
Registra las cartas que tiene un jugador en una game.

Canje y CanjeTarjetas
Canje: acción de intercambiar tarjetas por ejércitos.

CanjeTarjetas: relación entre el exchange y las tarjetas usadas.

8. Pactos
Pacto
Registra acuerdos entre jugadores:
Tipo de pact (TipoPacto: no agresión, zona internacional)
Jugadores involucrados
Países donde aplica
Métodos: realizarPacto(), romperPacto(), validarPactoEnAtaque()

9. Comunicación y Control Social
Mensaje
Representa los messages enviados entre jugadores en una game (si la comunicación está habilitada).

Denuncia
Permite reportar violaciones al estilo de comunicación (Fair Play o Vale Todo).
Denuncia tiene un acusador y un acusado.

10. Historial de Juego
HistorialEvento
Contiene el registro de acciones relevantes en la game: ataques, conquistas, cumplimiento de objectives, etc.
