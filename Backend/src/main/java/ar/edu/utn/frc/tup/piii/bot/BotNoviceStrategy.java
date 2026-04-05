package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.dto.RegisterMessageEventDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.StateGameEnum;
import ar.edu.utn.frc.tup.piii.services.impl.PlayerServiceImpl;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.state.GameContext;
import ar.edu.utn.frc.tup.piii.util.CombatUtil;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEventDTOBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 *  Representa un jugador bot con comportamiento novato:
 * Este bot realiza acciones básicas y sin planificación avanzada:
 * - Reparte sus ejércitos disponibles aleatoriamente entre sus países.
 * - Ataca automáticamente si encuentra un país enemigo vecino con menos ejércitos.
 * - Reagrupa 1 ejército desde un país hacia otro país vecino propio si es posible.
 * Su comportamiento simula un jugador inexperto, útil para partidas de baja dificultad
 * o pruebas del sistema.
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see BotPlayer
 * @see IBehaviorBot
 */
@Data
@Component
public class BotNoviceStrategy implements IBehaviorBot {

    List<CountryGame> countries;
    private final PlayerService playerService;
    private final CountryGameService countryGameService;
    private final GameService gameService;
    private final CardExchangeService exchangeService;
    private final TurnService turnService;
    private final CardService cardService;
    private final ContinentService continentService;
    boolean conqueredThisTurn = false;
    private Game gameActual;
    private final CombatService combatService;
    private final RegisterMessageEvent registerMessageEvent;
    private final HistoryService historyService;
    private ObjectiveService objectiveService;
    private GameStateService gameStateService;

    @Autowired
    public BotNoviceStrategy(
            @Lazy PlayerServiceImpl playerService,
            CountryGameService countryGameService,
            @Lazy GameService gameService,
            @Lazy CardExchangeService exchangeService,
            @Lazy CardService cardService,
            ContinentService continentService,
            CombatService combatService,
            TurnService turnService,
            RegisterMessageEvent registerMessageEvent,
            HistoryService historyService,
            ObjectiveService objectiveService,
            GameStateService gameStateService) {
        this.playerService = playerService;
        this.countryGameService = countryGameService;
        this.gameService = gameService;
        this.exchangeService = exchangeService;
        this.cardService = cardService;
        this.continentService = continentService;
        this.combatService = combatService;
        this.turnService = turnService;
        this.registerMessageEvent = registerMessageEvent;
        this.historyService = historyService;
        this.objectiveService = objectiveService;
        this.gameStateService = gameStateService;
    }



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
        System.out.println("=== BOT NOVATO ejecutando turno ===");
        System.out.println("Jugador: " + player.getPlayer().getName());
        System.out.println("Ejércitos disponibles: " + player.getPlayer().getAvailableArmies());

        this.conqueredThisTurn = false;
        String state = game.getStates().getDescription();
        System.out.println("Bot novato realizando su turno " + state);

        // PRIMERA RONDA: Coloca 5 ejércitos
        if (state.equalsIgnoreCase(StateGameEnum.FIRST_ROUND.name())) {
            System.out.println("Bot primera ronda");
            this.gameActual = game;
            putInitialArmies(player, player.getPlayer().getAvailableArmies());
            return;
        }

        // SEGUNDA RONDA: Coloca 3 ejércitos
        if (state.equalsIgnoreCase(StateGameEnum.SECOND_ROUND.name())) {
            System.out.println("Bot segunda ronda");
            this.gameActual = game;
            putInitialArmies(player, player.getPlayer().getAvailableArmies());
            return;
        }

        //Hostilidades
        if (state.equalsIgnoreCase(StateGameEnum.HOSTILITIES.name())) {
            // a. Canjear tarjetas si tiene 5 o más
            if (player.getCards().size() >= 5 && exchangeService.canExchange(player.getId())) {
                exchangeService.doExchange(player.getId(), game.getId());
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
        }
        // para verificar objetivo y finalizar si corresponde
        GameContext context = new GameContext(
                game,
                gameService,
                countryGameService,
                cardService,
                playerService,
                gameStateService,
                objectiveService
        );
        context.updateStateFromGame(game);
        context.executeTurn(game);
    }

    private void putInitialArmies(PlayerGame player, int amount) {
        List<CountryGame> countries = player.getCountries();
        if (countries == null || countries.isEmpty()) return;

        Random random = new Random();
        int total = amount;

        System.out.println("Bot colocando " + amount + " ejércitos iniciales");

        while (total > 0 && player.getPlayer().getAvailableArmies() > 0) {
            CountryGame country = countries.get(random.nextInt(countries.size()));
            country.setAmountArmies(country.getAmountArmies() + 1);

            player.getPlayer().setAvailableArmies(player.getPlayer().getAvailableArmies() - 1);

            //eliminar estas lineas si hay algun problema
            countryGameService.save(country);
            playerService.persistConcretPlayer(player.getPlayer());

            String message = RegisterMessageEvent.putArmy(player.getPlayer(), country.getCountry(), 1);
            historyService.registerEvent(gameActual, message);


            total--;

        }
        System.out.println("Bot terminó de colocar ejércitos iniciales");
    }

    /**
     * Reparte los ejércitos disponibles del player bot entre sus países de forma aleatoria.
     * Solo distribuye los ejércitos que el player tiene asignados en {@code ejercitosDisponibles}.
     *
     * @param player El player bot actual.
     * @param game La partida en curso.
     */
    @Override
    public void distributeArmies(PlayerGame player, Game game) {
        List<CountryGame> playerCountries = player.getCountries();
        int armies = player.getPlayer().getAvailableArmies();
        if(armies <=0)return;
        if(playerCountries == null || playerCountries.isEmpty())return;
        int size = playerCountries.size();


        Random random = new Random();
        System.out.println("Bot repartiendo " + armies + " ejércitos");
        while(armies > 0){
            int index = random.nextInt(size);
            CountryGame country = playerCountries.get(index);
            try {
                turnService.putArmy(player.getId(), country.getCountry().getId(), 1);
                System.out.println("Bot colocó 1 ejército en " + country.getCountry().getName());
                String message = RegisterMessageEvent.putArmy(player.getPlayer(), country.getCountry(), 1);
                historyService.registerEvent(game, message);
                armies--;

            } catch (Exception e) {
                System.out.println("Error al colocar ejército: " + e.getMessage());
                break;
            }
//            country.setCantidadEjercitos(country.getCantidadEjercitos()+1);
//            paisPartidaService.save(country);
//            armies--;
        }
        player.getPlayer().setAvailableArmies(0);
        playerService.savePlayerGame(game.getId(),player.getPlayer().getId(),player.getColor().getId());

    }

    /**
     * Ataca al primer país enemigo vecino que tenga menos ejércitos que uno de los países del bot.
     * La lógica es simple: busca la primera oportunidad de ataque favorable y la ejecuta.
     * Solo realiza un ataque por turno.
     *
     * @param player El player bot actual.
     * @param game La partida en curso.
     */
    @Override
    public void attack(PlayerGame player, Game game) {
        countries = player.getCountries();
        System.out.println("CANTIDAD DE EJERCITOS: "+player.getCountries());

        for(CountryGame countryBot : countries){
                if(countryBot.getAmountArmies() <= 1) continue;
                List<CountryGame> enemyNeighbor = countryGameService.findEnemyNeighbors(countryBot.getCountry().getId(), player, game);
                for (CountryGame enemyCountry : enemyNeighbor){
                    if(countryBot.getAmountArmies() > enemyCountry.getAmountArmies()){
                        combatService.announceAttack(countryBot.getId(), enemyCountry.getId());
                        RegisterMessageEventDTO dto = RegisterMessageEventDTOBuilder.forAttacksAndMovements(countryBot, enemyCountry, countryBot.getAmountArmies());
                        String attackMessage = registerMessageEvent.attackArmiesRegistry(dto);
                        historyService.registerEvent(game, attackMessage);

                        boolean conquered = CombatUtil.resolveCombat(countryBot, enemyCountry,player, gameService);
                        if(conquered) conqueredThisTurn = true;
                        String conquerMessage = registerMessageEvent.conquerCountryRegistry(dto);
                        historyService.registerEvent(game, conquerMessage);

                        return;
                    }
                }
        }
    }

    /**
     * Reagrupa un único ejército desde un país del bot hacia otro país vecino también controlado por él.
     * Solo reagrupa si el país origen tiene más de un ejército y son países adyacentes.
     *
     * @param player El player bot actual.
     * @param game La partida en curso.
     */
    @Override
    public void regroup(PlayerGame player, Game game) {
        countries = player.getCountries();
        if (countries == null || countries.isEmpty()) return;

        CountryGame originCountry, destinationCountry;
        int originIndex, destinationIndex;

        do {
            originIndex = (int) Math.floor(Math.random() * countries.size());
            destinationIndex = (int) Math.floor((Math.random() * countries.size()));
            originCountry = countries.get(originIndex);
            destinationCountry = countries.get(destinationIndex);
        } while (originIndex == destinationIndex || originCountry.getAmountArmies() <= 1);

        if (countryGameService.isBordering(originCountry.getCountry().getId(), destinationCountry.getCountry().getId())) {
            originCountry.setAmountArmies(originCountry.getAmountArmies() - 1);
            destinationCountry.setAmountArmies(destinationCountry.getAmountArmies() + 1);
            RegisterMessageEventDTO dto = RegisterMessageEventDTOBuilder.forAttacksAndMovements(originCountry, destinationCountry, 1);
            String message = registerMessageEvent.moveArmiesRegistry(dto);
            historyService.registerEvent(game, message);
        }
    }
}
