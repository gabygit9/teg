package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.GameStateMementoDTO;
import ar.edu.utn.frc.tup.piii.dto.SaveGameRequestDTO;
import ar.edu.utn.frc.tup.piii.model.entities.GameMemento;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameMementoService;
import ar.edu.utn.frc.tup.piii.model.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mementos")
public class GameMementoController {

    @Autowired
    private GameMementoService gameMementoService;

    @Autowired
    private GameRepository gameRepository;


    // Obtener el último estado guardado de una partida
    @GetMapping("/game/{gameId}/last")
    public ResponseEntity<GameMemento> getLastState(@PathVariable int gameId) {
        if (gameId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        GameMemento last = gameMementoService.getLastState(gameId);
        if (last == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(last);
    }

    // Guardar un nuevo memento para una partida
    @PostMapping("/game/{gameId}/save")
    public ResponseEntity<GameMemento> saveMemento(
            @PathVariable int gameId,
            @RequestParam(defaultValue = "1") int version) {

        if (gameId <= 0 || version <= 0) {
            return ResponseEntity.badRequest().build();
        }

        Game game = gameRepository.findById(gameId).orElse(null);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }

        GameMemento mementoSaved = gameMementoService.saveMementoComplete(game, version);
        return ResponseEntity.ok(mementoSaved);
    }

    // Restaurar un estado desde un memento
        @PostMapping("/restore/{mementoId}")
        public ResponseEntity<GameStateMementoDTO> restoreState(@PathVariable int mementoId) {
            System.out.println("Recibido mementoId: " + mementoId);
            if (mementoId <= 0) {
                System.out.println("MementoId inválido");
                return ResponseEntity.badRequest().body(null);
            }
            try {
                GameStateMementoDTO restoredState = gameMementoService.restoreAndPersistState(mementoId);
                System.out.println("Estado restaurado correctamente");
                return ResponseEntity.ok(restoredState);
            } catch (RuntimeException e) {
                System.err.println("Error al restaurar: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.badRequest().body(null);
            }
        }

    @GetMapping
    public ResponseEntity<List<SaveGameRequestDTO>> listSaveGames() {
        List<SaveGameRequestDTO> games = gameMementoService.listSaveGames();
        return ResponseEntity.ok(games);
    }

}
