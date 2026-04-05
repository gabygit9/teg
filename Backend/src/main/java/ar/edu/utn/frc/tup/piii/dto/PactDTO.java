package ar.edu.utn.frc.tup.piii.dto;

import lombok.Data;

import java.util.List;

@Data
public class PactDTO {
    private int gameId;
    private int pactTypeId;
    private int creatorPlayerId;
    private List<Integer> playersId;
    private List<Integer> countriesId;
}