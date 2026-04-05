package ar.edu.utn.frc.tup.piii.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayMatchDto {
    int playerGameId;
    int turnId;
    int countryAttackerId;
    int countryDeffenderId;
    int dice;
    int armiesToPut;
    int regroupOriginCountry;
    int regroupDestinationCountry;
    int regroupArmies;
}
