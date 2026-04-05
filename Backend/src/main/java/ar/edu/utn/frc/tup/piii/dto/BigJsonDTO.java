package ar.edu.utn.frc.tup.piii.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BigJsonDTO {

    private GameResponseDTO game;
    private List<PlayerGameDto> players;
    private List<CountryGameDTO> countries;

}
