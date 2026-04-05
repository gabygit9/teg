package ar.edu.utn.frc.tup.piii.dto.mementosDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PactMementoDTO {
    private int id;
    private String pactType;
    private List<String> playerNames;
    private List<String> countryNames;
    private String creatorName;
    private boolean active;
    private LocalDateTime dateTime;
}
