package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.AttackDto;
import ar.edu.utn.frc.tup.piii.dto.ResultAttackDto;
import ar.edu.utn.frc.tup.piii.dto.RegroupArmyDto;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.Turn;
import ar.edu.utn.frc.tup.piii.services.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.TurnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST que gestiona las operaciones relacionadas con los turnos
 * de cada jugador en una partida del juego TEG.
 * Incluye endpoints para:
 * - Crear y actualizar turnos
 * - Iniciar un turno
 * - Pasar de fase
 * - Consultar acciones disponibles
 * - Finalizar turno
 *
 * @author GabrielaCamacho
 * @version 1.0
 *
 * @see TurnService
 * @see Turn
 * @see PlayerGame
 * @see Game
 */
@RestController
@RequestMapping("/api/v1/turns")
@RequiredArgsConstructor
public class TurnController {

    private final TurnService turnService;
    private final CombatService combatService;
    private final PlayerService playerService;
    private final GameService gameService;

    /**
     * Guarda un nuevo turno.
     */
    @PostMapping
    public ResponseEntity<Boolean> save(@RequestBody Turn turn) {
        try {
            Boolean res = turnService.save(turn);
            if (res) {
                return ResponseEntity.status(HttpStatus.CREATED).body(res);
            } else {
                return ResponseEntity.badRequest().body(res);
            }
        } catch (Exception e) {
            System.out.println("Error en el controlador turno -> save");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    /**
     * Busca un turno por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Turn> findById(@PathVariable int id) {
        try {
            Turn res = turnService.findById(id);
            if (res != null) {
                return ResponseEntity.ok().body(res);
            } else {
                return ResponseEntity.badRequest().body(res);
            }
        } catch (Exception e) {
            System.out.println("Error en el controlador turno -> find by id");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Devuelve todos los turnos registrados.
     */
    @GetMapping
    public ResponseEntity<List<Turn>> findAll() {
        try {
            List<Turn> res = turnService.findAll();
            if (!res.isEmpty()) {
                return ResponseEntity.ok().body(res);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("Error en el controlador turno -> find all");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Inicia un nuevo turno para un jugador en una partida.
     */
    @PostMapping("/start")
    public ResponseEntity<Void> startTurn(@RequestParam int gameId) {
        try {
            PlayerGame player = playerService.findByGameId(gameId)
                    .stream()
                    .filter(j -> j.getOrderTurn() == 1)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("JugadorPartida no encontrado"));

            Game game = gameService.findById(gameId);

            turnService.startTurn(player, game);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error en el controlador turno -> iniciar turno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    //Permite al humano colocar  ejercitos durante la ronda
    @PutMapping("/{playerId}/put-armies")
    public ResponseEntity<String> putArmies(@PathVariable int playerId,
                                            @RequestParam int countryId,
                                            @RequestParam int amount) {
        try {
            turnService.putArmy(playerId, countryId, amount);
            return ResponseEntity.ok("Ejército colocado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }



    /**
     * Cambia a la siguiente fase del turno actual.
     */
    @PostMapping("/{id}/move-phase")
    public ResponseEntity<Void> movePhase(@PathVariable int id) {
        try {
            Turn turn = turnService.findById(id);
            turnService.movePhase(turn);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error en el controlador turno -> pasar fase");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retorna las acciones disponibles en la fase actual del turno.
     */
    @GetMapping("/{id}/available-actions")
    public ResponseEntity<List<String>> getAvailableActions(@PathVariable int id) {
        try {
            Turn turn = turnService.findById(id);
            if (turn == null) {
                return ResponseEntity.notFound().build();
            }
            List<String> res = turnService.getAvailableActions(turn);
            if (res != null && !res.isEmpty()) {
                return ResponseEntity.ok().body(res);
            } else {
                return ResponseEntity.ok().body(List.of());
            }
        } catch (Exception e) {
            System.out.println("Error en el controlador turno -> obtener acciones disponibles");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Finaliza el turno actual y pasa al siguiente jugador.
     */
    @PostMapping("/{id}/finish")
    public ResponseEntity<Void> finishTurn(@PathVariable int id) {
        try {
            Turn turn = turnService.findById(id);
            turnService.finishTurn(turn);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error en el controlador turno -> finalizar turno");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/regroup")
    public ResponseEntity<String> regroupArmies(@RequestBody RegroupArmyDto dto) {
        try {
            combatService.regroupArmy(dto.getPlayerId(), dto.getOriginId(), dto.getDestinationId(), dto.getAmount());
            return ResponseEntity.ok("Ejércitos reagrupados correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno al reagrupar ejércitos");
        }
    }

    @PostMapping("/attack")
    public ResponseEntity<?> attack(@RequestBody AttackDto dto) {
        try {
            ResultAttackDto result = combatService.attack(dto.getGameId(),dto.getCountryAttackerId(), dto.getCountryDefenderId(), dto.getDice());

            System.out.println("Enviando result al frontend: " + result);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno: " + e.getMessage()));
        }
    }

}
