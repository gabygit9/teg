package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.PlayerCardDto;
import ar.edu.utn.frc.tup.piii.model.entities.CardPlayer;

public class CardMapper {
    public static PlayerCardDto toDto(CardPlayer tj) {
       PlayerCardDto dto =new PlayerCardDto();
       dto.setId(tj.getId());
       dto.setCountry(tj.getCardCountry().getCountry().getName());
       dto.setSymbol(tj.getCardCountry().getSymbol().getType());
       dto.setUsed(tj.isUsed());
       return dto;
    }
}
