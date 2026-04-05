package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.ObjectiveDto;
import ar.edu.utn.frc.tup.piii.mappers.ObjectiveMapper;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.Objective;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.ObjectiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para la gestión de objetivos del juego.
 * Permite operaciones de consulta sobre los objetivos secretos y comunes.
 *
 * @author GabrielaCamacho
 */
@RestController
@RequestMapping("/api/v1/objectives")
@RequiredArgsConstructor
public class ObjectiveController {
    private final ObjectiveService objectiveService;
    private final PlayerService playerService;

    /**
     * Obtiene la lista de todos los objetivos registrados.
     */
    @GetMapping
    public ResponseEntity<List<Objective>> getAll() {
        try {
            List<Objective> res = objectiveService.findAll();
            if (!res.isEmpty()) {
                return ResponseEntity.ok().body(res);
            } else {
                return ResponseEntity.badRequest().body(res);
            }
        } catch (Exception e) {
            System.out.println("Error en el controlador objetivo -> get all");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/{gameId}/player/{playerGameId}/objective")
    public ResponseEntity<ObjectiveDto> getSecretObjective(@PathVariable int gameId, @PathVariable int playerGameId) {
        Optional<PlayerGame> playerGame = playerService.getAPlayerInAGame(gameId, playerGameId);

        if (playerGame.isPresent()) {
            Objective objective = playerGame.get().getSecretObjective();
            return ResponseEntity.ok(ObjectiveMapper.toDto(objective));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
