package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.ResultAttackDto;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.services.interfaces.*;
import ar.edu.utn.frc.tup.piii.util.RegisterMessageEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CombatServiceImplTest {

    @Mock
    private PlayerService playerService;

    @Mock
    private CountryGameService countryGameService;

    @Mock
    private ContinentService continentService;

    @Mock
    private HistoryService historyService;

    @Mock
    private RegisterMessageEvent registerMessageEvent;

    @Mock
    private CardService cardService;

    @InjectMocks
    private CombatServiceImpl combatService;

    private PlayerGame attackerPlayer;
    private PlayerGame defenderPlayer;
    private CountryGame attackerCountry;
    private CountryGame defenderCountry;
    private Game game;
    private Country country1, country2;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.setId(1);

        country1 = new Country();
        country1.setId(1);
        country1.setName("Argentine");

        country2 = new Country();
        country2.setId(2);
        country2.setName("Brazil");

        HumanPlayer player1 = new HumanPlayer();
        player1.setName("Jugador1");

        HumanPlayer player2 = new HumanPlayer();
        player2.setName("Jugador2");

        attackerPlayer = new PlayerGame();
        attackerPlayer.setId(1);
        attackerPlayer.setPlayer(player1);
        attackerPlayer.setGame(game);

        defenderPlayer = new PlayerGame();
        defenderPlayer.setId(2);
        defenderPlayer.setPlayer(player2);
        defenderPlayer.setGame(game);

        attackerCountry = new CountryGame();
        attackerCountry.setCountry(country1);
        attackerCountry.setGame(game);
        attackerCountry.setPlayerGame(attackerPlayer);
        attackerCountry.setAmountArmies(5);

        defenderCountry = new CountryGame();
        defenderCountry.setCountry(country2);
        defenderCountry.setGame(game);
        defenderCountry.setPlayerGame(defenderPlayer);
        defenderCountry.setAmountArmies(3);
    }

    @Test
    void testConquerCountry_Success() {

        when(continentService.findCountryById(1)).thenReturn(country1);
        when(countryGameService.findByCountryAndGameId(country1, 1)).thenReturn(defenderCountry);


        combatService.conquerCountry(1, attackerPlayer);


        assertEquals(attackerPlayer, defenderCountry.getPlayerGame());
        verify(countryGameService).save(defenderCountry);
        verify(cardService).markConquer(attackerPlayer.getId());
    }

    @Test
    void testConquerCountry_NullPlayer() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.conquerCountry(1, null));
        assertEquals("Jugador no válido", exception.getMessage());
    }

    @Test
    void testConquerCountryNotFound() {

        when(continentService.findCountryById(1)).thenReturn(null);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.conquerCountry(1, attackerPlayer));
        assertEquals("Pais no encontrado", exception.getMessage());
    }

    @Test
    void testConquerCountryGameNotFound() {

        when(continentService.findCountryById(1)).thenReturn(country1);
        when(countryGameService.findByCountryAndGameId(country1, 1)).thenReturn(null);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.conquerCountry(1, attackerPlayer));
        assertEquals("el pais de la partida no fue encontrado", exception.getMessage());
    }

    @Test
    void testAnnounceAttack_ValidCountries() {
        CountryGameId attackerId = new CountryGameId(1, 1);
        CountryGameId defenderId = new CountryGameId(2, 1);

        when(countryGameService.findById(1, 1)).thenReturn(attackerCountry);
        when(countryGameService.findById(2, 1)).thenReturn(defenderCountry);

        assertDoesNotThrow(() -> combatService.announceAttack(attackerId, defenderId));

        verify(countryGameService).findById(1, 1);
        verify(countryGameService).findById(2, 1);
    }

    @Test
    void testAnnounceAttack_SameCountries() {

        CountryGameId sameId = new CountryGameId(1, 1);

        combatService.announceAttack(sameId, sameId);

        verify(countryGameService, never()).findById(anyInt(), anyInt());
    }

    @Test
    void testAttackSuccess() {

        when(countryGameService.findById(1, 1)).thenReturn(attackerCountry);
        when(countryGameService.findById(2, 1)).thenReturn(defenderCountry);
        when(countryGameService.isBordering(1, 2)).thenReturn(true);
        when(registerMessageEvent.attackArmiesRegistry(any())).thenReturn("Mensaje de ataque");


        ResultAttackDto result = combatService.attack(1, 1, 2, 2);


        assertNotNull(result);
        assertNotNull(result.getAttackerDice());
        assertNotNull(result.getDeffenderDice());
        assertFalse(result.isWasConquest()); // No debería haber conquista con estos ejércitos
        verify(countryGameService, times(2)).save(any(CountryGame.class));
        verify(historyService).registerEvent(eq(game), anyString());
    }

    @Test
    void testAttack_IDsInvalids() {

        assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(0, 1, 2, 1));
        assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(1, 0, 2, 1));
        assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(1, 1, 0, 1));
    }

    @Test
    void testAttack_SameCountry() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(1, 1, 1, 1));
        assertEquals("Un país no puede atacarse a sí mismo", exception.getMessage());
    }

    @Test
    void testAttack_AttackerNotFound() {

        when(countryGameService.findById(1, 1)).thenReturn(null);


        assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(1, 1, 2, 1));
    }

    @Test
    void testAttack_DefenderNotFound() {

        when(countryGameService.findById(1, 1)).thenReturn(attackerCountry);
        when(countryGameService.findById(2, 1)).thenReturn(null);


        assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(1, 1, 2, 1));
    }

    @Test
    void testAttack_SamePlayer() {

        defenderCountry.setPlayerGame(attackerPlayer); // Mismo jugador
        when(countryGameService.findById(1, 1)).thenReturn(attackerCountry);
        when(countryGameService.findById(2, 1)).thenReturn(defenderCountry);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(1, 1, 2, 1));
        assertEquals("No puedes atacar a tu propio país", exception.getMessage());
    }

    @Test
    void testAttack_CountriesNoBordering() {

        when(countryGameService.findById(1, 1)).thenReturn(attackerCountry);
        when(countryGameService.findById(2, 1)).thenReturn(defenderCountry);
        when(countryGameService.isBordering(1, 2)).thenReturn(false);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(1, 1, 2, 1));
        assertEquals("Los países no son limítrofes", exception.getMessage());
    }

    @Test
    void testAttack_InsufficientsArmies() {

        attackerCountry.setAmountArmies(1); // Solo 1 ejército
        when(countryGameService.findById(1, 1)).thenReturn(attackerCountry);
        when(countryGameService.findById(2, 1)).thenReturn(defenderCountry);
        when(countryGameService.isBordering(1, 2)).thenReturn(true);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(1, 1, 2, 1));
        assertEquals("El país atacante no tiene suficientes ejércitos para atacar", exception.getMessage());
    }

    @Test
    void testAttack_InvalidsDice() {

        attackerCountry.setAmountArmies(2); // Solo permite 1 dado
        when(countryGameService.findById(1, 1)).thenReturn(attackerCountry);
        when(countryGameService.findById(2, 1)).thenReturn(defenderCountry);
        when(countryGameService.isBordering(1, 2)).thenReturn(true);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.attack(1, 1, 2, 2));
        assertEquals("Número inválido de dados para el atacante. Máximo permitido: 1", exception.getMessage());
    }


    @Test
    void testRegroupArmy_Success() {

        CountryGame originCountry = new CountryGame();
        originCountry.setCountry(country1);
        originCountry.setGame(game);
        originCountry.setPlayerGame(attackerPlayer);
        originCountry.setAmountArmies(5);

        CountryGame destinationCountry = new CountryGame();
        destinationCountry.setCountry(country2);
        destinationCountry.setGame(game);
        destinationCountry.setPlayerGame(attackerPlayer);
        destinationCountry.setAmountArmies(3);

        when(playerService.findPlayerGameById(1)).thenReturn(Optional.of(attackerPlayer));
        when(countryGameService.findById(1, 1)).thenReturn(originCountry);
        when(countryGameService.findById(2, 1)).thenReturn(destinationCountry);
        when(countryGameService.isBordering(1, 2)).thenReturn(true);
        when(registerMessageEvent.moveArmiesRegistry(any())).thenReturn("Mensaje de movimiento");


        combatService.regroupArmy(1, 1, 2, 2);


        assertEquals(3, originCountry.getAmountArmies());
        assertEquals(5, destinationCountry.getAmountArmies());
        verify(countryGameService).save(originCountry);
        verify(countryGameService).save(destinationCountry);
        verify(historyService).registerEvent(eq(game), anyString());
    }

    @Test
    void testRegroupArmy_PlayerNotFound() {

        when(playerService.findPlayerGameById(1)).thenReturn(Optional.empty());


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.regroupArmy(1, 1, 2, 2));
        assertEquals("Jugador no encontrado", exception.getMessage());
    }



    @Test
    void testRegroupArmy_CountriesNoBordering() {

        CountryGame originCountry = new CountryGame();
        originCountry.setCountry(country1);
        originCountry.setGame(game);
        originCountry.setPlayerGame(attackerPlayer);
        originCountry.setAmountArmies(5);

        CountryGame destinationCountry = new CountryGame();
        destinationCountry.setCountry(country2);
        destinationCountry.setGame(game);
        destinationCountry.setPlayerGame(attackerPlayer);
        destinationCountry.setAmountArmies(3);

        when(playerService.findPlayerGameById(1)).thenReturn(Optional.of(attackerPlayer));
        when(countryGameService.findById(1, 1)).thenReturn(originCountry);
        when(countryGameService.findById(2, 1)).thenReturn(destinationCountry);
        when(countryGameService.isBordering(1, 2)).thenReturn(false);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.regroupArmy(1, 1, 2, 2));
        assertEquals("Los países no son limítrofes.", exception.getMessage());
    }

    @Test
    void testRegroupArmy_DifferentPlayer() {

        CountryGame originCountry = new CountryGame();
        originCountry.setCountry(country1);
        originCountry.setGame(game);
        originCountry.setPlayerGame(attackerPlayer);
        originCountry.setAmountArmies(5);

        CountryGame destinationCountry = new CountryGame();
        destinationCountry.setCountry(country2);
        destinationCountry.setGame(game);
        destinationCountry.setPlayerGame(defenderPlayer);
        destinationCountry.setAmountArmies(3);

        when(playerService.findPlayerGameById(1)).thenReturn(Optional.of(attackerPlayer));
        when(countryGameService.findById(1, 1)).thenReturn(originCountry);
        when(countryGameService.findById(2, 1)).thenReturn(destinationCountry);
        when(countryGameService.isBordering(1, 2)).thenReturn(true);


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> combatService.regroupArmy(1, 1, 2, 2));
        assertEquals("Ambos países deben pertenecer al mismo jugador", exception.getMessage());
    }


    @Test
    void testAttack_ErrorAtSave() {

        when(countryGameService.findById(1, 1)).thenReturn(attackerCountry);
        when(countryGameService.findById(2, 1)).thenReturn(defenderCountry);
        when(countryGameService.isBordering(1, 2)).thenReturn(true);
        when(registerMessageEvent.attackArmiesRegistry(any())).thenReturn("Mensaje de ataque");
        doThrow(new RuntimeException("Error de BD")).when(countryGameService).save(any(CountryGame.class));


        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> combatService.attack(1, 1, 2, 1));
        assertEquals("Error al guardar el resultado del ataque", exception.getMessage());
    }
}