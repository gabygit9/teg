package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.PactMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import ar.edu.utn.frc.tup.piii.model.entities.Country;
import ar.edu.utn.frc.tup.piii.model.entities.Pact;

import java.util.List;

public class PactMementoMapper {

    public static PactMementoDTO toDTO(Pact pact) {
        List<String> playerNames = pact.getPlayers().stream()
                .map(BasePlayer::getName)
                .toList();

        List<String> countryNames = pact.getCountries().stream()
                .map(Country::getName)
                .toList();

        return new PactMementoDTO(
                pact.getId(),
                pact.getPactType().getDescription(),
                playerNames,
                countryNames,
                pact.getPlayerCreated().getName(),
                pact.isActive(),
                pact.getDateTime()
        );
    }
}
