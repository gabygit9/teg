package ar.edu.utn.frc.tup.piii.bot;

import ar.edu.utn.frc.tup.piii.model.entities.LevelBot;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
class BehaviorBotFactoryTest {

    @Test
    void getBehavior_levelExpert_returnBotExpertStrategy() {
        // Arrange
        LevelBot levelBot = mock(LevelBot.class);
        when(levelBot.getName()).thenReturn(LevelBot.EXPERT);

        // Act
        IBehaviorBot result = BehaviorBotFactory.getBehavior(levelBot);

        // Assert
        assertThat(result).isInstanceOf(BotExpertStrategy.class);
    }

    @Test
    void getBehavior_unknownLevel_throwIllegalArgumentException() {
        // Arrange
        LevelBot levelBot = mock(LevelBot.class);
        when(levelBot.getName()).thenReturn("NIVEL_INEXISTENTE");

        // Act & Assert
        assertThatThrownBy(() -> BehaviorBotFactory.getBehavior(levelBot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nivel de bot desconocido NIVEL_INEXISTENTE");
    }
}