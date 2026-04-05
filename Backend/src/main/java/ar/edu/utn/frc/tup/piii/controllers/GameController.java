package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.*;
import ar.edu.utn.frc.tup.piii.mappers.PlayerGameMapper;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.Turn;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestionar operaciones relacionadas con la entidad Partida.
 * Permite iniciar, continuar, finalizar partidas, consultar información y manejar lógica general del juego.
 *
 * @author GabrielaCamacho
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/games")
public class GameController {

    private final GameService gameService;
    private final CombatService combatService;
    private final TurnService turnService;
    private final CardService cardService;
    private final PlayerService playerService;
    private final PlayerGameMapper playerGameMapper;

    /**
     * Inicia una nueva gameDTO con los datos recibidos.
     *
     * @param gameDTO Objeto gameDTO con la configuración inicial.
     * @return La gameDTO creada.
     */
    @PostMapping("/init")
    public ResponseEntity<GameDTO> startGame(@RequestBody GameDTO gameDTO) {
        try {
            Game res = gameService.startGame(gameService.dtoToEntity(gameDTO));

            if (res.getId() > 0) {
                return ResponseEntity.ok(gameService.entityToDto(res));
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            System.out.println("Error en el controlador gameDTO -> iniciar gameDTO");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/state")
    public ResponseEntity<String> getGameState(@PathVariable int id) {
        try {
            Game game = gameService.findById(id);
            String state = game.getStates().getDescription();
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener el estado de la partida");
        }
    }


    /**
     * Finaliza una partida por ID.
     *
     * @param id ID de la partida.
     */
    @PutMapping("/finish/{id}")
    public ResponseEntity<String> finishGame(@PathVariable int id) {
        try {
            boolean res = gameService.endGame(id);

            if (res) {
                return ResponseEntity.ok("Partida finalizada correctamente");
            } else {
                return ResponseEntity.badRequest().body("La partida ya estaba finalizada o no se pudo actualizar");
            }
        } catch (Exception e) {
            System.out.println("Error en el controlador partida -> finalizar partida");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }



    @GetMapping("/{gameId}/player/{playerGameId}")
    public ResponseEntity<PlayerGameDto> getPlayerGame(
            @PathVariable int gameId,
            @PathVariable int playerGameId) {
        Optional<PlayerGame> player = playerService.getAPlayerInAGame(gameId, playerGameId);
        System.out.println(player);
        return player.map(jp -> ResponseEntity.ok(playerGameMapper.toDto(jp)))
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * Recupera una partida específica por ID.
     *
     * @param id ID de la partida.
     * @return La partida encontrada, o null.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Game> getById(@PathVariable int id) {
        try {
            Game res = gameService.findById(id);

            if (res != null) {
                return ResponseEntity.ok(res);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            System.out.println("Error en el controlador partida -> obtener por id");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/turn/locate")
    public ResponseEntity<?> putArmies(@RequestBody CollocationDto dto) {
        try {
            turnService.putArmy(dto.getPlayerGameId(), dto.getCountryId(), dto.getArmies());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/turn/attack")
    public ResponseEntity<?> attack(@RequestBody AttackDto dto) {
        try {
            ResultAttackDto result = combatService.attack(dto.getGameId(), dto.getCountryAttackerId(), dto.getCountryDefenderId(), dto.getDice());
            if (result.isWasConquest()) {
                cardService.askCard(dto.getPlayerGameId());
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/turn/regroup")
    public ResponseEntity<?> regroup(@RequestBody RegroupArmyDto dto) {
        try {
            combatService.regroupArmy(dto.getPlayerId(), dto.getOriginId(), dto.getDestinationId(), dto.getAmount());
            return ResponseEntity.ok("Reagrupamiento realizado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/turn/finish")
    public ResponseEntity<Map<String, String>> finishTurn(@RequestBody FinishTurnDto dto) {
        try {
            Turn turn = turnService.findById(dto.getTurnId());
            if (turn == null) throw new IllegalArgumentException("Turno no encontrado");
            turnService.finishTurn(turn);
            turnService.finishTurnRound(dto.getPlayerGameId(), turn);
            return ResponseEntity.ok(Map.of("message", "Turno finalizado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}/players")
    public ResponseEntity<List<PlayerGame>> getPlayer(@PathVariable int id) {
        List<PlayerGame> players = gameService.getPlayerGame(id);
        return players.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(players);
    }

    @GetMapping("/{id}/player-in-turn")
    public ResponseEntity<PlayerGameDto> getPlayerInTurn(@PathVariable int id) {
        Optional<PlayerGame> player = playerService.getPlayerInTurn(id);
        return player.map(jp -> ResponseEntity.ok(playerGameMapper.toDto(jp)))
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * Registra que un jugador ha conquistado un país.
     *
     * @param countryId  id del país conquistado.
     * @param player id del jugador conquistador.
     */
    @PostMapping("/conquer-country")
    public ResponseEntity<Void> conquerCountry(@RequestParam PlayerGame player, @RequestParam int countryId) {
        try {
            combatService.conquerCountry(countryId, player);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error en el controlador partida -> conquistar país");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Devuelve el tipo de comunicación configurado para la partida.
     *
     * @param id ID de la partida.
     * @return Descripción del estilo de comunicación (ej. "Fair Play").
     */
    @GetMapping("/{id}/communication")
    public ResponseEntity<String> communicationStyle(@PathVariable int id) {
        try {
            String res = gameService.communicationStyle(id);

            if (!res.isBlank()) {
                return ResponseEntity.ok().body(res);
            } else {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            System.out.println("Error en el controlador partida -> estilo comunicación");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
    }

    /**
     * Asigna el objetivo común a una partida.
     *
     * @param gameId ID de la partida.
     */
    @PostMapping("/{gameId}/assign-common-objective")
    public ResponseEntity<Void> assignCommonObjective(@PathVariable int gameId) {
        try {
            gameService.assignCommonObjective(gameId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error en el controlador partida -> asignar objetivo común");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //Cambiar de estado --> 1ºronda, 2ºronda, hostilidades...
    @PostMapping("/{gameId}/move-state")
    public ResponseEntity<Map<String, String>> moveState(@PathVariable int gameId) {
        try {
            gameService.moveState(gameId);
            return ResponseEntity.ok(Map.of("message", "Estado de la partida actualizado correctamente"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "No se puede avanzar: " + e.getMessage()));
        }
    }

    @PostMapping("/attack")
    public ResponseEntity<ResultAttackDto> attack(@RequestParam int gameId,
                                                  @RequestParam int countryAttackerId,
                                                  @RequestParam int countryDefenderId,
                                                  @RequestParam int maximumDice) {
        try {
            ResultAttackDto result = combatService.attack(gameId, countryAttackerId, countryDefenderId, maximumDice);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.out.println("Error en el controlador partida -> realizar ataque");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/data-game/{gameId}")
    public ResponseEntity<BigJsonDTO> getDataGame(@PathVariable int gameId) {
        BigJsonDTO res = gameService.getDataGame(gameId);
        return ResponseEntity.ok(res);
    }


}