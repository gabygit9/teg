package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.ObjectiveDto;
import ar.edu.utn.frc.tup.piii.model.entities.Objective;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectiveMapperTest {

    @Test
    @DisplayName(" toDto debe mapear correctamente un Objetivo a ObjetivoDto")
    void testToDto() {
        // Arrange
        Objective objective = new Objective();
        objective.setId(1);
        objective.setDescription("Conquistar América del Sur");

        // Act
        ObjectiveDto dto = ObjectiveMapper.toDto(objective);

        // Assert
        assertEquals(1, dto.getId());
        assertEquals("Conquistar América del Sur", dto.getDescription());
    }

    @Test
    @DisplayName(" toEntity debe mapear correctamente un ObjetivoDto a Objetivo")
    void testToEntity() {
        // Arrange
        ObjectiveDto dto = new ObjectiveDto();
        dto.setId(2);
        dto.setDescription("Eliminar al jugador rojo");

        // Act
        Objective objective = ObjectiveMapper.toEntity(dto);

        // Assert
        assertEquals(2, objective.getId());
        assertEquals("Eliminar al jugador rojo", objective.getDescription());
    }
}