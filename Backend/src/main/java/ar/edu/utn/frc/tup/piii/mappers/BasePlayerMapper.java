package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase utilitaria para mapeo entre entidades JugadorBase y DTOs JugadorBaseDto.
 * Proporciona métodos estáticos para conversiones bidireccionales y manejo de colecciones.
 * {@code @author:} GabrielaCamacho
 */
@Component
public class BasePlayerMapper {
    /**
     * Convierte una entidad JugadorBase a su correspondiente DTO.
     *
     * @param player Entidad player a convertir
     * @return DTO del player o null si la entidad es null
     */
    public static BasePlayerDTO toDto(BasePlayer player) {
        if (player == null) {
            return null;
        }
        BasePlayerDTO dto = new BasePlayerDTO();
        dto.setId(player.getId());
        dto.setPlayerName(player.getName());
        dto.setAvailableArmies(player.getAvailableArmies());
        return dto;
    }

    /**
     * Convierte un DTO JugadorBaseDto a una entidad JugadorHumano.
     *
     * @param dto DTO del jugador a convertir
     * @return Entidad JugadorHumano o null si el DTO es null
     */
    public static HumanPlayer toEntity(BasePlayerDTO dto) {
        if (dto == null) {
            return null;
        }

        HumanPlayer player = new HumanPlayer();
        player.setId(0);
        player.setName(dto.getPlayerName());
        player.setAvailableArmies(dto.getAvailableArmies());
        return player;
    }

    /**
     * Convierte una lista de entidades JugadorBase a una lista de DTOs.
     *
     * @param players Lista de entidades jugador a convertir
     * @return Lista de DTOs o lista vacía si la entrada es null o vacía
     */
    public static List<BasePlayerDTO> toDtoList(List<BasePlayer> players) {
        if (players == null || players.isEmpty()) {
            return new ArrayList<>();
        }

        return players.stream()
                .map(BasePlayerMapper::toDto)
                .collect(Collectors.toList());
    }
}
