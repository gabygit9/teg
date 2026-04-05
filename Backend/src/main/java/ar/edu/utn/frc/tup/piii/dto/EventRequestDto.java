package ar.edu.utn.frc.tup.piii.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class EventRequestDto {

    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    public EventRequestDto(String description, LocalDateTime dateTime) {
        this.description = description;
        this.dateTime = dateTime;
    }
}
