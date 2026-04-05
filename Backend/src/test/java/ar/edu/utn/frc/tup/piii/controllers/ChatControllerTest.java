package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dto.MessageDTO;
import ar.edu.utn.frc.tup.piii.services.interfaces.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/chat/{gameId}/mensajes - obtener mensajes")
    void testGetMessages_OK() throws Exception {
        MessageDTO message = new MessageDTO();
        message.setId(1);
        message.setGameId(1);
        message.setSenderId(2);
        message.setContent("Hola");
        message.setDateTime(LocalDateTime.now());
        message.setStateActive(true);
        message.setModified(false);

        Mockito.when(chatService.getMessagesPerGame(1)).thenReturn(List.of(message));

        mockMvc.perform(get("/api/v1/chat/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hola"));
    }

    @Test
    @DisplayName("POST /api/v1/chat/send - enviar mensaje")
    void testSendMessage_OK() throws Exception {
        MessageDTO request = new MessageDTO();
        request.setGameId(1);
        request.setSenderId(2);
        request.setContent("Hola");

        MessageDTO response = new MessageDTO();
        response.setId(1);
        response.setGameId(1);
        response.setSenderId(2);
        response.setContent("Hola");
        response.setDateTime(LocalDateTime.now());
        response.setStateActive(true);
        response.setModified(false);

        Mockito.when(chatService.sendMessage(anyInt(), anyInt(), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/v1/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hola"));
    }

    @Test
    @DisplayName("PATCH /api/v1/chat/{messageId}/modificar - modificar mensaje")
    void testModifyMessage_OK() throws Exception {
        MessageDTO request = new MessageDTO();
        request.setGameId(1);
        request.setSenderId(2);
        request.setContent("Nuevo contenido");

        MessageDTO response = new MessageDTO();
        response.setId(1);
        response.setGameId(1);
        response.setSenderId(2);
        response.setContent("Nuevo contenido");
        response.setModified(true);
        response.setDateTime(LocalDateTime.now());
        response.setStateActive(true);

        Mockito.when(chatService.modifyMessage(eq(1), eq(2), eq(1), eq("Nuevo contenido"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/chat/1/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Nuevo contenido"));
    }

    @Test
    @DisplayName("PATCH /api/v1/chat/{gameId}/mensajes/{messageId}/remove/{senderId} - baja lógica")
    void testRemove_OK() throws Exception {
        Mockito.when(chatService.remove(1, 2, 3)).thenReturn(true);

        mockMvc.perform(patch("/api/v1/chat/1/messages/3/remove/2"))
                .andExpect(status().isOk());
    }
}
