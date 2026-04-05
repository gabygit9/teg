package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.AnalizeObjective;
import ar.edu.utn.frc.tup.piii.util.AttackOption;
import ar.edu.utn.frc.tup.piii.util.CombatUtil;
import ar.edu.utn.frc.tup.piii.util.ProcessedObjective;
import lombok.Data;

import java.util.*;

/**
 *  Representa un jugador bot con comportamiento balanceado:
 *  evalúa probabilidades antes de atacar y reparte tropas estratégicamente.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see BotPlayer
 */
@Data
public class BotBalancedStrategy implements IBehaviorBot {

    private final PlayerService playerService;
    private List<CountryGame> countriesBot;
    private boolean conquerThisTurn = false;

    private CountryGameService countryGameService;
    private GameService gameService;
    private CardService cardService;
    private CardExchangeService exchangeService;
    private ContinentService continentService;
    private StateGameEnum stateGameEnum;
    private CombatService combatService;

    /**
     * Ejecuta el turno completo del bot, en el orden oficial de fases del juego:
     * 1. Refuerzo (reparto de ejércitos disponibles).
     * 2. Ataque (si encuentra un país enemigo vulnerable vecino).
     * 3. Reagrupamiento (movimiento mínimo entre países propios adyacentes).
     *
     * @param player El player bot actual.
     * @param game La partida en curso.
     */
    @Override
    public void playTurn(PlayerGame player, Game game) {
        this.countriesBot = player.getCountries();
        this.conquerThisTurn = false;

        String state = game.getStates().getDescription();

        // 1. PRIMERA RONDA: colocar 5 ejércitos
        if (state.equalsIgnoreCase(stateGameEnum.FIRST_ROUND.name())) {
            putInitialArmies(player, 5);
            return;
        }

        // 2. SEGUNDA RONDA: colocar 3 ejércitos
        if (state.equalsIgnoreCase(stateGameEnum.SECOND_ROUND.name())) {
            putInitialArmies(player, 3);
            return;
        }

        // 3. HOSTILIDADES
        if (state.equalsIgnoreCase(stateGameEnum.HOSTILITIES.name())) {
            // a. Canjear tarjetas si tiene 5 o más
            if (player.getCards().size() >= 5 && exchangeService.canExchange(player.getId())) {
                exchangeService.doExchange(player.getId(), game.getId());
            }

            distributeArmies(player, game);
            attack(player, game);

            //Solicitar tarjeta si conquistó al menos un país
            if (conquerThisTurn) {
                CardCountry card = cardService.getAvailableCard();
                if (card != null) {
                    cardService.assignCardToPlayer(card.getCountry().getId(), player.getId());
                }
            }

            regroup(player, game);
        }
    }

    public void putInitialArmies(PlayerGame player, int amount) {
        List<CountryGame> countries = player.getCountries();
        if (countries == null || countries.isEmpty()) return;

        Random random = new Random();
        int total = amount;

        while (total > 0) {
            CountryGame country = countries.get(random.nextInt(countries.size()));
            countryGameService.increaseArmies(country.getId(), 1);
            total--;
        }
        playerService.persistConcretPlayer(player.getPlayer());
        System.out.println("EL BOT TERMINO DE COLOCAR EJERCITOS INICIALES");
    }

    /**
     * Ejecuta la fase de refuerzos para el bot balanceado.
     * Este metodo distribuye los ejércitos disponibles del player de forma estratégica,
     * siguiendo los siguientes criterios:
     * 1. Se identifican primero los territorios más vulnerables (fronterizos con enemigos),
     *    y entre ellos se priorizan aquellos con menor cantidad de tropas.
     * 2. Si hay ejércitos disponibles luego de reforzar las fronteras,
     *    se asignan a territorios clave relacionados con el objetivo secreto del player
     *    (por ejemplo, países de continentes objetivo o incluidos en condiciones especiales).
     * 3. Si aún hay tropas disponibles y no hay territorios prioritarios, se reparten
     *    equitativamente entre los países propios, intentando no dejar puntos débiles.
     * Esta fase busca lograr un equilibrio entre defensa y avance hacia el objetivo,
     * sin priorizarlo al 100%, manteniendo una lógica balanceada de riesgo-beneficio.
     *
     * @param player El player bot que está ejecutando su fase de refuerzo.
     * @param game La partida actual en la que se encuentra el player.
     *
     * @see PlayerGame
     * @see ProcessedObjective
     * @see CountryGame
     */
    @Override
    public void distributeArmies(PlayerGame player, Game game) {
        List<CountryGame> enemiesCountries = countryGameService.findByGame(game)
                .stream()
                .filter(p -> p.getPlayerGame().getId() != player.getId())
                .toList();

        List<CountryGame> borderingCountries = filterBorderingCountries(countriesBot, enemiesCountries);

        if(borderingCountries.isEmpty())return;

        ProcessedObjective processedObjective = AnalizeObjective.analizeObjective(player.getSecretObjective());

        List<CountryGame> objetiveCountries = priorObjectivesCountries(borderingCountries, processedObjective, player);

        distributeAvailableTroops(borderingCountries, objetiveCountries, countriesBot, player);

        player.getPlayer().setAvailableArmies(0);
    }

    /**
     * Reparte los ejércitos disponibles del bot balanceado en tres fases de prioridad:
     * 1. Primero refuerza los países que forman parte del objetivo secreto.
     * 2. Luego refuerza los países borderingCountries que están expuestos a enemigos.
     * 3. Finalmente, reparte cualquier ejército restante en los países restantes del bot.
     * Cada grupo se ordena ascendentemente por cantidad de ejércitos antes de repartir,
     * de modo que los países más débiles reciban refuerzos primero.
     *
     * @param borderingCountries Países propios limítrofes con enemigos.
     * @param ObjectiveCountries Países relevantes para cumplir el objetivo secreto.
     * @param countriesBot Todos los países controlados por el bot.
     * @param player Instancia del player bot.
     */

    public void distributeAvailableTroops(List<CountryGame> borderingCountries, List<CountryGame> ObjectiveCountries, List<CountryGame> countriesBot, PlayerGame player) {
        int armies = player.getPlayer().getAvailableArmies();

        //Repartir en paises del objetivo
        armies = distributeInPriority(ObjectiveCountries,armies);
        //repartir en paises borderingCountries
        armies = distributeInPriority(borderingCountries,armies);
        //repartir en paises del bot
        distributeInPriority(countriesBot,armies);

        player.getPlayer().setAvailableArmies(0);
    }

    /**
     * Reparte de forma equitativa los ejércitos disponibles sobre un conjunto de países
     * ordenado por menor cantidad de tropas primero. Este metodo busca reforzar
     * estratégicamente los países más débiles.
     * El reparto se realiza de a un ejército por país en cada ciclo, hasta que
     * se agoten los ejércitos disponibles o no haya países a reforzar.
     *
     * @param countries Lista de países a reforzar, idealmente ordenada por prioridad.
     * @param armies Cantidad total de ejércitos que pueden ser asignados.
     * @return Cantidad restante de ejércitos sin asignar (puede ser 0).
     */

    public int distributeInPriority(List<CountryGame> countries, int armies) {
        //ordenar de forma ascendente
        countries.sort(Comparator.comparingInt(CountryGame::getAmountArmies));

        for(CountryGame country : countries){
            if(armies == 0) break;
            country.setAmountArmies(country.getAmountArmies()+1);
            armies--;
        }
        return armies;
    }

    /**
     * Filtra los países borderingCountries del player que son relevantes para su objetivo secreto.
     * Un país se considera parte del objetivo si:
     *   Pertenece a un continente requerido.
     *   Es uno de los países sueltos definidos en el objetivo.
     *   Contribuye al objetivo especial de tener 3 países limítrofes entre sí.
     *   El objetivo es destruir un color, y el país pertenece a ese player.
     *
     *
     * @param borderingCountries Lista de países borderingCountries del player bot.
     * @param processedObjective Estructura que representa el objetivo secreto ya analizado.
     * @param player El player bot actual.
     * @return Lista de países borderingCountries estratégicos para el objetivo.
     */
    public List<CountryGame> priorObjectivesCountries(List<CountryGame> borderingCountries, ProcessedObjective processedObjective, PlayerGame player) {
        return borderingCountries.stream().filter(p-> isPartOfObjective(p, processedObjective, player)).toList();
    }


    /**
     * Filtra y retorna los países del bot que son fronterizos con al menos un país enemigo.
     * Un país se considera fronterizo si tiene al menos un país enemigo adyacente
     * según la lógica del juego (es limítrofe).
     *
     * @param countriesBot Lista de países controlados por el bot.
     * @param enemyCountries Lista de países controlados por los enemigos en la partida.
     * @return Lista de países propios que están en la frontera (adyacentes a enemigos).
     */
    public List<CountryGame> filterBorderingCountries(List<CountryGame> countriesBot, List<CountryGame> enemyCountries) {
        List<CountryGame> borderingCountries = new ArrayList<>();
        for(CountryGame countryBot : countriesBot){
            for(CountryGame enemyCountry : enemyCountries){
                if(countryGameService.isBordering(countryBot.getCountry().getId(), enemyCountry.getCountry().getId())){
                    borderingCountries.add(countryBot);
                    break;
                }
            }
        }
        return borderingCountries;
    }


    /**
     * Ejecuta la fase de ataque del bot balanceado siguiendo las reglas estratégicas:
     * - Solo ataca si tiene al menos el doble de tropas que el enemigo
     * - Prioriza países que sean parte de su objetivo secreto
     * - Si no hay ataques favorables, pasa sin atacar
     * - Considera la conectividad entre países para objetivos especiales
     *
     * @param player El player bot que ejecuta el turno
     * @param game La partida actual en curso
     */
    @Override
    public void attack(PlayerGame player, Game game) {
        countriesBot = player.getCountries();
        List<CountryGame> enemyCountries = countryGameService.findByGame(game)
                .stream()
                .filter(p -> p.getPlayerGame().getId() != player.getId())
                .toList();

        ProcessedObjective processedObjective = AnalizeObjective.analizeObjective(player.getSecretObjective());
        List<AttackOption> availableAttacks = findAvailableAttacks(countriesBot, enemyCountries,processedObjective,player);

        if(availableAttacks.isEmpty())return;

        availableAttacks.sort((a1,a2) -> {
            if(a1.isFromObjective() && !a2.isFromObjective()) return -1;
            if(!a1.isFromObjective() && a2.isFromObjective()) return 1;

            return Double.compare(a2.getTroopsAdvantage(), a1.getTroopsAdvantage());
        });

        AttackOption bestAttack = availableAttacks.get(0);
        CountryGame origin = bestAttack.getOriginCountry();
        CountryGame destination = bestAttack.getDestinationCountry();
        combatService.announceAttack(origin.getId(), destination.getId());

        boolean conquest = CombatUtil.resolveCombat(origin, destination, player, gameService);
        if(conquest) conquerThisTurn = true;
    }


    /**
     * Encuentra todos los ataques tácticamente viables según las reglas del bot balanceado.
     * Un ataque es viable si:
     * - El país atacante tiene más de 1 ejército (debe dejar reserva)
     * - Los países son limítrofes
     * - El atacante tiene al menos el doble de tropas que el defensor
     *
     * @param countriesBot Lista de países que posee el bot
     * @param countriesEnemy Lista de todos los países enemigos
     * @param processedObjective Objetivo del player ya analizado y estructurado
     * @param player El player bot (necesario para análisis de objetivos complejos)
     * @return Lista de opciones de ataque ordenables por prioridad
     */
    public List<AttackOption> findAvailableAttacks(List<CountryGame> countriesBot,
                                                   List<CountryGame> countriesEnemy,
                                                   ProcessedObjective processedObjective,
                                                   PlayerGame player) {
        List<AttackOption> attacks = new ArrayList<>();

        for(CountryGame countryBot : countriesBot){
            if(countryBot.getAmountArmies() <= 1) continue;

            for(CountryGame enemyCountry : countriesEnemy){
                if(!countryGameService.isBordering(countryBot.getCountry().getId(), enemyCountry.getCountry().getId())) continue;

               double troopsAdvantage = (double) countryBot.getAmountArmies()/ enemyCountry.getAmountArmies();
               if(troopsAdvantage <2.0) continue;

               boolean isFromObjective = isPartOfObjective(enemyCountry, processedObjective, player);

               attacks.add(new AttackOption(countryBot, enemyCountry, troopsAdvantage, isFromObjective));
            }
        }
        return attacks;
    }


    /**
     * Determina si un país dado (propio o enemigo) es relevante para cumplir
     * el objetivo secreto del player bot.
     * Este metodo es utilizado tanto en la fase de ataque (para decidir a quién atacar),
     * como en la fase de refuerzo (para priorizar qué países reforzar).
     * Un país se considera parte del objetivo si:
     *     Está en un continente requerido.
     *     Corresponde a un subconjunto de países específicos definidos en el objetivo.
     *     Contribuye al cumplimiento del objetivo de tener países limítrofes entre sí.
     *     Es de un player con color objetivo en caso de misión de destrucción.
     *
     * @param evaluatedCountry El país a evaluar (puede ser propio o enemigo).
     * @param processedObjective Estructura que representa el objetivo secreto ya analizado.
     * @param player El player bot al que pertenece el objetivo.
     * @return true si el país contribuye directamente al objetivo.
     */
    public boolean isPartOfObjective(CountryGame evaluatedCountry, ProcessedObjective processedObjective, PlayerGame player) {
        String countryName = evaluatedCountry.getCountry().getName();
        String countryContinent = evaluatedCountry.getCountry().getContinent().getName();
        String enemyColor = evaluatedCountry.getPlayerGame().getColor().getName();

        switch(processedObjective.getType()){
            case CONTINENT_AND_COUNTRIES:
                if(processedObjective.getTotalContinents() != null && processedObjective.getTotalContinents().contains(countryContinent)){
                    return true;
                }
                if(processedObjective.getCountriesPerContinent() != null && processedObjective.getCountriesPerContinent().containsKey(countryContinent)){
                    return true;
                }
                if(processedObjective.getSingleCountries() != null && processedObjective.getSingleCountries().contains(countryName)){
                    return true;
                }
                if(processedObjective.getSingleCountries() != null && processedObjective.getSingleCountries().contains("BORDERING_EACH_OTHER")){
                    return handleBorderlineObjectives(evaluatedCountry, player);
                }
                break;
            case ARMY_COLOR:
                if(enemyColor != null && enemyColor.equals(processedObjective.getObjectiveColor())){
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }

    /**
     * Maneja la lógica específica para el objetivo 2 del TEG:
     * "Conquistar América del Sur + 7 países de Europa + 3 países limítrofes entre sí"
     * Este metodo evalúa si conquistar un país específico ayudaría a completar
     * el requisito de tener 3 países que sean todos limítrofes entre sí.
     *
     * @param countryEnemy El país enemigo que se está considerando atacar
     * @param player El player bot para analizar su situación actual
     * @return true si conquistar este país contribuye al grupo de países limítrofes
     */
    public boolean handleBorderlineObjectives(CountryGame countryEnemy, PlayerGame player) {
        List<CountryGame> playerCountries = player.getCountries();

        List<CountryGame> borderingCountries = new ArrayList<>();
        for(CountryGame country : playerCountries){
            if(isBorderingAndKnowsAnotherOwnCountry(country, playerCountries)){
                borderingCountries.add(country);
            }
        }
        if(borderingCountries.size() >= 3 && areAllBorderingEachOther(borderingCountries)) return false;
        return canCompleteBorderingGroup(countryEnemy, playerCountries);
    }


    /**
     * Simula qué ocurriría si el jugador conquistara un país específico,
     * y evalúa si esto le permitiría formar un grupo de 3 países limítrofes entre sí.
     *
     * @param enemyCountry El país que se está considerando conquistar
     * @param playerCountry Los países actuales del jugador
     * @return true si conquistar este país permitiría completar el objetivo de países limítrofes
     */
    public boolean canCompleteBorderingGroup(CountryGame enemyCountry, List<CountryGame> playerCountry) {
        List<CountryGame> simulatedCountries = new ArrayList<>(playerCountry);
        CountryGame simulatedCountry = new CountryGame();
        simulatedCountry.setCountry(enemyCountry.getCountry());
        simulatedCountries.add(simulatedCountry);

        List<List<CountryGame>> borderingGroups = findBorderingGroups(simulatedCountries);
        
        for(List<CountryGame> group : borderingGroups){
            if(group.size() >= 3 && areAllBorderingEachOther(group)){
                return true;
            }
        }
        return false;
    }


    /**
     * Identifica todos los grupos de países conectados usando algoritmo de búsqueda en profundidad.
     * Un grupo es un conjunto de países donde existe un camino de adyacencias entre cualquier par.
     *
     * @param simulatedCountries Lista completa de países a analizar
     * @return Lista de grupos, donde cada grupo es una lista de países conectados
     */
    public List<List<CountryGame>> findBorderingGroups(List<CountryGame> simulatedCountries) {
        List<List<CountryGame>> groups = new ArrayList<>();
        Set<CountryGame> visited = new HashSet<>();

        for(CountryGame country : simulatedCountries){
            if(!visited.contains(country)){
                List<CountryGame> group = new ArrayList<>();
                findBorderingGroupsDFS(country, simulatedCountries, visited, group);
                if(group.size()>1){
                        groups.add(group);
                }
            }
        }
        return groups;
    }


    /**
     * Implementación recursiva de búsqueda en profundidad (DFS) para encontrar
     * todos los países conectados a partir de un país inicial.
     *
     * @param country País actual en la búsqueda
     * @param simulatedCountries Lista completa de países disponibles
     * @param visited Set de países ya procesados (evita ciclos infinitos)
     * @param group Lista que se va construyendo con el group conectado
     */
    public void findBorderingGroupsDFS(CountryGame country, List<CountryGame> simulatedCountries, Set<CountryGame> visited, List<CountryGame> group) {
        visited.add(country);
        group.add(country);

        for(CountryGame anotherCountry : simulatedCountries) {
            if(!visited.contains(anotherCountry) && countryGameService.isBordering(
                    country.getCountry().getId(), anotherCountry.getCountry().getId())){
                findBorderingGroupsDFS(anotherCountry, simulatedCountries, visited, group);
            }
        }
    }


    /**
     * Verifica si una lista de países están todos conectados entre sí (grafo completo).
     * Para el objetivo 2, necesita exactamente 3 países donde cada uno sea limítrofe
     * con los otros dos.
     *
     * @param borderingCountries Lista de países a verificar
     * @return true si todos los países son limítrofes entre sí
     */
    public boolean areAllBorderingEachOther(List<CountryGame> borderingCountries) {
        for(int i = 0; i < borderingCountries.size(); i++){
            for(int j = i+1; j<borderingCountries.size(); j++){
                if(!countryGameService.isBordering(borderingCountries.get(i).getCountry().getId(),
                        borderingCountries.get(j).getCountry().getId()));
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica si un país del jugador tiene al menos un país propio adyacente.
     * Esto es útil para identificar países que forman parte de grupos conectados.
     *
     * @param country El país a evaluar
     * @param playerCountries Lista completa de países del jugador
     * @return true si el país tiene al menos un país propio adyacente
     */
    public boolean isBorderingAndKnowsAnotherOwnCountry(CountryGame country, List<CountryGame> playerCountries) {
        for(CountryGame anotherCountry : playerCountries){
            if(!country.equals(anotherCountry) && countryGameService.isBordering(country.getCountry().getId(), anotherCountry.getCountry().getId()))
                return true;
        }
        return false;
    }

    /**
     * Ejecuta la fase de reagrupación del bot balanceado, moviendo tropas
     * de manera estratégica entre sus propios territorios.
     * La lógica implementada sigue las siguientes reglas:
     *     - Prioriza mover tropas desde territorios seguros (no limítrofes con enemigos)
     *         hacia territorios fronterizos con pocas tropas.
     *     - Los territorios seguros deben tener más de un ejército para poder ceder tropas.
     *     - El movimiento se realiza solo si los territorios origen y destino son adyacentes.
     *     - En caso de no haber territorios fronterizos válidos, intenta reagrupar entre
     *         territorios propios cercanos para consolidar fuerzas.
     * Solo se realiza un movimiento por turno en esta fase.
     *
     * @param player El player bot actual.
     * @param game La partida en curso.
     */
    @Override
    public void regroup(PlayerGame player, Game game) {
        List<CountryGame> countriesBot = player.getCountries();
        
        List<CountryGame> enemyCountries = countryGameService.findByGame(game)
                .stream()
                .filter(p -> p.getPlayerGame().getId() != player.getId())
                .toList();
        List<CountryGame> borderingCountries = filterBorderingCountries(countriesBot, enemyCountries);
        
        List<CountryGame> safeTerritories = countriesBot.stream().filter(p -> !borderingCountries.contains(p)).toList();

        List<CountryGame> movements = moveTroops(safeTerritories, borderingCountries);
        if(!movements.isEmpty()){
            movements.get(0).setAmountArmies(movements.get(0).getAmountArmies() - 1);
            movements.get(1).setAmountArmies(movements.get(1).getAmountArmies() + 1);
        } else {
            //orden ascendente
            countriesBot.sort(Comparator.comparingInt(CountryGame::getAmountArmies));
            for(CountryGame country : countriesBot){
                if(country.getAmountArmies() <= 1) continue;
                country.setAmountArmies(country.getAmountArmies() - 1);
                countriesBot.get(countriesBot.size()-1).setAmountArmies(country.getAmountArmies() + 1);
                countryGameService.save(country);
                break;
            }
        }
    }

    /**
     * Determina un movimiento de reagrupación desde un territorio seguro hacia uno fronterizo.
     * Un territorio seguro es aquel que no tiene enemigos adyacentes, y que posee más de un ejército disponible.
     * La lógica del bot balanceado prioriza trasladar tropas hacia la frontera para reforzar posiciones expuestas.
     * Esta función devuelve una lista de dos elementos:
     * - El país de origen (de donde se moverá una tropa)
     * - El país de destino (que recibirá la tropa)
     * Si no se encuentra un movimiento válido, devuelve una lista vacía.
     *
     * @param secure Lista de territorios del bot sin enemigos limítrofes.
     * @param borderingCountries Lista de territorios del bot que tienen enemigos adyacentes.
     * @return Lista con [origen, destino] si se encuentra un movimiento, o una lista vacía si no.
     */

    public List<CountryGame> moveTroops(List<CountryGame> secure, List<CountryGame> borderingCountries) {
        List<CountryGame> movements = new ArrayList<>();

        if(secure.isEmpty() || secure == null || borderingCountries.isEmpty() || borderingCountries == null) return movements;

        List<CountryGame> possibleOrigin = secure.stream().filter(p -> p.getAmountArmies() > 1).toList();

        if(possibleOrigin.isEmpty()) return movements;

        List<CountryGame> orderedDestinations = borderingCountries.stream().sorted(Comparator.comparingInt(CountryGame::getAmountArmies)).toList();

        for(CountryGame destination : orderedDestinations){
            for(CountryGame origin : possibleOrigin){
                if(countryGameService.isBordering(destination.getCountry().getId(), origin.getCountry().getId())){
                    movements.add(origin);
                    movements.add(destination);
                    return movements;
                }
            }
        }
        return movements;
    }

}
