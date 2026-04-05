package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.PlayerDTO;
import ar.edu.utn.frc.tup.piii.dto.CountryGameDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase utilitaria para mapeo entre entidades JugadorPartida y DTOs JugadorDto.
 * Maneja conversiones complejas incluyendo relaciones con países, objetivos y partidas.
 * {@code @author:} GabrielaCamacho
 */
@Component
public class PlayerMapper {

    /**
     * Convierte una entidad JugadorPartida a su correspondiente DTO.
     * Incluye el mapeo de relaciones como países, objetivos y color.
     *
     * @param entity Entidad JugadorPartida a convertir
     * @return DTO del jugador con todas sus relaciones mapeadas o null si la entidad es null
     */
    public static PlayerDTO toDto(PlayerGame entity){
        if(entity == null)return null;

        List<CountryGameDTO> countryGameDTOS = entity.getCountries() != null ?
        entity.getCountries().stream()
                .map(CountryGameMapper::toDto)
                .toList() :
                new ArrayList<>();

        return PlayerDTO.builder()
                .playerGameId(entity.getId())
                .orderTurn(entity.getOrderTurn())
                .isTurn(entity.isTurn())
                .color(entity.getColor() != null ? entity.getColor().toString() : null)
                .objectiveAchieved(entity.isObjectiveAchieved())
                .active(entity.isActive())
                .secretObjectiveId(entity.getSecretObjective() != null ? entity.getSecretObjective().getId() : null)
                .secretObjectiveDescription(entity.getSecretObjective() != null ? entity.getSecretObjective().getDescription() : null)
                .countries(countryGameDTOS)
                .build();
    }

    /**
     * Convierte un DTO JugadorDto a una entidad JugadorPartida completa.
     * Requiere todas las entidades relacionadas como parámetros.
     *
     * @param dto DTO del basePlayer a convertir
     * @param basePlayer Entidad JugadorBase asociada
     * @param objective Entidad Objetivo secreta del basePlayer
     * @param countryGames Lista de países controlados por el basePlayer
     * @param game Entidad Partida a la que pertenece
     * @param color Enum Color asignado al basePlayer
     * @return Entidad JugadorPartida completa o null si el DTO es null
     */
    public static PlayerGame toEntity(PlayerDTO dto, BasePlayer basePlayer, Objective objective, List<CountryGame> countryGames, Game game, Color color) {
        if (dto == null) return null;

        PlayerGame jp = new PlayerGame();
        jp.setId(dto.getPlayerGameId());
        jp.setOrderTurn(dto.getOrderTurn());
        jp.setTurn(dto.isTurn());
        jp.setColor(color);
        jp.setObjectiveAchieved(dto.isObjectiveAchieved());
        jp.setActive(dto.isActive());
        jp.setPlayer(basePlayer);
        jp.setGame(game);
        jp.setSecretObjective(objective);
        jp.setCountries(countryGames);

        return jp;
    }

    /**
     * Convierte una lista de entidades JugadorPartida a una lista de DTOs.
     *
     * @param playerGames Lista de entidades jugador a convertir
     * @return Lista de DTOs o lista vacía si la entrada es null o vacía
     */
    public static List<PlayerDTO> toDtoList(List<PlayerGame> playerGames) {
        if (playerGames == null || playerGames.isEmpty()) {
            return new ArrayList<>();
        }

        return playerGames.stream()
                .map(PlayerMapper::toDto)
                .toList();
    }

    /**
     * Convierte un DTO JugadorDto a una entidad JugadorPartida básica.
     * No establece las relaciones complejas, útil para casos donde estas se asignan posteriormente.
     *
     * @param dto DTO del jugador a convertir
     * @return Entidad JugadorPartida con propiedades básicas o null si el DTO es null
     * @implNote Las relaciones (jugador, partida, objetivo, países, color) deben establecerse manualmente
     */
    public static PlayerGame toEntitySimple(PlayerDTO dto) {
        if (dto == null) {
            return null;
        }

        PlayerGame jp = new PlayerGame();
        jp.setId(dto.getPlayerGameId());
        jp.setOrderTurn(dto.getOrderTurn());
        jp.setTurn(dto.isTurn());
        jp.setObjectiveAchieved(dto.isObjectiveAchieved());
        jp.setActive(dto.isActive());
        // Las relaciones (jugador, partida, objetivo, países, color) se setearían después

        return jp;
    }
}
