package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.SaveGameRequestDTO;
import ar.edu.utn.frc.tup.piii.dto.mementosDTOs.GameStateMementoDTO;
import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameMementoServiceImplTest {

    @Mock
    private GameMementoRepository gameMementoRepository;
    @Mock
    private PlayerGameRepository playerGameRepository;
    @Mock
    private CountryGameRepository countryGameRepository;
    @Mock
    private TurnRepository turnRepository;
    @Mock
    private PactRepository pactRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private HistoryRepository historyEventRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private ColorRepository colorRepository;
    @Mock
    private ObjectiveRepository objectiveRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private CountryRepository countryRepository;
    @Mock
    private PactTypeRepository pactTypeRepository;

    @InjectMocks
    private GameMementoServiceImpl service;

    @Test
    void saveMementoTest() throws JsonProcessingException {
        Game game = new Game();
        game.setId(1);
        game.setStartDate(LocalDateTime.now());

        when(playerGameRepository.findByGame_Id(1)).thenReturn(Collections.emptyList());
        when(countryGameRepository.findByGame(game)).thenReturn(Collections.emptyList());
        when(turnRepository.findByGame_Id(1)).thenReturn(Collections.emptyList());
        when(pactRepository.findByGame_Id(1)).thenReturn(Collections.emptyList());
        when(messageRepository.findByGame_Id(1)).thenReturn(Collections.emptyList());
        when(historyEventRepository.findByGame_Id(1)).thenReturn(Collections.emptyList());

        ArgumentCaptor<GameMemento> captor = ArgumentCaptor.forClass(GameMemento.class);
        when(gameMementoRepository.save(any(GameMemento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GameMemento memento = service.saveMementoComplete(game, 1);

        assertNotNull(memento);
        assertEquals(game, memento.getGame());
        assertEquals(1, memento.getVersion());
        assertNotNull(memento.getStateSerialized());
        assertTrue(memento.getStateSerialized().contains("\"id\":1"));

        verify(gameMementoRepository).save(captor.capture());
    }

    @Test
    void getStatesGameTest() {
        int gameId = 1;
        List<GameMemento> list = List.of(new GameMemento());
        when(gameMementoRepository.findByGameId(gameId)).thenReturn(list);

        List<GameMemento> result = service.getStatesByGame(gameId);

        assertEquals(list, result);
        verify(gameMementoRepository).findByGameId(gameId);
    }

    @Test
    void getLastStateTest() {
        int gameId = 1;
        GameMemento memento = new GameMemento();
        when(gameMementoRepository.findTopByGameIdOrderByDateTimeDesc(gameId)).thenReturn(memento);

        GameMemento result = service.getLastState(gameId);

        assertEquals(memento, result);
        verify(gameMementoRepository).findTopByGameIdOrderByDateTimeDesc(gameId);
    }


    @Test
    void restoreSuccessTest() {
        int mementoId = 10;

        Game game = new Game();
        game.setId(1);

        String json = """
    {
      "game": { "id": 1 },
      "players": [
        {
          "id": 1,
          "basePlayerId": 1,
          "colorId": 1,
          "objectiveId": 1,
          "objectiveAchieved": false,
          "orderTurn": 1,
          "isTurn": false,
          "active": true
        }
      ],
      "countries": [
        {
          "id": 1,
          "countryId": 1,
          "playerGameId": 1,
          "armyAmount": 5
        }
      ],
      "turns": [],
      "pacts": [],
      "messages": [],
      "history": []
    }
    """;

        GameMemento memento = new GameMemento();
        memento.setMementoId(mementoId);
        memento.setStateSerialized(json);
        memento.setGame(game);

        when(gameMementoRepository.findById(mementoId)).thenReturn(Optional.of(memento));
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));

        HumanPlayer playerBaseMock = new HumanPlayer();
        playerBaseMock.setId(1);
        when(playerRepository.findById(1)).thenReturn(Optional.of(playerBaseMock));

        Color colorMock = new Color();
        colorMock.setId(1);
        when(colorRepository.findById(1)).thenReturn(Optional.of(colorMock));

        Objective objectiveMock = new Objective();
        objectiveMock.setId(1);
        when(objectiveRepository.findById(1)).thenReturn(Optional.of(objectiveMock));

        Country countryMock = new Country();
        countryMock.setId(1);
        when(countryRepository.findById(1)).thenReturn(Optional.of(countryMock));

        when(playerGameRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(countryGameRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        GameStateMementoDTO result = service.restoreAndPersistState(mementoId);

        assertNotNull(result);
        assertEquals(game.getId(), result.getGame().getId());

        verify(pactRepository).deleteByGameId(game.getId());
        verify(messageRepository).deleteByGameId(game.getId());
        verify(historyEventRepository).deleteByGameId(game.getId());
        verify(turnRepository).deleteByGameId(game.getId());
        verify(countryGameRepository).deleteByGameId(game.getId());
        verify(playerGameRepository).deleteByGameId(game.getId());
    }


    @Test
    void restoreWithoutMementoTest() {
        int mementoId = 999;

        when(gameMementoRepository.findById(mementoId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.restoreAndPersistState(mementoId));
        assertTrue(ex.getMessage().contains("Memento no encontrado"));
    }

    @Test
    void restoreJsonInvalidTest() {
        int mementoId = 20;

        GameMemento memento = new GameMemento();
        memento.setMementoId(mementoId);
        memento.setStateSerialized("JSON_INVALIDO");
        memento.setGame(new Game());

        when(gameMementoRepository.findById(mementoId)).thenReturn(Optional.of(memento));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.restoreAndPersistState(mementoId));
        assertTrue(ex.getMessage().contains("Error al deserializar el estado JSON"));
    }

    @Test
    void listGamesWithoutTest() {
        when(gameRepository.findAll()).thenReturn(Collections.emptyList());

        List<?> result = service.listSaveGames();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void listGamesTest() {
        Game p1 = new Game();
        p1.setId(1);
        Game p2 = new Game();
        p2.setId(2);

        GameMemento memento = new GameMemento();
        memento.setMementoId(100);
        memento.setDateTime(LocalDateTime.now());

        when(gameRepository.findAll()).thenReturn(List.of(p1, p2));
        when(gameMementoRepository.findTopByGameOrderByDateTimeDesc(p1)).thenReturn(Optional.of(memento));
        when(gameMementoRepository.findTopByGameOrderByDateTimeDesc(p2)).thenReturn(Optional.empty());

        List<SaveGameRequestDTO> result = service.listSaveGames();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getGameId());
        assertEquals(100, result.get(0).getLastMementoId());
    }









}
