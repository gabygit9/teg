package ar.edu.utn.frc.tup.piii.dto;

import lombok.Data;

@Data
public class PlayerCardDto {
    private int id;
    private String country;
    private String symbol;
    private boolean used;
}
