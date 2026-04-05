package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.AnalizeObjective;
import ar.edu.utn.frc.tup.piii.util.CombatUtil;
import ar.edu.utn.frc.tup.piii.util.ProcessedObjective;
import lombok.Data;

import java.util.*;

/**
 *  Representa un jugador bot con comportamiento experto
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see BotPlayer
 */
@Data
public class BotExpertStrategy implements IBehaviorBot {
    private static final int MINIMUM_TROOPS_ATTACK = 2;
    private static final int SUPERIORITY_FACTOR = 3;
    private static final int MAX_TROOPS_REGROUP = 3;
    private static final int PRIORITY_KEY_TERRITORY = 2;
    private static final int PRIORITY_VULNERABLE_TERRITORY = 1;

    private boolean conqueredThisTurn = false;
    private List<CountryGame> countriesBot;
    private CountryGameService countryGameService;
    private GameService gameService;
    private CardExchangeService cardExchangeService;
    private CardService cardService;
    private ContinentService continentService;
    private TurnService turnService;
    private PlayerService playerService;
    private CombatService combatService;


    /**
     * Ejecuta el turno completo del bot experto.
     * El turno se divide en tres fases:
     *   Fase de Refuerzos: Reparte los ejércitos disponibles reforzando puntos clave y defensivos.
     *   Fase de Ataque: Evalúa y ejecuta ataques estratégicos si son favorables para cumplir el objetivo o bloquear oponentes.
     *   Fase de Reagrupación: Redistribuye tropas para fortalecer la defensa y planear futuros movimientos.
     *
     * @param player el player bot que realiza el turno.
     * @param game la partida en curso en la que se ejecuta el turno.
     */
    @Override
    public void playTurn(PlayerGame player, Game game) {
        this.countriesBot = player.getCountries();

        String state = game.getStates().getDescription();

        // 1. PRIMERA RONDA
        if (state.equalsIgnoreCase(StateGameEnum.FIRST_ROUND.name())) {
            putInitialArmies(player, 5);
            return;
        }

        // 2. SEGUNDA RONDA
        if (state.equalsIgnoreCase(StateGameEnum.SECOND_ROUND.name())) {
            putInitialArmies(player, 3);
            return;
        }

        // 3. HOSTILIDADES
        if (state.equalsIgnoreCase(StateGameEnum.HOSTILITIES.name())) {
            // Canje de tarjetas si tiene 5 o más
            if (player.getCards().size() >= 5 && cardExchangeService.canExchange(player.getId())) {
                cardExchangeService.doExchange(player.getId(), game.getId());
            }

            distributeArmies(player, game);
            attack(player, game);

            // Solicitar tarjeta si conquistó al menos un país
            if (conqueredThisTurn) {
                CardCountry card = cardService.getAvailableCard();
                if (card != null) {
                    cardService.assignCardToPlayer(card.getCountry().getId(), player.getId());
                }
            }
            regroup(player, game);

            if (countryGameService.checkVictory(player, game)) {
                gameService.endGame(game.getId());
            }
        }
    }

    private void putInitialArmies(PlayerGame player, int amount) {
        List<CountryGame> countries = player.getCountries();
        if (countries == null || countries.isEmpty()) return;

        Random random = new Random();
        int total = amount;

        while (total > 0) {
            CountryGame country = countries.get(random.nextInt(countries.size()));
            countryGameService.increaseArmies(country.getId(), 1);
            total--;
        }
    }

    /**
     * Fase de refuerzo del bot experto.
     * Reparte los ejércitos disponibles considerando criterios estratégicos:
     *   Refuerza territorios clave que sean esenciales para cumplir su misión.
     *   No descuida la defensa de fronteras vulnerables o de alta amenaza.
     *   Evalúa el progreso de otros jugadores: si alguno está cerca de ganar, coloca tropas para bloquear su avance.
     *   Distribuye los refuerzos de forma balanceada entre ofensiva y defensiva, evitando concentraciones ineficientes.
     * Esta fase es crítica para preparar ataques, consolidar control territorial y prevenir derrotas por descuido defensivo.
     *
     * @param player el player bot que realiza la fase de refuerzo.
     * @param game la partida en curso donde se está jugando.
     */
    @Override
    public void distributeArmies(PlayerGame player, Game game) {
        ProcessedObjective processedObjective = AnalizeObjective.analizeObjective(player.getSecretObjective());

        List<CountryGame> keyTerritories = identifyKeyTerritories(processedObjective, player, game);
        List<CountryGame> vulnerables = detectVulnerableBorders(player, game);
        Map<CountryGame, Integer> priorities = calculatePriorities(keyTerritories, vulnerables);

        int availableArmies = player.getPlayer().getAvailableArmies();
        distributeArmies(availableArmies, priorities);
    }

    /**
     * Distribuye los ejércitos disponibles entre los países según sus priorities.
     * Validar entradas, ordena los países por prioridad, asigna ejércitos proporcionalmente,
     * distribuye los ejércitos restantes y aplica la asignación.
     *
     * @param availableArmies la cantidad total de ejércitos para repartir
     * @param priorities mapa que asocia cada país con su prioridad (mayor prioridad = más ejércitos)
     */
    private void distributeArmies(int availableArmies, Map<CountryGame, Integer> priorities) {
        if (availableArmies <= 0 || priorities == null || priorities.isEmpty()) return;

        List<CountryGame> ordered = orderByDescendentPriority(priorities);
        Map<CountryGame, Integer> assignation = assignProportionalArmies(availableArmies, priorities);
        assignation = distributeRemainingArmies(availableArmies, assignation, ordered);
        applyAssignation(assignation);
    }

    /**
     * Ordena los países por prioridad en orden descendente.
     *
     * @param priorities mapa que asocia cada país con su prioridad
     * @return lista de países ordenados de mayor a menor prioridad
     */
    private List<CountryGame> orderByDescendentPriority(Map<CountryGame, Integer> priorities) {
        return priorities.entrySet().stream()
                .sorted(Map.Entry.<CountryGame, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Asigna ejércitos proporcionalmente a cada país según su prioridad.
     * El cálculo redondea hacia abajo la asignación proporcional.
     * Además, calcula los ejércitos que quedan sin asignar (por redondeos).
     *
     * @param availableArmies cantidad total de ejércitos a repartir
     * @param priorities mapa de países con sus priorities
     * @return mapa con la asignación inicial de ejércitos por país,
     *         y una entrada temporal con clave null para los ejércitos restantes
     */
    private Map<CountryGame, Integer> assignProportionalArmies(int availableArmies, Map<CountryGame, Integer> priorities) {
        Map<CountryGame, Integer> assignation = new HashMap<>();
        int totalPriority = priorities.values().stream().mapToInt(Integer::intValue).sum();
        int sumAssigned = 0;

        for (Map.Entry<CountryGame, Integer> entry : priorities.entrySet()) {
            int assigned = (int) Math.floor((double) entry.getValue() / totalPriority * availableArmies);
            assignation.put(entry.getKey(), assigned);
            sumAssigned += assigned;
        }
        // Guardamos la cantidad asignada para distribuir restos después
        assignation.put(null, availableArmies - sumAssigned); // Temporal: el null guarda los restos
        return assignation;
    }

    /**
     * Distribuye los ejércitos restantes de forma equitativa comenzando por los países de mayor prioridad.
     *
     * @param availableArmies cantidad total de ejércitos originalmente disponibles
     * @param assignation mapa con asignación inicial de ejércitos, que incluye la clave temporal null para restos
     * @param ordered lista de países ordered por prioridad descendente
     * @return mapa actualizado con la asignación final de ejércitos por país (sin la clave temporal)
     */

    private Map<CountryGame, Integer> distributeRemainingArmies(int availableArmies, Map<CountryGame, Integer> assignation, List<CountryGame> ordered) {
        int remainingArmies = assignation.remove(null);
        int i = 0;
        while (remainingArmies > 0) {
            CountryGame country = ordered.get(i % ordered.size());
            assignation.put(country, assignation.get(country) + 1);
            remainingArmies--;
            i++;
        }
        return assignation;
    }

    /**
     * Aplica la asignación de ejércitos a cada país utilizando el servicio correspondiente.
     *
     * @param assignation mapa con la cantidad de ejércitos a asignar a cada país
     */
    public void applyAssignation(Map<CountryGame, Integer> assignation) {
        assignation.forEach((country, troops) -> {
            countryGameService.increaseArmies(country.getId(), troops);
        });
    }


    /**
     * Calcula un puntaje de prioridad para cada país controlado por el bot, considerando su valor estratégico.
     * Se asigna un valor entero a cada {@link CountryGame} del bot, en función de si es un territorio clave
     * (objetivo estratégico), si está en peligro por amenazas externas, o ambos.
     * Este puntaje puede luego utilizarse para tomar decisiones sobre refuerzos, ataques o movimientos defensivos.
     * Reglas sugeridas (pueden adaptarse):
     * - +2 si el país está en la lista de territorios clave
     * - +1 si el país está amenazado por enemigos activos limítrofes
     * - 0 si no cumple ninguna de las condiciones anteriores
     *
     * @param keyTerritories lista de países importantes a controlar (objetivos)
     * @param externalThreats lista de países enemigos que representan amenazas
     * @return un {@link Map} que asocia cada país propio con su prioridad calculada
     */
    public Map<CountryGame, Integer> calculatePriorities(List<CountryGame> keyTerritories, List<CountryGame> externalThreats) {
        Map<CountryGame, Integer> priorities = new HashMap<>();
        for(CountryGame country : countriesBot){
            int priority = 0;

            if(keyTerritories.contains(country)) priority += PRIORITY_KEY_TERRITORY;
            if(externalThreats.contains(country)) priority += PRIORITY_VULNERABLE_TERRITORY;

            priorities.put(country,priority);
        }
        return priorities;
    }

    /**
     * Identifica los territorios clave a conquistar o reforzar en función del objetivo secreto del player.
     * Este metodo analiza {@link ProcessedObjective} y determina los países estratégicos a considerar durante
     * la fase de refuerzo o planificación de ataque del bot experto.
     * La estrategia depende del tipo de objetivo:
     *     CONTINENTE_Y_PAISES: devuelve países pertenecientes a los continentes a conquistar,
     *         países necesarios dentro de ciertos continentes, y países individuales clave (si aplica).
     *     COLOR_EJERCITO: devuelve los países del player objetivo que debe ser eliminado.
     *     DESCONOCIDO: como estrategia por defecto, devuelve territorios fronterizos vulnerables
     *         o adyacentes a enemigos.
     *
     * @param processedObjective la representación estructurada del objetivo secreto del player
     * @param player el {@link PlayerGame} bot que ejecuta la estrategia
     * @param game la partida actual que contiene la información global del juego
     * @return una lista de {@link CountryGame} considerados prioritarios para cumplir el objetivo
     */
    public List<CountryGame> identifyKeyTerritories(ProcessedObjective processedObjective, PlayerGame player, Game game) {
        switch (processedObjective.getType()) {
            case CONTINENT_AND_COUNTRIES -> {
                List<CountryGame> keys = new ArrayList<>();
                keys.addAll(countriesOfContinent(processedObjective.getCountriesPerContinent()));
                keys.addAll(countriesPerAmount(processedObjective.getCountriesPerContinent()));
                keys.addAll(remainingCountries(processedObjective.getSingleCountries(), player));
                return keys;
            }
            case ARMY_COLOR -> {
                PlayerGame objective = searchPlayerByColor(processedObjective.getObjectiveColor(), game);
                return objective != null ? objective.getCountries() : List.of();
            }
            case UNKNOWN -> {
                return detectVulnerableBorders(player, game);
            }
            default -> {
                return List.of();
            }
        }
    }

    /**
     * Detecta los países del bot que están en frontera con países enemigos activos.
     * Recorre todos los países del bot y verifica si algún país vecino está controlado
     * por otro player (enemigo) que todavía está activo en la partida.
     * Si es así, considera ese país del bot como vulnerable.
     *
     * @param player el {@link PlayerGame} bot que analiza su territorio
     * @param game la partida actual
     * @return lista de países fronterizos del bot en riesgo de ser atacados
     */
    public List<CountryGame> detectVulnerableBorders(PlayerGame player, Game game) {
        List<CountryGame> allCountries = countryGameService.findByGame(game);
        List<CountryGame> vulnerables = new ArrayList<>();

        for(CountryGame country : countriesBot){
            List<CountryGame> borderingEnemies = getBorderingEnemies(country, allCountries,player);
            if(!borderingEnemies.isEmpty()){
                vulnerables.add(country);
            }
        }
        return vulnerables;
    }

    /**
     * Busca y devuelve el jugador de la partida que tenga asignado el color indicado.
     * Este metodo se utiliza en objetivos que requieren eliminar a un jugador específico
     * identificado por su color.
     *
     * @param colorObjective el color del jugador que debe ser eliminado (según el objetivo secreto)
     * @param game la {@link Game} actual donde se debe buscar al jugador
     * @return el {@link PlayerGame} cuyo color coincide con el proporcionado, o null si no se encuentra
     */
    public PlayerGame searchPlayerByColor(String colorObjective, Game game) {
        List<CountryGame> countries = countryGameService.findByGame(game);

        for(CountryGame country:countries){
            PlayerGame player = country.getPlayerGame();
            if(player.isActive() && player.getColor().getName().equalsIgnoreCase(colorObjective)){
                return player;
            }
        }
        return null;
    }

    /**
     * Filtra los países "sueltos" (individuales) especificados en el objetivo secreto,
     * que pertenecen al player bot en esta partida.
     * Estos países suelen ser requeridos de forma puntual y deben ser tenidos en cuenta
     * durante la planificación del bot experto.
     *
     * @param remainingCountries lista de nombres de países requeridos individualmente según el objetivo
     * @param player el {@link PlayerGame} actual (bot) evaluando su estrategia
     * @return una colección de {@link CountryGame} correspondientes a los países requeridos que el player posee
     */
    public Collection<? extends CountryGame> remainingCountries(List<String> remainingCountries, PlayerGame player) {
        return countriesBot.stream()
                .filter(p -> remainingCountries.contains(p.getCountry().getName()))
                .toList();
    }


    /**
     * Prioriza los países del bot que pertenecen a los continentes involucrados en el objetivo.
     * Ordena los países según:
     * 1. La cantidad de enemigos en territorios limítrofes.
     * 2. La cantidad de tropas disponibles.
     * Esto ayuda al bot a decidir en qué territorios debe concentrar sus refuerzos,
     * privilegiando zonas en disputa o con alto valor estratégico dentro de los continentes objetivos.
     *
     * @param countriesPerContinent mapa con los continentes relevantes y la cantidad de países requeridos en cada uno.
     * @return lista de {@link CountryGame} del bot ordenado estratégicamente para cumplir con el objetivo.
     */
    public Collection<? extends CountryGame> countriesPerAmount(Map<String, Integer> countriesPerContinent) {
        if(countriesBot == null || countriesBot.isEmpty()) return List.of();

        List<CountryGame> countries = countryGameService.findAll();

        List<CountryGame> orderedCountries = countriesBot.stream()
                .sorted(Comparator
                        .comparingInt((CountryGame p) -> amountBorderingEnemies(p, countries))
                        .reversed() //+ enemigos limitrofes primero
                        .thenComparing(CountryGame::getAmountArmies).reversed() // + tropas
                ).toList();

        return filterPerContinentAndLimit(orderedCountries, countriesPerContinent);
    }
    /**
     * Filtra una lista de países, seleccionando una cantidad específica por continente.
     *
     * @param countries la lista de países ya filtrada u ordenada
     * @param countriesPerContinent mapa con continentes y la cantidad de países a seleccionar por cada uno
     * @return lista de países seleccionados según el mapa
     */
    public List<CountryGame> filterPerContinentAndLimit(List<CountryGame> countries, Map<String, Integer> countriesPerContinent) {
        List<CountryGame> selected = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : countriesPerContinent.entrySet()) {
            String continent = entry.getKey();
            int amount = entry.getValue();

            List<CountryGame> fromContinent = countries.stream()
                    .filter(p -> p.getCountry().getContinent().getName().equals(continent))
                    .limit(amount)
                    .toList();

            selected.addAll(fromContinent);
        }
        return selected;
    }

    /**
     * Calcula cuántos países limítrofes al país dado están controlados por jugadores enemigos.
     * Este metodo analiza allCountries los países vecinos del país actual, y cuenta cuántos de ellos no
     * pertenecen al mismo jugador. Sirve para calcular el nivel de amenaza externa y priorizar
     * defensa o ataque.
     *
     * @param country el país del bot que se analiza
     * @param allCountries los países de la partida (incluidos los del bot y los enemigos)
     * @return la cantidad de países limítrofes controlados por enemigos
     */
    public int amountBorderingEnemies(CountryGame country, List<CountryGame> allCountries) {
        int counter = 0;
        for(CountryGame p : allCountries) {
            if(p.getPlayerGame().getPlayer().getId() != country.getPlayerGame().getPlayer().getId()
            && countryGameService.isBordering(country.getCountry().getId(), p.getCountry().getId())) {
                counter++;
            }
        }
        return counter;
    }

    /**
     * Retorna una colección de países que el jugador controla y que pertenecen
     * a los continentes indicados en el mapa de requisitos del objetivo.
     * Cada entrada del mapa especifica un continente y la cantidad de países
     * que se deben conquistar en ese continente. Este metodo selecciona hasta
     * esa cantidad de países (si están disponibles) que ya pertenecen al jugador
     * y que pueden ser considerados para cumplir parcialmente con ese objetivo.
     *
     * @param countriesPerContinent un mapa donde la clave es el nombre del continente
     *                            y el valor es la cantidad de países requeridos en ese continente.
     * @return una colección de {@link CountryGame} que pertenecen al jugador
     *         y que están en los continentes requeridos.
     */
    public Collection<? extends CountryGame> countriesOfContinent(Map<String, Integer> countriesPerContinent) {
        return filterPerContinentAndLimit(countriesBot, countriesPerContinent);
    }

    /**
     * Ejecuta la fase de ataque del bot experto utilizando una estrategia basada en caminos mínimos (Dijkstra).
     * El bot evalúa todos los países que forman parte de su objetivo secreto y utiliza el algoritmo de Dijkstra
     * para encontrar el camino más corto desde sus territorios hacia los países enemigos objetivo.
     * Si encuentra una ruta viable donde tenga superioridad militar (más del doble de tropas enemigas en el camino),
     * ataca cada país en el recorrido de manera secuencial. Si no encuentra rutas viables, ejecuta una lógica
     * tradicional de ataque basada en vecinos.
     * Reglas que aplica:
     * - Solo considera rutas donde tenga más del triple de tropas que la suma de enemigos en el camino.
     * - Evita atacar si no tiene superioridad numérica.
     * - Ataca solo un camino por turno para limitar la agresividad.
     *
     * @param player El bot que ejecuta la fase de ataque.
     * @param game La partida actual del juego.
     */
    @Override
    public void attack(PlayerGame player, Game game) {
        ProcessedObjective objective = AnalizeObjective.analizeObjective(player.getSecretObjective());
        countriesBot = player.getCountries();

        List<CountryGame> allCountries = countryGameService.findByGame(game);
        List<CountryGame> objectiveCountries = obtenerPaisesObjetivo(player, objective,allCountries);

        if(executeStrategicAttack(player, objectiveCountries, allCountries)) return;

        executeTraditionalAttack(player, allCountries);
    }

    /**
     * Verifica si un país pertenece a un player enemigo
     */
    public boolean isEnemy(CountryGame country, PlayerGame player){
        return country.getPlayerGame().getPlayer().getId() != player.getId();
    }

    /**
     * Verifica si un país tiene tropas suficientes para atacar
     */
    public boolean hasTroopsToAttack(CountryGame country){
        return country.getAmountArmies() >= MINIMUM_TROOPS_ATTACK;
    }
    /**
     * Metodo centralizado para obtener países limitrofes enemigos
     */
    public List<CountryGame> getBorderingEnemies(CountryGame country, List<CountryGame> allCountries, PlayerGame player) {
        List<CountryGame> enemiesBordering = new ArrayList<>();

        for(CountryGame p : allCountries){
            if(isEnemy(p,player) &&
                    countryGameService.isBordering(country.getCountry().getId(), p.getCountry().getId()) &&
                    p.getPlayerGame().isActive()){
                enemiesBordering.add(p);
            }
        }
        return enemiesBordering;
    }

    /**
     * Ejecuta ataque tradicional basado en vecinos
     */
    public void executeTraditionalAttack(PlayerGame player, List<CountryGame> allCountries) {
        for (CountryGame attacker : countriesBot) {
            if (!hasTroopsToAttack(attacker)) continue;

            CountryGame[] neighbors = countryGameService.getBorder(attacker, allCountries);

            for (CountryGame neighbor : neighbors) {
                if (isViableAttack(attacker, neighbor, player)) {
                    combatService.announceAttack(attacker.getId(), neighbor.getId());
                    boolean conquered = CombatUtil.resolveCombat(attacker, neighbor, player, gameService);
                    if (conquered) conqueredThisTurn = true;
                    return;
                }
            }
        }
    }

    /**
     * Verifica si un ataque directo es viable
     */
    public boolean isViableAttack(CountryGame attacker, CountryGame neighbor, PlayerGame player) {
        return isEnemy(neighbor, player) &&
                attacker.getAmountArmies() >= neighbor.getAmountArmies() * SUPERIORITY_FACTOR;
    }

    /**
     * Ejecuta ataque estratégico usando Dijkstra
     */
    public boolean executeStrategicAttack(PlayerGame player, List<CountryGame> objectiveCountries, List<CountryGame> allCountries) {
        List<CountryGame> viableAttackers = getViableAttackers();

        for (CountryGame objectiveCountry : objectiveCountries) {
            for (CountryGame origin : viableAttackers) {
                List<CountryGame> path = DijkstraFindPath.findShorterPath(
                        origin, objectiveCountry, allCountries, countryGameService);

                if (isViableRoute(path, origin, player)) {
                    executeAttackPerPath(path, origin, player);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Ejecuta ataque siguiendo un path específico
     */
    public void executeAttackPerPath(List<CountryGame> path, CountryGame origin, PlayerGame player) {
        CountryGame current = origin;

        for (CountryGame next : path) {
            if (isEnemy(next, player)) {
                combatService.announceAttack(current.getId(), next.getId());
                boolean conquered = CombatUtil.resolveCombat(current, next,player, gameService);
                if(conquered) conqueredThisTurn = true;
                current = next;
            } else {
                current = next;
            }
        }
    }

    /**
     * Verifica si una ruta es viable para atacar
     */
    public boolean isViableRoute(List<CountryGame> path, CountryGame origin, PlayerGame player) {
        if (path.isEmpty() || path.size() == 1) return false;

        int totalEnemies = 0;
        for (CountryGame country : path) {
            if (isEnemy(country, player)) {
                totalEnemies += country.getAmountArmies();
            }
        }
        return origin.getAmountArmies() > totalEnemies * SUPERIORITY_FACTOR;
    }

    /**
     * Obtiene países del bot que pueden atacar
     */
    public List<CountryGame> getViableAttackers() {
        List<CountryGame> attackers = new ArrayList<>();

        for (CountryGame country : countriesBot) {
            if (hasTroopsToAttack(country)) {
                attackers.add(country);
            }
        }

        return attackers;
    }

    /**
     * Obtiene países objetivos según la estrategia
     */
    public List<CountryGame> obtenerPaisesObjetivo(PlayerGame player, ProcessedObjective objective, List<CountryGame> allCountries) {
       List<CountryGame> keyTerritories = identifyKeyTerritories(objective,player,null);
       List<CountryGame> ObjectiveCountries = new ArrayList<>();

       if(!keyTerritories.isEmpty()){
           for(CountryGame p : allCountries){
               if(isEnemy(p,player)){
                   ObjectiveCountries.add(p);
               }
           }
       }
       return ObjectiveCountries;
    }


    /**
     * Ejecuta la fase de reagrupamiento del bot experto.
     * Estrategia:
     * - Busca países del bot con tropas excedentes (más de 1).
     * - Identifica países propios en frontera con enemigos como destinos estratégicos.
     * - Utiliza el algoritmo de Dijkstra para encontrar caminos entre países propios.
     * - Mueve tropas por un único camino hacia un destino clave, si hay un camino posible.
     * Reglas:
     * - Solo realiza un movimiento por turno.
     * - Deja al menos 1 tropa en el país de origen.
     * - Usa un máximo de 3 tropas por movimiento.
     *
     * @param player El bot que ejecuta su turno.
     * @param game La partida actual del juego.
     */
    @Override
    public void regroup(PlayerGame player, Game game) {
        List<CountryGame> origins = getTroopsWithExcessCountries(countriesBot);
        List<CountryGame> destinations = getCountriesInFrontier(player, game);

        executeFirstViableMovement(origins, destinations);
    }

    /**
     * Ejecuta el primer movimiento viable de reagrupamiento
     */
    public void executeFirstViableMovement(List<CountryGame> origins, List<CountryGame> destinations) {
        for (CountryGame origin : origins) {
            for (CountryGame destination : destinations) {
                if (origin.equals(destination)) continue;

                List<CountryGame> path = DijkstraFindPath.findShorterPath(
                        origin, destination, countriesBot, countryGameService);

                if (!path.isEmpty()) {
                    int availableTroops = origin.getAmountArmies() - 1;
                    int troopsToMove = Math.min(availableTroops, MAX_TROOPS_REGROUP);

                    if (troopsToMove > 0) {
                        turnService.moveArmies(origin.getId(),
                                destination.getId(), troopsToMove);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Filtra los países propios que están en frontera, es decir, tienen vecinos enemigos.
     *
     * @param player El player bot actual.
     * @param game La partida actual.
     * @return Lista de países en frontera que podrían necesitar refuerzo.
     */
    public List<CountryGame> getCountriesInFrontier(PlayerGame player, Game game) {
        List<CountryGame> frontier = new ArrayList<>();

        for (CountryGame country : countriesBot) {
            List<CountryGame> enemyNeighbors = countryGameService.findEnemyNeighbors(country.getCountry().getId(), player, game);
            if (!enemyNeighbors.isEmpty()) {
                frontier.add(country);
            }
        }
        return frontier;
    }

    /**
     * Obtiene los países que tienen tropas excedentes disponibles para mover.
     * Se considera excedente si el país tiene más de 1 ejército.
     *
     * @param countries Lista de países del jugador.
     * @return Lista de países con tropas disponibles para reagrupamiento.
     */
    public List<CountryGame> getTroopsWithExcessCountries(List<CountryGame> countries) {
        return countries.stream()
                .filter(p ->p.getAmountArmies() > 1)
                .toList();
    }

}
