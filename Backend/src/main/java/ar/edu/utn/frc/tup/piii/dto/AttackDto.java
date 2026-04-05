package ar.edu.utn.frc.tup.piii.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttackDto {
    private int gameId;
    private int playerGameId;
    private int countryAttackerId;
    private int countryDefenderId;
    private int dice;
}
