package ar.edu.utn.frc.tup.piii.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO para enviar un mensaje en el chat.
 *
 * @author GabrielaCamacho
 */
@Data
public class MessageDTO {

    private int id;

    @JsonProperty("gameId")
    private int gameId;

    @JsonProperty("senderId")
    private int senderId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("isActive")
    private boolean stateActive;

    @JsonProperty("isEdited")
    private boolean modified;

    @JsonProperty("dateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime dateTime;
}
