package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.dto.BasePlayerDTO;
import ar.edu.utn.frc.tup.piii.dto.MessageDTO;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.HumanPlayer;
import ar.edu.utn.frc.tup.piii.model.entities.Message;
import ar.edu.utn.frc.tup.piii.model.repository.MessageRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.services.interfaces.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChatServiceImplTest {
    @Mock
    private MessageRepository messageRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendMessageDTO() {
        int gameId = 1;
        int emitterId = 1;
        String content = "Nuevo mensaje";

        Game game = new Game();
        game.setId(gameId);

        BasePlayerDTO basePlayerDTO = new BasePlayerDTO();
        basePlayerDTO.setId(emitterId);

        Message messageSaved = new Message();
        messageSaved.setId(10);
        messageSaved.setContent(content);
        messageSaved.setGame(game);
        messageSaved.setSender(new HumanPlayer() {{
            setId(emitterId);
        }});
        messageSaved.setActiveState(true);
        messageSaved.setModified(false);
        messageSaved.setDatetime(LocalDateTime.of(2023, 1, 1, 10, 0));

        when(gameService.findById(gameId)).thenReturn(game);
        when(playerService.findById(emitterId)).thenReturn(Optional.of(basePlayerDTO));
        when(messageRepository.save(any(Message.class))).thenReturn(messageSaved);

        MessageDTO result = chatService.sendMessage(gameId, emitterId, content);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getContent()).isEqualTo("Nuevo mensaje");
        assertThat(result.getGameId()).isEqualTo(1);
        assertThat(result.getSenderId()).isEqualTo(1);
        assertThat(result.isStateActive()).isTrue();
        assertThat(result.isModified()).isFalse();
        assertThat(result.getDateTime()).isEqualTo(LocalDateTime.of(2023, 1, 1, 10, 0)); // JSON: dateTime
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void getMessagesPerGame() {
        int gameId = 1;
        int emitterId = 2;
        LocalDateTime date = LocalDateTime.of(2023, 6, 1, 12, 0);

        Game game = new Game();
        game.setId(gameId);

        HumanPlayer emitter = new HumanPlayer();
        emitter.setId(emitterId);

        Message message = new Message();
        message.setId(10);
        message.setGame(game);
        message.setSender(emitter);
        message.setContent("Nuevo msg");
        message.setActiveState(true);
        message.setModified(false);
        message.setDatetime(date);

        when(gameService.findById(gameId)).thenReturn(game);
        when(messageRepository.findByGameIdOrderByHourAsc(gameId)).thenReturn(List.of(message));

        List<MessageDTO> result = chatService.getMessagesPerGame(gameId);

        assertEquals(1, result.size());
        MessageDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(10);
        assertThat(dto.getGameId()).isEqualTo(gameId);
        assertThat(dto.getSenderId()).isEqualTo(emitterId);
        assertThat(dto.getContent()).isEqualTo("Nuevo msg");
        assertThat(dto.isStateActive()).isTrue();
        assertThat(dto.isModified()).isFalse();
        assertThat(dto.getDateTime()).isEqualTo(date);
        verify(gameService).findById(gameId);
        verify(messageRepository).findByGameIdOrderByHourAsc(gameId);
    }

    @Test
    void getMessagesPerGameNoExists() {

        int gameId = 1099;
        when(gameService.findById(gameId)).thenReturn(null);

        List<MessageDTO> result = chatService.getMessagesPerGame(gameId);

        assertTrue(result.isEmpty());
        verify(gameService).findById(gameId);
        verifyNoInteractions(messageRepository);
    }

    @Test
    void remove() {
        int gameId = 1;
        int emitterId = 5;
        int messageId = 10;

        Game game = new Game();
        game.setId(gameId);

        HumanPlayer emitter = new HumanPlayer();
        emitter.setId(emitterId);

        Message message = new Message();
        message.setId(messageId);
        message.setGame(game);
        message.setSender(emitter);
        message.setActiveState(true);

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        boolean result = chatService.remove(gameId, emitterId, messageId);

        assertTrue(result);
        assertFalse(message.isActiveState());
        verify(messageRepository).save(message);
    }

    @Test
    void remove_messageNotFound() {
        int messageId = 10;
        Message message = new Message();
        message.setId(messageId);
        message.setSender(new HumanPlayer() {{
            setId(1999);
        }});
        message.setActiveState(true);

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        boolean rtdo = chatService.remove(1, 5, messageId);

        assertFalse(rtdo);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void remove_messageInactive() {
        int messageId = 10;
        Message message = new Message();
        message.setId(messageId);
        message.setSender(new HumanPlayer() {{
            setId(5);
        }});
        message.setActiveState(false);

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        boolean result = chatService.remove(1, 5, messageId);

        assertFalse(result);
        verify(messageRepository, never()).save(any());
    }


    @Test
    void modifyMessage() {
        int gameId = 1;
        int emitterId = 2;
        int messageId = 10;
        String newContent = "Mensaje editado";

        Game game = new Game();
        game.setId(gameId);

        HumanPlayer emitter = new HumanPlayer();
        emitter.setId(emitterId);

        Message message = new Message();
        message.setId(messageId);
        message.setGame(game);
        message.setSender(emitter);
        message.setContent("editado");
        message.setActiveState(true);
        message.setModified(false);
        message.setDatetime(LocalDateTime.of(2023, 1, 1, 12, 0));

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageDTO dto = chatService.modifyMessage(gameId, emitterId, messageId, newContent);

        assertNotNull(dto);
        assertEquals(messageId, dto.getId());
        assertEquals(newContent, dto.getContent());
        assertTrue(dto.isModified());
        assertEquals(emitterId, dto.getSenderId());
        assertEquals(gameId, dto.getGameId());
        assertTrue(dto.getDateTime().isAfter(LocalDateTime.of(2023, 1, 1, 12, 0)));
        verify(messageRepository).save(message);
    }


    @Test
    void modifyMessage_gameEmitterNotFound() {
        int messageId = 10;

        Message message = new Message();
        message.setId(messageId);
        message.setGame(new Game() {{
            setId(1999);
        }});
        message.setSender(new HumanPlayer() {{
            setId(1888);
        }});

        when(messageRepository.findById(messageId)).thenReturn(Optional.of(message));

        MessageDTO dto = chatService.modifyMessage(1, 5, messageId, "nuevo msg");

        assertNull(dto);
        verify(messageRepository, never()).save(any());
    }

}