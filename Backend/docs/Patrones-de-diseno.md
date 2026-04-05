# **Patrones de diseño**

Los patrones de diseño son soluciones reutilizables a problemas comunes en el diseño de software. No son código específico, sino plantillas o esquemas que se pueden adaptar a distintas situaciones.

**Creacionales:** Se enfocan en la forma en que se crean los objetos (ej. Singleton, Factory, Builder).

**Estructurales:** Se enfocan en cómo se componen los objetos o clases (ej. Adapter, Composite, Decorator).

**De comportamiento:** Se enfocan en cómo los objetos interactúan y se comunican (ej. Observer, Strategy, Command).

_Los patrones que usaremos en este trabajo práctico son: factory, repository, mvc, state, observer, mediator, strategy y memento._

# **El patrón Repository**

¿Qué hace?

Es un patrón **estructural** que se ocupa del diseño de acceso a datos que actúa como una capa intermedia entre la lógica de negocio y el origen de datos (por ejemplo, una base de datos, archivo o API externa).

¿Qué soluciona?

El patrón de diseño Repository soluciona problemas de interacción entre la lógica de negocio y la persistencia de datos. Proporciona una abstracción de la base de datos, permitiendo que la lógica de la aplicación trabaje con una interfaz común para acceder, manipular y almacenar datos, independientemente de la fuente de datos subyacente.

Para el proyecto usaremos Java con Spring Boot que es un framework basado en el ecosistema Spring que facilita la creación de aplicaciones Java, especialmente para aplicaciones web y microservicios. Internamente. **Spring Boot lo usa implícitamente a través de Spring Data JPA** (similar a lo que vimos en el cuatrimestre anterior con C# y entity framework solo que este último es un ORM y en el caso de Spring el ORM por defecto es Hibernate).

### **Ejemplo de consulta y manejo de la base de datos**:

<br>

Antes de repository (que lo usa implícitamente spring boot):

Cada vez que queramos crear operaciones del CRUD deberíamos crear las respectivos comandos en sql. Antes deberíamos abrir la conexión, hacer el constructor, usar un método al que teníamos que pasarle los parámetros para hacer las consultas SQL y manejábamos los errores .

Con uso de repository (con spring):

Definimos la entidad como clase java, creamos una interfaz que extiende JpaRepository y Spring genera todo el acceso a la base de datos por nosotros, lo único que queda es generar la lógica dentro de cada método.

# **El patrón Factory**

¿Qué hace?

Es un patrón de diseño **creacional** que proporciona una interfaz para crear objetos en una superclase, mientras permite a las subclases alterar el tipo de objetos que se crearán.

¿Qué soluciona?

Soluciona el problema de crear familias enteras de productos sin acoplarse a sus clases concretas. En concreto, define una interfaz para la creación de todos los productos de una familia, pero deja que sean las fábricas concretas quienes implementan la creación real de cada uno de ellos. Desacopla quién pide las implementaciones concretas, centraliza la lógica de creación en un solo lugar y facilita agregar nuevos tipos de tarjeta sin tocar el código cliente.

### **Ejemplo de creación de bots**

Permite delegar la creación de objetos a una clase especializada, sin acoplar el cliente a clases concretas. Se puede aplicar creando una fábrica de bots (ej = BotFactory ) dado un nivel de dificultad, devuelve una instancia de BotService. Este patrón solo gestiona la “elección y creación” del bot, pero no ayuda a simplificar el comportamiento interno del bot. Además, solo se\*\* usa al principio, cuando hay que instanciar algo configurable sin acoplarse a sus clases concretas.

```
BotService bot = BotFactory.createBot(nivelSeleccionado);
```

_El bot se crea con una estrategia acorde al nivel pero ya no se cambia automáticamente después._

También con factory se puede gestionar → configuración de games (tablero, jugadores, cartas iniciales, estado de turn inicial, número de ejércitos iniciales, distribución de territorios).

**El patrón MVC**

¿Qué hace?

MVC (Modelo-Vista-Controlador) es un patrón en el diseño **estructural** comúnmente utilizado para implementar interfaces de user, datos y lógica de control. Enfatiza una separación entre la lógica de negocios y su visualización. Proporciona una mejor división del trabajo y una mejora de mantenimiento. Su clasificación no

¿Qué soluciona?

Resuelve principalmente el problema del acoplamiento entre la interfaz y la lógica del negocio, permitiendo cambiar la vista (proyecto frontend de Angular + Typescript) sin modificar el backend. Al separar responsabilidades, mejora la mantenibilidad, facilita la detección y corrección de errores, y permite agregar nuevas funcionalidades sin afectar otras partes del sistema. También hace que el proyecto sea más escalable y flexible, permitiendo crecer sin rehacer todo, y posibilita que distintos equipos trabajen en paralelo sobre backend, frontend y lógica de juego.

### **Ejemplo usando los modelos creados:**

**Modelo**: Encargado de representar y gestionar el estado del juego y las reglas del dominio. Carpetas/clases que forman el Modelo:

`model/entities`: clases como `Jugador`, `Partida`, `Objetivo`, `Turno`, etc. → representan el estado persistente del juego.

`model/repository`: `PartidaRepository`, `UsuarioRepository` → acceden a la base de datos.

`model/enums`: constantes de dominio (enums) como `FaseTurno` podrian contener también el tipo de pact.

`model/interfaces`: ej: `IJugador` → define comportamientos comunes.

**Controlador**: Encargado de recibir solicitudes del frontend (desde Angular), ejecutar lógica de negocio y devolver resultados. Estructura del controller:

`Controller` : <br>
`PartidaController`, `TurnoController`, `JugadorController` → Sirven para manejar rutas REST como /game, /turn, etc.

`PingController` → Utilizado para verificar el estado del jugador.

`ChatController`, `PactoController` → Ayudan a gestionar los pacts entre jugadores en la game.

**Vista:** Está en el frontend, en el proyecto Angular con Typescript. Los componentes de Angular (.component.ts + .html) y servicios (.service.ts) forman la vista. Muestran la información al user, recogen sus interacciones y consumen las APIs REST del back (los controladores).

# **El patrón State**

¿Qué hace?

El patrón State es un patrón de diseño de comportamiento que permite que un objeto cambie su comportamiento cuando cambia su estado interno. El patrón permite que el objeto se comporte de manera diferente dependiendo de su estado

¿Qué soluciona?

Soluciona el problema de tener múltiples comportamientos en un objeto que cambian en función de su estado interno, encapsulando cada uno en una clase separada.

Si tuviéramos que usar “if” cada vez que quisiéramos saber el estado o acción del juego para verificar en qué fase se encuentra el jugador y de ahí ejecutar el comportamiento del método podríamos tener mucho código. Este patrón nos permite separar cada fase en su propia clase y cada una maneja solo lo que pueda pasar en ella. Además si en algún momento necesitamos añadir otra fase solo tenemos que crear otra clase y no tener que revisar todos los condicionales y actualizarlos manualmente, en lugar de eso agregas otro “estado” y creamos su clase correspondiente.

### **Ejemplo del estado de las fases del juego:**

Antes de state:

```
if (estadoJuego == EstadoJuego.Colocacion) {

    // Lógica para colocar tropas

} else if (estadoJuego == EstadoJuego.Ataque) {

    // Lógica para atacar

} else if (estadoJuego == EstadoJuego.Refuerzo) {
    // Lógica para reforzar tropas

}
```

Con state:

```
estadoJuego.faseColocacionTropas(juego);
estadoJuego.faseAtaque(juego);
estadoJuego.faseRefuerzoTropas(juego);
```

# **El patrón Observer**

¿Qué hace?

Es un patrón de diseño de **comportamiento** que permite definir un mecanismo de suscripción para notificar a varios objetos sobre cualquier evento que le suceda al objeto que están observando.

¿Qué soluciona?

El patrón Observer resuelve cómo hacer que múltiples objetos se mantengan sincronizados con un cambio, sin que el objeto que cambia tenga que conocer a todos los que dependen de él y sin que el sujeto tenga que conocer o gestionar los detalles de los observadores.

### **Ejemplo con el chat del juego, usando websockets:**

Sin observer:

Tendríamos que guardar una lista de los jugadores en game (con websockets y sesiones), luego iterar manualmente cada vez que alguien manda un message sobre los jugadores, enviar los messages uno a uno en el orden correcto, controlar que el websocket no se cierre abruptamente por una desconexión de un jugador o que el jugador tenga mala conexión.

Con observer:

**Sujeto:** El servidor del juego, que maneja los messages del chat de una game. <br>
**Observadores:** Los jugadores en game, que están observando los messages en el chat y el estado de otros jugadores.

Cuando un jugador envía un message de chat, el servidor (el sujeto) notifica a todos los jugadores (observadores) de que un nuevo message ha llegado. Los jugadores pueden actualizar su interfaz de chat automáticamente sin tener que pedir al servidor si hay nuevos messages.

También con observer se puede gestionar → notificaciones de conquista de continentes, alertas de tiempo de turn agotado, notificaciones de cualquier evento de estado (atacar, refuerzo, fin de turn), escuchar los eventos del user y registrar las acciones en el historial de game.

# **El patrón Strategy**

¿Qué hace?

Es un patrón de diseño de **comportamiento** que te permite definir una familia de algoritmos, colocar cada uno de ellos en una clase separada y hacer sus objetos intercambiables.

¿Qué soluciona?

Resuelve el problema de tener múltiples variantes de una misma operación, **evitando condicionales repetitivos** y facilitando el cambio dinámico de comportamiento. Cada algoritmo o estrategia se encapsula en su propia clase, lo que permite intercambiarlos sin modificar el código cliente. Por ejemplo, en un juego, en lugar de usar if para distinguir entre comportamientos de bots novatos, expertos o balanceados, cada uno implementa su propia estrategia en una clase distinta, y el programa decide en tiempo de ejecución cuál utilizar

Tanto el patrón strategy como el patrón state nos van a ayudar a evitar condicionales repetitivos, pero el objective de Strategy es encapsular distintos algoritmos o formas de hacer algo, que pueden cambiar dinámicamente por una elección externa o configuración (normalmente no cambia solo).

<br>

### **Ejemplo para la estrategia de bots:**

Según el enunciado tenemos varias estrategias de ataque:Un bot novato que actúa sin estrategia, eligiendo países y ataques al azar, mientras que uno experto toma decisiones evaluando riesgos, objectives y ventajas numéricas. En el medio, un bot balanceado aplica reglas simples pero lógicas, sin llegar a usar análisis complejos.

Se debería definir una interfaz EstrategiaBot con método decidirAtaque() e implementar clases concretas `EstrategiaBotNovato`, `EstrategiaBotBalanceado` , `EstrategiaBotExperto` . Un `BotService` recibe en tiempo de ejecución la estrategia adecuada (basada en la dificultad) y la usa para tomar decisiones. Esto facilita agregar nuevas estrategias sin cambiar el código cliente.

También con strategy se puede gestionar → asignación de objectives (Objetivos al azar, Objetivos balanceados por dificultad, )

Nota: en el patrón **Strategy**, lo típico es **definir una interfaz (o una clase abstracta)** que representa el comportamiento intercambiable.

# **El patrón Mediator:**

¿Qué hace?

Es un patrón de diseño de **comportamiento** que te permite reducir las dependencias caóticas entre objetos. El patrón restringe las comunicaciones directas entre los objetos, forzándolos a colaborar únicamente a través de un objeto mediador.

¿Qué soluciona?

Resuelve el problema del acoplamiento directo entre múltiples objetos que deben comunicarse, como jugadores, games o controladores de eventos. En lugar de que cada clase sepa cómo interactuar con las demás, un mediador central coordina la comunicación. Esto simplifica la lógica de interacción, facilita el mantenimiento y evita que el sistema se vuelva inmanejable cuando crecen las dependencias.

### **Ejemplo de mediador entre múltiples componentes:**

Deberíamos crear una interfaz `IGameMediator` actúa como intermediario entre controladores como `JugadorController`, `ObjetivoController`, `TurnoController`, `PactoController`, y servicios de lógica como `BatallaService` o `TurnoService`.

En lugar de que estos controladores y servicios interactúen entre sí directamente, cada uno notifica sus eventos relevantes al `IGameMediator`, que toma decisiones sobre la transición de estado del juego, la evaluación de objectives, o la ejecución de nuevas acciones según el contexto.

# **El patrón Memento**

¿Qué hace?

Es un patrón de diseño de **comportamiento** que te permite guardar y restaurar el estado previo de un objeto sin revelar los detalles de su implementación.

¿Qué soluciona?

Puede surgir la necesidad de revisar el historial o simplemente guardar la game y retomarla más tarde. Sin embargo, intentar guardar ese estado desde fuera del objeto suele generar problemas. Por un lado, se compromete el encapsulamiento al acceder directamente a atributos internos que deberían mantenerse ocultos. El patrón de diseño Memento surge como una solución a este problema, al permitir almacenar y restaurar el estado interno de un objeto sin violar su encapsulamiento ni comprometer su integridad.

### **Ejemplo en el caso de guardar el estado de una game online:**

Durante una game online, la clase `Partida` (`./models/entities`) concentra todo el estado del juego: Cual jugador controla qué country tiene en la game, cuántos ejércitos hay en cada uno, en qué fase turn está, qué tarjetas de paises tiene cada jugador, el turn actual y los pacts en curso. Esta clase debe poder generar un snapshot fiel de sí misma sin que el resto del sistema acceda directamente a su estado interno.

Para lograrlo sin romper el principio de encapsulamiento, `Partida` genera una instancia de `EstadoPartida` (`./models/entities`) que funciona como un **Memento**. Esta clase captura el estado actual de todos los elementos necesarios para guardar o restaurar el progreso de la game.

El componente que gestiona el historial `HistorialService` actúa como el **Caretaker** del patrón Memento. Este servicio se encarga de almacenar los objetos `EstadoPartida` ya sea en memoria temporal o persistente.

Cada vez que un jugador finaliza su turn (mediante una llamada a `Turno- controller`), el sistema le solicita a `Partida` que genere su `EstadoPartida` actual. Ese estado se guarda para poder restaurarlo más tarde si la game se interrumpe, si un jugador se desconecta (detectado por `Ping-controller`).

Cuando un jugador desea continuar una game pausada el sistema busca el último `EstadoPartida` asociado a esa `Partida` en la base de datos. Ese `EstadoPartida`, que actúa como **memento**, contiene una copia fiel del estado de juego en ese momento: turns, posiciones, ejércitos, objectives, etc. Así, el juego se reanuda exactamente donde se dejó, con toda la lógica encapsulada y sin inconsistencias, respetando el patrón Memento.

Caretaker → El Caretaker (cuidador) es el componente responsable de guardar, recuperar y administrar los mementos, pero **sin conocer su contenido interno.**

**Con memento también podemos →** Gestionar el tiempo por jugador con consecuencias automáticas y registrar una snapshot de ese estado (por ej: Asignar automáticamente tropas si el user no elige).