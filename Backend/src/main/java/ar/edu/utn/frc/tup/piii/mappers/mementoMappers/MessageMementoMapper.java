package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.MessageMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.Message;

public class MessageMementoMapper {

    public static MessageMementoDTO toDTO(Message message) {
        return new MessageMementoDTO(
                message.getId(),
                message.getSender().getName(),
                message.getContent(),
                message.isActiveState(),
                message.isModified(),
                message.getDatetime()
        );
    }
}
