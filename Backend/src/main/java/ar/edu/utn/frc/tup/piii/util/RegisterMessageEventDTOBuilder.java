package ar.edu.utn.frc.tup.piii.util;

import ar.edu.utn.frc.tup.piii.dto.RegisterMessageEventDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;

public class RegisterMessageEventDTOBuilder {



    public static RegisterMessageEventDTO forAttacksAndMovements(CountryGame origin, CountryGame destination, int troops) {
        RegisterMessageEventDTO dto = new RegisterMessageEventDTO();
        dto.setGame(origin.getGame());
        dto.setOriginCountry(origin);
        dto.setDestinationCountry(destination);
        dto.setAmountTroops(troops);
        return dto;
    }






}
