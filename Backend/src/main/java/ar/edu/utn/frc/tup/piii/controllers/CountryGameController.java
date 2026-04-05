package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.ArmyMovementsDto;
import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.dto.NeighborsRequestDto;
import ar.edu.utn.frc.tup.piii.mappers.CountryGameMapper;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGameId;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.CountryGameService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Controlador REST para gestionar el estado de los países en una partida.
 *
 * Este controlador expone endpoints para consultar y modificar la información
 * relacionada a los países ocupados durante el transcurso del juego, como:
 * - Refuerzos
 * - Reagrupaciones
 * - Ocupaciones
 * - Cantidad de ejércitos por país
 *
 * Se utiliza durante el desarrollo de la lógica principal del juego, como el refuerzo de tropas,
 * la fase de ataque o el control del mapa por parte de los jugadores.
 *
 * @author GabrielaCamacho
 * @version 1.0
 * @see CountryGame
 * @see Game
 * @see PlayerGame
 */
@RestController
@RequestMapping("/api/v1/country-game")
@RequiredArgsConstructor
@Validated
public class CountryGameController {

    private final CountryGameService countryGameService;
    private final PlayerService playerService;
    private final GameService gameService;


    /**
     * Devuelve todos los países de una partida (mapa actual).
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<List<CountryGameDTO>> getGameMap(@PathVariable int gameId) {
        List<CountryGameDTO> countriesDto = countryGameService.getCountriesOfGame(gameId);

        return countriesDto.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(countriesDto);
    }


    /**
     * Devuelve todos los países ocupados por un jugador en una partida.
     */
    @GetMapping("/{gameId}/player/{playerId}")
    public ResponseEntity<List<CountryGameDTO>> countriesPerPlayer(@PathVariable int gameId,
                                                                   @PathVariable int playerId) {
        List<CountryGame> countriesPlayer = countryGameService.findByGameAndPlayerGame(gameId, playerId);

        if (countriesPlayer.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<CountryGameDTO> countriesDTO = CountryGameMapper.toDtoList(countriesPlayer);

        return ResponseEntity.ok(countriesDTO);
    }

    /**
     * Aumenta la cantidad de ejércitos en un país.
     */
    @PostMapping("/increase-armies")
    public ResponseEntity<?> strengthenCountry(@RequestBody @NotNull int countryId,
                                               @RequestBody @NotNull int gameId,
                                               @RequestBody @NotNull int amount) {
        CountryGameId countryGameId = new CountryGameId(countryId, gameId);
        return countryGameService.increaseArmies(countryGameId, amount) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Reduce la cantidad de ejércitos en un país.
     */
    @PostMapping("/reduce-armies")
    public ResponseEntity<Void> reduceArmies(@RequestBody @Valid ArmyMovementsDto dto) {
        CountryGameId countryGameId = new CountryGameId(dto.getCountryId(), dto.getGameId());
        return countryGameService.reduceArmies(countryGameId, dto.getAmount()) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();

    }

    @PostMapping("/enemy-neighbors")
    public ResponseEntity<List<CountryGame>> getEnemyNeighbors(@RequestBody NeighborsRequestDto dto){
        List<PlayerGame> players = playerService.findByGameId(dto.getGameId());
        PlayerGame player = players.stream()
                .filter(j -> j.getId() == dto.getPlayerGameId())
                .findFirst()
                .orElse(null);

        if (player == null) return ResponseEntity.badRequest().build();

        Game game = gameService.findById(dto.getGameId());
        List<CountryGame> neighbors = countryGameService.findEnemyNeighbors(dto.getCountryId(), player, game);
        return neighbors.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(neighbors);
    }


    @PostMapping("/{id}/distribute-countries")
    public ResponseEntity<List<CountryGame>> distributeCountries(@PathVariable int id) {
        List<CountryGame> res = countryGameService.distributeInitialCountries(id);
        return ResponseEntity.ok(res);
    }
}


