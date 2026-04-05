package ar.edu.utn.frc.tup.piii.dto.mementosDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryMementoDTO {
    private int id;
    private String description;
    private LocalDateTime dateTime;
}
