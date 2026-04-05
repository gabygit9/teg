package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.ResultAttackDto;
import ar.edu.utn.frc.tup.piii.dto.RegisterMessageEventDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEventDTOBuilder;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

import static ar.edu.utn.frc.tup.piii.util.CombatUtil.combatService;

@Service
@Getter
@RequiredArgsConstructor
@Transactional
public class CombatServiceImpl implements CombatService {
    private final PlayerService playerService;
    private final CountryGameService countryGameService;
    private final ContinentService continentService;
    private final HistoryService historyService;
    private final RegisterMessageEvent registerMessageEvent;
    private final CardService cardService;

    @Override
    public List<Integer> throwDice(int attackerId, int defenderId, int maxDice) {
        Random random = new Random();

        List<Integer> atackerDice = new ArrayList<>();

        if (maxDice > 3) { maxDice = 3; }

        for (int i = 0; i < maxDice; i++) {
            atackerDice.add(random.nextInt(6) + 1);
        }

        atackerDice.sort(Collections.reverseOrder());

        return new ArrayList<>(atackerDice);
    }

    @Override
    public void conquerCountry(int countryId, PlayerGame player) {
        Country country = continentService.findCountryById(countryId);

        if (player == null || player.getGame() == null) {
            throw new IllegalArgumentException("Jugador no válido");
        }

        if (country == null) {
            throw new IllegalArgumentException("Pais no encontrado");
        }

        CountryGame pp = countryGameService.findByCountryAndGameId(country, player.getGame().getId());
        if (pp == null) {
            throw new IllegalArgumentException("el pais de la partida no fue encontrado");
        }

        System.out.println("SE ESTÄ CONQUISTANDO EL PAIS");

        pp.setPlayerGame(player);
        countryGameService.save(pp);

        cardService.markConquer(player.getId());
    }

    @Override
    public void announceAttack(CountryGameId attackerId, CountryGameId defenderId) {
        if (attackerId != defenderId) {
            CountryGame attacker = countryGameService.findById(attackerId.getCountryId(), attackerId.getGameId());
            CountryGame defender = countryGameService.findById(defenderId.getCountryId(), defenderId.getGameId());

            if (attacker == null || defender == null) {
                System.out.println("No se puede anunciar el ataque. País no encontrado");
                return;
            }

            String playerAttacker = attacker.getPlayerGame().getPlayer().getName();

            System.out.println("jugador: " + playerAttacker);
        }
    }

    @Override
    @Transactional
    public ResultAttackDto attack(int gameId, int countryAttackerId, int countryDefenderId, int maxDice) {
        // Validar parámetros de entrada
        if (gameId <= 0 || countryAttackerId <= 0 || countryDefenderId <= 0) {
            throw new IllegalArgumentException("IDs inválidos proporcionados");
        }

        if (countryAttackerId == countryDefenderId) {
            throw new IllegalArgumentException("Un país no puede atacarse a sí mismo");
        }

        // Obtener países atacante y defensor
        CountryGame attackerCountry;
        CountryGame defenderCountry;

        try {
            System.out.println("Buscando atacante con paisId=" + countryAttackerId + ", partidaId=" + gameId);
            attackerCountry = countryGameService.findById(countryAttackerId, gameId);
            if (attackerCountry == null) {
                throw new IllegalArgumentException("País atacante no encontrado con paisId=" + countryAttackerId + ", partidaId=" + gameId);
            }

            System.out.println("Atacante obtenido: " + attackerCountry.getCountry().getName());
        } catch (Exception e) {
            System.out.println("Error al obtener país atacante: " + e.getMessage());
            throw new IllegalArgumentException("País atacante no encontrado", e);
        }

        try {
            System.out.println("Buscando defensor con paisId=" + countryDefenderId + ", partidaId=" + gameId);
            defenderCountry = countryGameService.findById(countryDefenderId, gameId);
            if (defenderCountry == null) {
                throw new IllegalArgumentException("País defensor no encontrado con paisId=" + countryDefenderId + ", partidaId=" + gameId);
            }
            System.out.println("defenderCountry.getPais(): " + defenderCountry.getCountry());

            System.out.println("Defensor obtenido: " + defenderCountry.getCountry().getName());
        } catch (Exception e) {
            System.out.println("Error al obtener país defensor: " + e.getMessage());
            throw new IllegalArgumentException("País defensor no encontrado", e);
        }

        // Validar que los jugadores existen y son diferentes
        PlayerGame attackerPlayer = attackerCountry.getPlayerGame();
        PlayerGame defenderPlayer = defenderCountry.getPlayerGame();

        if (attackerPlayer == null || defenderPlayer == null) {
            throw new IllegalArgumentException("Jugador atacante o defensor no definido");
        }
        System.out.printf("Ataque recibido: partidaId=%d, atacantePaisId=%d, defensorPaisId=%d, dados=%d%n",
                gameId, countryAttackerId, countryDefenderId, maxDice);

        if (Objects.equals(attackerPlayer.getId(), defenderPlayer.getId())) {
            throw new IllegalArgumentException("No puedes atacar a tu propio país");
        }
        System.out.println("Atacante tiene ejércitos: " + attackerCountry.getAmountArmies());

        // Validar que los países son limítrofes
        if (!countryGameService.isBordering(countryAttackerId, countryDefenderId)) {
            throw new IllegalArgumentException("Los países no son limítrofes");
        }

        int availableArmies = attackerCountry.getAmountArmies();

        if (availableArmies <= 1) {
            throw new IllegalArgumentException("El país atacante no tiene suficientes ejércitos para atacar");
        }

        int defenderDice = Math.min(2, defenderCountry.getAmountArmies());
        int maxAttackerDice;
        if (availableArmies == 2) {
            maxAttackerDice = 1;
        } else if (availableArmies == 3) {
            maxAttackerDice = 2;
        } else {
            maxAttackerDice = 3;
        }

        if (maxDice > maxAttackerDice || maxDice <= 0) {
            throw new IllegalArgumentException("Número inválido de dados para el atacante. Máximo permitido: " + maxAttackerDice);
        }

        // Realizar tiradas de dados
        List<Integer> attackerThrowing = throwDice(maxDice);
        List<Integer> defenderThrowing = throwDice(defenderDice);

        // Ordenar dados de mayor a menor
        attackerThrowing.sort(Comparator.reverseOrder());
        defenderThrowing.sort(Comparator.reverseOrder());

        // Calcular pérdidas
        int attackerLose = 0;
        int defenderLose = 0;

        int comparatives = Math.min(attackerThrowing.size(), defenderThrowing.size());
        for (int i = 0; i < comparatives; i++) {
            if (attackerThrowing.get(i) > defenderThrowing.get(i)) {
                defenderLose++;
            } else {
                attackerLose++;
            }
        }

        // Actualizar cantidades de ejércitos
        attackerCountry.setAmountArmies(attackerCountry.getAmountArmies() - attackerLose);
        defenderCountry.setAmountArmies(defenderCountry.getAmountArmies() - defenderLose);

        // registro en historial
        RegisterMessageEventDTO dto = RegisterMessageEventDTOBuilder.forAttacksAndMovements(attackerCountry, defenderCountry, maxDice);
        String message = registerMessageEvent.attackArmiesRegistry(dto);
        historyService.registerEvent(attackerCountry.getGame(), message);

        boolean wasConquer = false;

        // Verificar si hubo conquista
        if (defenderCountry.getAmountArmies() <= 0) {
            // El defensor pierde el país
            defenderCountry.setPlayerGame(attackerPlayer);
            defenderCountry.setAmountArmies(maxDice); // Los ejércitos que se mueven al país conquistado
            attackerCountry.setAmountArmies(attackerCountry.getAmountArmies() - maxDice);
            wasConquer = true;
            combatService.conquerCountry(defenderCountry.getCountry().getId(), attackerPlayer);

            // registro en historial
            RegisterMessageEventDTO dtoConquer = RegisterMessageEventDTOBuilder.forAttacksAndMovements(attackerCountry, defenderCountry, maxDice);
            String conquerMessage = registerMessageEvent.conquerCountryRegistry(dtoConquer);
            historyService.registerEvent(attackerCountry.getGame(), conquerMessage);


            System.out.println("Conquista! " + attackerCountry.getCountry().getName() +
                    " ha conquistado " + defenderCountry.getCountry().getName());
        }

        // Guardar cambios
        try {
            countryGameService.save(attackerCountry);
            countryGameService.save(defenderCountry);
        } catch (Exception e) {
            System.out.println("Error al guardar los cambios: " + e.getMessage());
            throw new RuntimeException("Error al guardar el resultado del ataque", e);
        }

        return new ResultAttackDto(attackerThrowing, defenderThrowing, wasConquer);
    }

    private List<Integer> throwDice(int quantity) {
        Random random = new Random();
        List<Integer> dice = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            dice.add(random.nextInt(6) + 1);
        }
        return dice;
    }

    @Override
    public void regroupArmy(int playerId, int originId, int destinationId, int amount) {
        System.out.println("Reagrupamiento iniciado");
        System.out.println("Buscando jugadorPartida con ID = " + playerId);

        Optional<PlayerGame> player = playerService.findPlayerGameById(playerId);
        if (player.isEmpty()) {
            System.out.println("Jugador no encontrado");
            throw new IllegalArgumentException("Jugador no encontrado");
        }

        int gameId = player.get().getGame().getId();
        System.out.println("Jugador encontrado en partida ID = " + gameId);

        CountryGame origin = countryGameService.findById(originId, gameId);
        CountryGame destination = countryGameService.findById(destinationId, gameId);

        System.out.println("País origin: " + origin.getCountry().getName() + " (Ejércitos: " + origin.getAmountArmies() + ")");
        System.out.println("País destination: " + destination.getCountry().getName() + " (Ejércitos: " + destination.getAmountArmies() + ")");
        System.out.println("Ejércitos a reagrupar: " + amount);

        if (origin.getAmountArmies() <= amount) {
            System.out.println("No se puede dejar el país sin al menos un ejército.");
            throw new IllegalArgumentException("No se puede dejar el país sin al menos un ejército.");
        }

        if (!countryGameService.isBordering(originId, destinationId)) {
            System.out.println("Los países no son limítrofes.");
            throw new IllegalArgumentException("Los países no son limítrofes.");
        }

        if (origin.getPlayerGame().getId() != destination.getPlayerGame().getId()) {
            System.out.println("Los países no pertenecen al mismo player");
            throw new IllegalArgumentException("Ambos países deben pertenecer al mismo player");
        }

        System.out.println("Validaciones superadas, aplicando cambios...");

        origin.setAmountArmies(origin.getAmountArmies() - amount);
        destination.setAmountArmies(destination.getAmountArmies() + amount);

        countryGameService.save(origin);
        countryGameService.save(destination);

        // para registro de historial
        RegisterMessageEventDTO dto = RegisterMessageEventDTOBuilder.forAttacksAndMovements(origin, destination, amount);
        String message = registerMessageEvent.moveArmiesRegistry(dto);
        historyService.registerEvent(origin.getGame(), message);


        System.out.println("Cambios guardados. Reagrupamiento finalizado.");
    }
}
