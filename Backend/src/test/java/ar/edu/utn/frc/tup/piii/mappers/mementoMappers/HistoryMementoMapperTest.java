package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.HistoryMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.HistoryEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HistoryMementoMapperTest {

    @Test
    void toDTO() {
        LocalDateTime date = LocalDateTime.now();

        HistoryEvent event = new HistoryEvent();
        event.setId(1);
        event.setDescription("Evento de prueba");
        event.setDateTime(date);

        HistoryMementoDTO dto = HistoryMementoMapper.toDTO(event);

        assertEquals(1, dto.getId());
        assertEquals("Evento de prueba", dto.getDescription());
        assertEquals(date, dto.getDateTime());
    }
}
