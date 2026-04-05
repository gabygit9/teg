package ar.edu.utn.frc.tup.piii.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePlayerRequestDto {
    private String name;
    private String user;
    private int availableArmies;
    private int gameId;
    private int colorId;
}
