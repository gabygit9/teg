package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CountryGameMapper {
    public static CountryGameDTO toDto(CountryGame entity) {
        if(entity == null) return null;

        return new CountryGameDTO(
                entity.getId().getCountryId(),
                entity.getId().getGameId(),
                entity.getCountry().getName(),
                entity.getCountry().getContinent().getName(),
                entity.getAmountArmies(),
                entity.getPlayerGame().getId(),
                entity.getPlayerGame().getPlayer().getName(),
                entity.getPlayerGame().getColor().toString()
        );
    }

    public static CountryGame toEntity(CountryGameDTO dto, Country country, Game game, PlayerGame player) {
        if (dto == null) return null;

        CountryGameId id = new CountryGameId();
        id.setCountryId(dto.getCountryId());
        id.setGameId(dto.getGameId());

        CountryGame entity = new CountryGame();
        entity.setId(id);
        entity.setCountry(country);
        entity.setGame(game);
        entity.setAmountArmies(dto.getAvailableArmies());
        entity.setPlayerGame(player);

        return entity;
    }

    /**
     * Convierte una lista de entidades PaisPartida a una lista de DTOs.
     *
     * @param entities Lista de entidades PaisPartida a convertir
     * @return Lista de DTOs o lista vacía si la entrada es null o vacía
     */
    public static List<CountryGameDTO> toDtoList(List<CountryGame> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }

        return entities.stream()
                .map(CountryGameMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de DTOs PaisPartidaDto a una lista de entidades.
     * Nota: Este metodo requiere que proporciones los mapas de dependencias necesarios.
     *
     * @param dtos Lista de DTOs a convertir
     * @param countriesMap Mapa de países por ID
     * @param gamesMap Mapa de partidas por ID
     * @param playersMap Mapa de jugadores partida por ID
     * @return Lista de entidades o lista vacía si la entrada es null o vacía
     */
    public static List<CountryGame> toEntityList(List<CountryGameDTO> dtos,
                                                 Map<Integer, Country> countriesMap,
                                                 Map<Integer, Game> gamesMap,
                                                 Map<Integer, PlayerGame> playersMap) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }

        return dtos.stream()
                .map(dto -> {
                    Country country = countriesMap.get(dto.getCountryId());
                    Game game = gamesMap.get(dto.getGameId());
                    PlayerGame playerGame = playersMap.get(dto.getPlayerId());

                    return toEntity(dto, country, game, playerGame);
                })
                .collect(Collectors.toList());
    }
}
