package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.CountryGameMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.CountryGame;

public class CountryGameMementoMapper {

    public static CountryGameMementoDTO toDTO(CountryGame countryGame) {
        return new CountryGameMementoDTO(
                countryGame.getCountry().getId(),
                countryGame.getCountry().getName(),
                countryGame.getCountry().getContinent().getName(),
                countryGame.getPlayerGame().getId(),
                countryGame.getPlayerGame().getPlayer().getName(),
                countryGame.getPlayerGame().getColor().getName(),
                countryGame.getAmountArmies()
        );
    }
}
