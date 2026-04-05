package ar.edu.utn.frc.tup.piii.mappers.mementoMappers;

import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.PactMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import ar.edu.utn.frc.tup.piii.model.entities.Country;
import ar.edu.utn.frc.tup.piii.model.entities.Pact;
import ar.edu.utn.frc.tup.piii.model.entities.PactType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PactMementoMapperTest {

    @Test
    void toDTO() {
        LocalDateTime date = LocalDateTime.now();

        PactType type = new PactType();
        type.setDescription("Alianza");

        BasePlayer j1 = new BasePlayer() {};
        j1.setName("Alicia");
        BasePlayer j2 = new BasePlayer() {};
        j2.setName("Benito");

        Country p1 = new Country();
        p1.setName("Narnia");
        Country p2 = new Country();
        p2.setName("Oz");

        Pact pact = new Pact();
        pact.setId(4);
        pact.setPactType(type);
        pact.setPlayers(List.of(j1, j2));
        pact.setCountries(List.of(p1, p2));
        pact.setPlayerCreated(j2);
        pact.setActive(false);
        pact.setDateTime(date);

        PactMementoDTO dto = PactMementoMapper.toDTO(pact);

        assertEquals(4, dto.getId());
        assertEquals("Alianza", dto.getPactType());
        assertEquals(List.of("Alicia", "Benito"), dto.getPlayerNames());
        assertEquals(List.of("Narnia", "Oz"), dto.getCountryNames());
        assertEquals("Benito", dto.getCreatorName());
        assertFalse(dto.isActive());
        assertEquals(date, dto.getDateTime());
    }
}
