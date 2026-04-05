package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.CreatePlayerRequestDto;
import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.dto.PlayerGameDto;
import ar.edu.utn.frc.tup.piii.mappers.BasePlayerMapper;
import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Esta clase es el controlador de la clase jugador donde se realizaran los endpoints de la API REST
 * {@code @author:} Ismael Ceballos
 */
@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    /**
     * Obtiene la lista de todos los jugadores.
     *
     * @return ResponseEntity con:
     *         - 200 OK: Lista de jugadores si existen registros
     *         - 204 No Content: Si no hay jugadores registrados
     */
    @GetMapping
    public ResponseEntity<List<BasePlayerDTO>> getAll() {
        List<BasePlayerDTO> players = playerService.findAll();

        return players.isEmpty()
                ? ResponseEntity.noContent().build() //204
                : ResponseEntity.ok(players);
    }

    /**
     * Obtiene un jugador específico mediante su identificador único.
     *
     * @param id Identificador único del jugador a buscar
     * @return ResponseEntity con:
     *         - 200 OK: Datos del jugador encontrado
     * @throws NoSuchElementException Si no existe un jugador con el ID especificado
     */
    @GetMapping("/{id}")
    public ResponseEntity<BasePlayerDTO> getById(@PathVariable int id) throws NoSuchElementException {
        Optional<BasePlayerDTO> player = playerService.findById(id);
        return player.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/player-game-bot/{gameId}")
    public ResponseEntity<PlayerGameDto> registrarJugadorBot(@PathVariable int gameId,
                                                             @RequestParam int difficultId,
                                                             @RequestParam int colorId) {
        PlayerGameDto bot = playerService.createBotPlayerGame(gameId, difficultId, colorId);
        return ResponseEntity.ok(bot);
    }

    @PostMapping("/player-game-human/{gameId}")
    public ResponseEntity<PlayerGameDto> assignaHumanInGame(@PathVariable int gameId,
                                                            @RequestParam String name,
                                                            @RequestParam int colorId) {
        PlayerGameDto res = playerService.assignHumanInGame(gameId, name, colorId);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/humans")
    public ResponseEntity<BasePlayerDTO> registerHumanPlayer(@RequestBody CreatePlayerRequestDto request) {
        // Armar DTO básico con los datos del jugador
        BasePlayerDTO dto = new BasePlayerDTO();
        dto.setPlayerName(request.getName());
        dto.setAvailableArmies(request.getAvailableArmies());

        try {
            // Crear jugador humano y asociarlo a la partida
            PlayerGame jp = playerService.createHumanPlayerAndAssignToGame(
                    dto,
                    request.getUser(),
                    request.getGameId(),
                    request.getColorId()
            );

            // Devolver respuesta con DTO
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(BasePlayerMapper.toDto(jp.getPlayer()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/load-player")
    public ResponseEntity<HumanPlayer> createPlayer(@RequestBody BasePlayerDTO player) {
        HumanPlayer res = playerService.saveHumanPlayer(player);
        return res != null ?
                ResponseEntity.status(HttpStatus.CREATED).body(res)
                : ResponseEntity.badRequest().build();
    }

    /**
     * Actualiza la cantidad de ejércitos disponibles para un jugador existente.
     * Valida que la cantidad de ejércitos sea un valor válido (no negativo).
     *
     * @param id Identificador único del jugador a actualizar
     * @param dto Objeto que contiene la nueva cantidad de ejércitos disponibles
     * @return ResponseEntity con:
     *         - 200 OK: Jugador actualizado exitosamente con los nuevos datos
     *         - 400 Bad Request: Si el ID no existe o la cantidad de ejércitos es inválida (negativa)
     */
    @PutMapping("/{id}/armies")
    public ResponseEntity<BasePlayerDTO> updatePlayerArmies(@PathVariable int id, @RequestBody BasePlayerDTO dto) {
        BasePlayerDTO playerUpdated = playerService.updateArmies(id, dto.getAvailableArmies());
        return ResponseEntity.ok(playerUpdated);

    }

    @GetMapping("/base-per-user/{userId}")
    public ResponseEntity<BasePlayerDTO> getBasePlayerPerIdUser(@PathVariable int userId) {
        BasePlayerDTO basePlayerDTO = playerService.findBasePlayerPerUserId(userId);
        return ResponseEntity.ok(basePlayerDTO);
    }

    @PostMapping("/execute-turn-bot/{gameId}")
    public ResponseEntity<Boolean> executeActionsBot(@PathVariable int gameId,
                                                     @RequestParam int playerGameId,
                                                     @RequestParam int turnId) {
        Boolean res = playerService.executeTurnBot(playerGameId, gameId, turnId);
        return ResponseEntity.ok(res);
    }

}


