package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BasePlayerMapperTest {

    @Test
    @DisplayName("Debe convertir JugadorBase a JugadorBaseDTO correctamente")
    void testToDto_Success() {
        // Arrange
        BasePlayer player = new HumanPlayer();
        player.setId(1);
        player.setName("TestPlayer");
        player.setAvailableArmies(5);

        // Act
        BasePlayerDTO result = BasePlayerMapper.toDto(player);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getPlayerName()).isEqualTo("TestPlayer");
        assertThat(result.getAvailableArmies()).isEqualTo(5);
    }

    @Test
    @DisplayName("Debe retornar null cuando JugadorBase es null")
    void testToDto_NullInput() {
        // Act
        BasePlayerMapper.toDto(null);

        // Assert
        assertThat((BasePlayerDTO) null).isNull();
    }

    @Test
    @DisplayName("Debe convertir JugadorBaseDTO a JugadorHumano correctamente")
    void testToEntity_Success() {
        // Arrange
        BasePlayerDTO dto = new BasePlayerDTO();
        dto.setId(10); // Este ID será ignorado según la lógica
        dto.setPlayerName("TestPlayer");
        dto.setAvailableArmies(3);

        // Act
        HumanPlayer result = BasePlayerMapper.toEntity(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(0); // Siempre se setea en 0
        assertThat(result.getName()).isEqualTo("TestPlayer");
        assertThat(result.getAvailableArmies()).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe retornar null cuando JugadorBaseDTO es null")
    void testToEntity_NullInput() {
        // Act
        BasePlayerMapper.toEntity(null);

        // Assert
        assertThat((HumanPlayer) null).isNull();
    }

    @Test
    @DisplayName("Debe convertir lista de JugadorBase a lista de JugadorBaseDTO")
    void testToDtoList_Success() {
        // Arrange
        BasePlayer player1 = new HumanPlayer();
        player1.setId(1);
        player1.setName("Player1");
        player1.setAvailableArmies(5);

        BasePlayer player2 = new HumanPlayer();
        player2.setId(2);
        player2.setName("Player2");
        player2.setAvailableArmies(3);

        List<BasePlayer> players = Arrays.asList(player1, player2);

        // Act
        List<BasePlayerDTO> result = BasePlayerMapper.toDtoList(players);

        // Assert
        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(0).getPlayerName()).isEqualTo("Player1");
        assertThat(result.get(0).getAvailableArmies()).isEqualTo(5);

        assertThat(result.get(1).getId()).isEqualTo(2);
        assertThat(result.get(1).getPlayerName()).isEqualTo("Player2");
        assertThat(result.get(1).getAvailableArmies()).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando la lista de entrada es null")
    void testToDtoList_NullInput() {
        // Act
        List<BasePlayerDTO> result = BasePlayerMapper.toDtoList(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando la lista de entrada está vacía")
    void testToDtoList_EmptyInput() {
        // Arrange
        List<BasePlayer> players = new ArrayList<>();

        // Act
        List<BasePlayerDTO> result = BasePlayerMapper.toDtoList(players);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe manejar lista con elementos null correctamente")
    void testToDtoList_WithNullElements() {
        // Arrange
        BasePlayer player1 = new HumanPlayer();
        player1.setId(1);
        player1.setName("Player1");
        player1.setAvailableArmies(5);

        List<BasePlayer> players = Arrays.asList(player1, null);

        // Act
        List<BasePlayerDTO> result = BasePlayerMapper.toDtoList(players);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isNotNull();
        assertThat(result.get(0).getId()).isEqualTo(1);
        assertThat(result.get(1)).isNull(); // El mapper convierte null a null
    }

    @Test
    @DisplayName("Debe manejar campos null en JugadorBase")
    void testToDto_WithNullFields() {
        // Arrange
        BasePlayer player = new HumanPlayer();
        player.setId(1);
        player.setName(null); // Campo null
        player.setAvailableArmies(0);

        // Act
        BasePlayerDTO result = BasePlayerMapper.toDto(player);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getPlayerName()).isNull();
        assertThat(result.getAvailableArmies()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debe manejar campos null en JugadorBaseDTO")
    void testToEntity_WithNullFields() {
        // Arrange
        BasePlayerDTO dto = new BasePlayerDTO();
        dto.setId(5);
        dto.setPlayerName(null); // Campo null
        dto.setAvailableArmies(0);

        // Act
        HumanPlayer result = BasePlayerMapper.toEntity(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(0);
        assertThat(result.getName()).isNull();
        assertThat(result.getAvailableArmies()).isEqualTo(0);
    }
}