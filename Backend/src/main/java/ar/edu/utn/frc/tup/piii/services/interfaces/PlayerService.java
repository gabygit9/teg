package ar.edu.utn.frc.tup.piii.services.interfaces;

import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.dto.PlayerGameDto;
import ar.edu.utn.frc.tup.piii.model.entities.*;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de servicio para la gestión de jugadores en el sistema.
 * Define las operaciones de negocio disponibles para el manejo de jugadores.
 * {@code @author:} Ismael Ceballos
 */
public interface PlayerService {
    Optional<BasePlayerDTO> findById(int id);

    List<BasePlayerDTO> findAll();

    boolean update(BasePlayer player);

    BasePlayerDTO updateArmies(int id, int armies);

    HumanPlayer saveHumanPlayer(BasePlayerDTO basePlayerDTO);

    PlayerGame savePlayerGame(int gameId, int basePlayerId, int colorId);

    List<PlayerGame> findByGameId(int gameId);

    Optional<PlayerGame> findPlayerGameById(int id);

    void persistConcretPlayer(BasePlayer player);

    PlayerGame createHumanPlayerAndAssignToGame(BasePlayerDTO dto, String user, int gameId, int colorId);

    Optional<PlayerGame> getPlayerInTurn(int id);

    Optional<PlayerGame> getPlayerTurnByGame(int gameId);

    Optional<PlayerGame> getAPlayerInAGame(int gameId, int playerGameId);

    void save(PlayerGame playerGame);

    void saveBasePlayer(BasePlayer basePlayer);

    PlayerGameDto createBotPlayerGame(int gameId, int difficultId, int colorId);

    List<PlayerGame> findAllPlayersGame();

    PlayerGameDto assignHumanInGame(int gameId, String name, int colorId);

    PlayerGameDto PlayerGameToDTO(PlayerGame jp);

    Boolean executeTurnBot(int playerGameId, int gameId, int turnId);

    BasePlayerDTO findBasePlayerPerUserId(int userId);

    boolean wasEliminatedColor(String colorObjective, int id);

}

