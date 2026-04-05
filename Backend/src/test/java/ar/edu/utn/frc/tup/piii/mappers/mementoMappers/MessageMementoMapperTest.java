package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.MessageMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import ar.edu.utn.frc.tup.piii.model.entities.Message;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageMementoMapperTest {

    @Test
    void toDTO() {
        LocalDateTime date = LocalDateTime.now();

        BasePlayer emitter = new BasePlayer() {};
        emitter.setName("Juan");

        Message message = new Message();
        message.setId(2);
        message.setSender(emitter);
        message.setContent("¡Hola mundo!");
        message.setActiveState(true);
        message.setModified(false);
        message.setDatetime(date);

        MessageMementoDTO dto = MessageMementoMapper.toDTO(message);

        assertEquals(2, dto.getId());
        assertEquals("Juan", dto.getSenderName());
        assertEquals("¡Hola mundo!", dto.getContent());
        assertTrue(dto.isActiveState());
        assertFalse(dto.isModified());
        assertEquals(date, dto.getDateTime());
    }
}
