package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.PlayerGameDto;
import ar.edu.utn.frc.tup.piii.dto.ObjectiveDto;
import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.PlayerGame;
import ar.edu.utn.frc.tup.piii.model.entities.Turn;
import ar.edu.utn.frc.tup.piii.services.interfaces.TurnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerGameMapper {
    private final TurnService turnService;
    public final PlayerGameDto toDto(PlayerGame jp) {
        PlayerGameDto dto = new PlayerGameDto();
        dto.setId(jp.getId());
        dto.setColor(jp.getColor().getName());
        dto.setObjective(new ObjectiveDto(jp.getSecretObjective().getId(), jp.getSecretObjective().getDescription()));
        dto.setPlayer(BasePlayerMapper.toDto(jp.getPlayer()));
        dto.setHuman(jp.getPlayer() instanceof HumanPlayer);
        dto.setDeleted(jp.isActive());
        dto.setTurn(jp.isTurn());

        Turn turn = turnService.getPlayerGameId(jp.getId());
        if (turn != null) {
            dto.setTurnId(turn.getId());
            dto.setCurrentPhase(turn.getCurrentPhase().name());
        }
        return dto;
    }
}
