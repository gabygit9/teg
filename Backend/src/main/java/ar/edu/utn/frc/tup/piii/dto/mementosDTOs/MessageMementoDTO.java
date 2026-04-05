package ar.edu.utn.frc.tup.piii.dto.mementosDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageMementoDTO {
    private int id;
    private String senderName;
    private String content;
    private boolean activeState;
    private boolean modified;
    private LocalDateTime dateTime;
}
