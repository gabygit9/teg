package ar.edu.utn.frc.tup.piii.util;

import ar.edu.utn.frc.tup.piii.dto.RegisterMessageEventDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegisterMessageEventTest {

    private RegisterMessageEvent registerMessageEvent;

    @BeforeEach
    void setUp() {
        registerMessageEvent = new RegisterMessageEvent();
    }

    private RegisterMessageEventDTO mockDtoMovement() {
        RegisterMessageEventDTO dto = mock(RegisterMessageEventDTO.class);

        CountryGame origin = mock(CountryGame.class);
        CountryGame destine = mock(CountryGame.class);

        PlayerGame playerGameOrigin = mock(PlayerGame.class);
        BasePlayer playerOrigin = mock(BasePlayer.class);
        PlayerGame playerGameDestine = mock(PlayerGame.class);
        BasePlayer playerDestination = mock(BasePlayer.class);

        Country countryOrigin = mock(Country.class);
        Country countryDestination = mock(Country.class);

        when(dto.getAmountTroops()).thenReturn(5);

        when(dto.getOriginCountry()).thenReturn(origin);
        when(origin.getPlayerGame()).thenReturn(playerGameOrigin);
        when(playerGameOrigin.getPlayer()).thenReturn(playerOrigin);
        when(playerOrigin.getName()).thenReturn("Usuario");
        when(origin.getCountry()).thenReturn(countryOrigin);
        when(countryOrigin.getName()).thenReturn("california");

        when(dto.getDestinationCountry()).thenReturn(destine);
        when(destine.getPlayerGame()).thenReturn(playerGameDestine);
        when(playerGameDestine.getPlayer()).thenReturn(playerDestination);
        when(playerDestination.getName()).thenReturn("Admin");
        when(destine.getCountry()).thenReturn(countryDestination);
        when(countryDestination.getName()).thenReturn("oregon");

        return dto;
    }

    @Test
    void testMoveArmiesRegistry() {
        RegisterMessageEventDTO dto = mockDtoMovement();
        String result = registerMessageEvent.moveArmiesRegistry(dto);
        String expected = "El jugador Usuario movió 5 tropas desde california hacia oregon para reagrupar fuerzas.";
        assertEquals(expected, result);
    }

    @Test
    void conquerCountryRegistryTest() {
        RegisterMessageEventDTO dto = mock(RegisterMessageEventDTO.class);

        CountryGame origin = mock(CountryGame.class);
        CountryGame destiny = mock(CountryGame.class);
        PlayerGame playerGameOrigin = mock(PlayerGame.class);
        PlayerGame playerGameDestination = mock(PlayerGame.class);
        BasePlayer playerOrigin = mock(BasePlayer.class);
        BasePlayer playerDestination = mock(BasePlayer.class);
        Country countryDestine = mock(Country.class);

        when(dto.getOriginCountry()).thenReturn(origin);
        when(origin.getPlayerGame()).thenReturn(playerGameOrigin);
        when(playerGameOrigin.getPlayer()).thenReturn(playerOrigin);
        when(playerOrigin.getName()).thenReturn("Usuario");

        when(dto.getDestinationCountry()).thenReturn(destiny);
        when(destiny.getCountry()).thenReturn(countryDestine);
        when(countryDestine.getName()).thenReturn("oregon");
        when(destiny.getPlayerGame()).thenReturn(playerGameDestination);
        when(playerGameDestination.getPlayer()).thenReturn(playerDestination);
        when(playerDestination.getName()).thenReturn("Admin");

        String result = registerMessageEvent.conquerCountryRegistry(dto);
        String expected = "El jugador Usuario ha conquistado oregon perteneciente al jugador Admin";
        assertEquals(expected, result);
    }


    @Test
    void attackArmyRegistryTest() {
        RegisterMessageEventDTO dto = mockDtoMovement();

        String result = registerMessageEvent.attackArmiesRegistry(dto);
        String expected = "El jugador Usuario ha atacado al jugador Admin con 5 tropas, desde california a oregon";
        assertEquals(expected, result);
    }

    @Test
    void testStartGameRegistry() {
        Game game = new Game();
        game.setId(1);
        String result = registerMessageEvent.startGameRegistry(game);
        assertEquals("La partida nro°1 ha sido iniciada.", result);
    }

    @Test
    void continueGameRegistryTest() {
        Game game = new Game();
        game.setId(2);
        String result = registerMessageEvent.continueGameRegistry(game);
        assertEquals("La partida nro°2 ha sido reanudada.", result);
    }

    @Test
    void testFinishGameRegistry() {
        Game game = new Game();
        game.setId(3);
        String result = registerMessageEvent.finishGameRegistry(game);
        assertEquals("La partida nro°3 ha sido finalizada.", result);
    }

    @Test
    void initHostilitiesRegistryTest() {
        String result = registerMessageEvent.startHostilitiesGameRegistry(4);
        assertEquals("La partida nro°4 ha entrado en la fase de hostilidades.", result);
    }

    @Test
    void moveStateRegistryTest() {
        String result = registerMessageEvent.moveStateGameRegistry(5);
        assertEquals("La partida nro°5 avanzó al siguiente estado.", result);
    }

    @Test
    void exchangeCardRegistryTest() {
        PlayerGame playerGame = mock(PlayerGame.class);
        BasePlayer player = mock(BasePlayer.class);
        when(playerGame.getPlayer()).thenReturn(player);
        when(player.getName()).thenReturn("Pepito");

        String result = registerMessageEvent.exchangeCardsRegister(playerGame, 7);
        assertEquals("El player Pepito ha canjeado tarjetas y recibió 7 ejércitos.", result);
    }

    @Test
    void testChangeStateGameRegistry() {
        Game game = new Game();
        game.setId(6);

        StateGameEntity newState = mock(StateGameEntity.class);
        when(newState.getDescription()).thenReturn("En juego");

        String result = registerMessageEvent.changeStateGameRegistry(game, newState);
        assertEquals("La partida nro°6 cambió su estado a 'En juego'.", result);
    }

    @Test
    void increaseArmyRegistryEST() {
        BasePlayer player = mock(BasePlayer.class);
        when(player.getName()).thenReturn("Usuario");

        String result = RegisterMessageEvent.increaseArmies(player, "california", 10);
        assertEquals("El player 'Usuario' agregó 10 ejércitos al país 'california'", result);
    }

    @Test
    void receiveCardRegistryTest() {
        BasePlayer player = mock(BasePlayer.class);
        when(player.getName()).thenReturn("Admin");

        String result = RegisterMessageEvent.receiveCard(player, "canada");
        assertEquals("El player Admin recibió una tarjeta del país canada.", result);
    }

    @Test
    void getArmyRegistryTest() {
        BasePlayer player = mock(BasePlayer.class);
        when(player.getName()).thenReturn("Usuario");

        String result = RegisterMessageEvent.giveArmiesPerCard(player, "mexico");
        assertEquals("El player Usuario obtuvo 2 ejércitos adicionales por poseer el país mexico al usar una tarjeta.", result);
    }

    @Test
    void changePhaseRegistryTest() {
        BasePlayer player = mock(BasePlayer.class);
        when(player.getName()).thenReturn("Usuario");
        TurnPhase phase = TurnPhase.INCORPORATION;

        String result = RegisterMessageEvent.changePhase(player, phase);
        assertEquals("El player Usuario pasó a la phase INCORPORACION", result);
    }

    @Test
    void putArmyRegistryTest() {
        BasePlayer player = mock(BasePlayer.class);
        Country country = mock(Country.class);
        when(player.getName()).thenReturn("Usuario");
        when(country.getName()).thenReturn("california");

        String result = RegisterMessageEvent.putArmy(player, country, 4);
        assertEquals("El player Usuario colocó 4 ejército(s) en california.", result);
    }

    @Test
    void startTurnRegistryTest() {
        BasePlayer player = mock(BasePlayer.class);
        when(player.getName()).thenReturn("Usuario");

        String result = RegisterMessageEvent.startTurn(player);
        assertEquals("El player Usuario ha iniciado su turno.", result);
    }

}
