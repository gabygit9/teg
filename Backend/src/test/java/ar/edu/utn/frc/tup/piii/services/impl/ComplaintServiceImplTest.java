package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.*;
import ar.edu.utn.frc.tup.piii.model.repository.ComplaintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceImplTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    private Complaint complaint;
    private Game game;
    private BasePlayer accuser;
    private BasePlayer accused;
    private StateGameEntity stateGameEntity;
    private CommunicationType communicationType;
    private Objective objective;
    private LocalDateTime dateTime;

    @BeforeEach
    void setUp() {
        dateTime = LocalDateTime.now();

        // Crear entidades auxiliares
        stateGameEntity = new StateGameEntity();
        stateGameEntity.setId(1);
        stateGameEntity.setDescription("In Course");

        communicationType = new CommunicationType();
        communicationType.setId(1);
        communicationType.setDescription("Chat");

        objective = new Objective();
        objective.setId(1);
        objective.setDescription("Conquistar el mundo");

        // Crear partida
        game = new Game(1, dateTime, stateGameEntity, communicationType, objective);

        // Crear jugadores usando una implementación concreta de JugadorBase
        accuser = new ConcretePlayer(1, "Jugador1", 10);
        accused = new ConcretePlayer(2, "Jugador2", 15);

        // Crear denuncia
        complaint = new Complaint(1, game, accuser, accused, "Uso de lenguaje inapropiado", dateTime);
    }

    @Test
    void save_WhenComplaintIsSavedSuccessfully_ShouldReturnTrue() {

        Complaint complaintNew = new Complaint(0, game, accuser, accused, "Motivo test", dateTime);
        Complaint complaintSaved = new Complaint(1, game, accuser, accused, "Motivo test", dateTime);

        when(complaintRepository.save(complaintNew)).thenReturn(complaintSaved);


        boolean result = complaintService.save(complaintNew);


        assertTrue(result);
        verify(complaintRepository).save(complaintNew);
    }

    @Test
    void save_WhenComplaintIsNotSaved_ShouldReturnFalse() {

        Complaint complaintNew = new Complaint(0, game, accuser, accused, "Motivo test", dateTime);
        Complaint complaintSaved = new Complaint(0, game, accuser, accused, "Motivo test", dateTime);

        when(complaintRepository.save(complaintNew)).thenReturn(complaintSaved);


        boolean result = complaintService.save(complaintNew);


        assertFalse(result);
        verify(complaintRepository).save(complaintNew);
    }

    @Test
    void save_WhenOccursException_ShouldThrowException() {

        Complaint complaintNew = new Complaint(0, game, accuser, accused, "Motivo test", dateTime);

        when(complaintRepository.save(complaintNew)).thenThrow(new RuntimeException("Error de base de datos"));


        assertThrows(RuntimeException.class, () -> complaintService.save(complaintNew));
        verify(complaintRepository).save(complaintNew);
    }

    @Test
    void update_WhenComplaintExists_ShouldReturnTrue() {

        when(complaintRepository.existsById(1)).thenReturn(true);
        when(complaintRepository.save(complaint)).thenReturn(complaint);


        boolean result = complaintService.update(complaint);


        assertTrue(result);
        verify(complaintRepository).existsById(1);
        verify(complaintRepository).save(complaint);
    }

    @Test
    void update_WhenComplaintDoesNotExists_ShouldReturnFalse() {

        when(complaintRepository.existsById(1)).thenReturn(false);


        boolean result = complaintService.update(complaint);


        assertFalse(result);
        verify(complaintRepository).existsById(1);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void update_WhenOccursExceptionInExistsById_ShouldThrowException() {

        when(complaintRepository.existsById(1)).thenThrow(new RuntimeException("Error de base de datos"));


        assertThrows(RuntimeException.class, () -> complaintService.update(complaint));
        verify(complaintRepository).existsById(1);
        verify(complaintRepository, never()).save(any());
    }

    @Test
    void findById_WhenComplaintExists_ShouldReturnComplaint() {

        when(complaintRepository.findById(1)).thenReturn(Optional.of(complaint));


        Complaint result = complaintService.findById(1);


        assertNotNull(result);
        assertEquals(complaint, result);
        assertEquals(1, result.getId());
        assertEquals("Uso de lenguaje inapropiado", result.getReason());
        verify(complaintRepository).findById(1);
    }

    @Test
    void findById_WhenComplaintDoNotExists_ShouldReturnNull() {

        when(complaintRepository.findById(999)).thenReturn(Optional.empty());


        Complaint result = complaintService.findById(999);


        assertNull(result);
        verify(complaintRepository).findById(999);
    }

    @Test
    void findById_WhenOccursException_ShouldThrowException() {

        when(complaintRepository.findById(1)).thenThrow(new RuntimeException("Error de base de datos"));


        assertThrows(RuntimeException.class, () -> complaintService.findById(1));
        verify(complaintRepository).findById(1);
    }

    @Test
    void findAll_WhenExistsComplaints_ShouldReturnList() {

        Complaint complaint2 = new Complaint(2, game, accused, accuser, "Hacer trampa", dateTime.plusMinutes(30));
        List<Complaint> expectedComplaints = Arrays.asList(complaint, complaint2);

        when(complaintRepository.findAll()).thenReturn(expectedComplaints);


        List<Complaint> result = complaintService.findAll();


        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedComplaints, result);
        assertTrue(result.contains(complaint));
        assertTrue(result.contains(complaint2));
        verify(complaintRepository).findAll();
    }

    @Test
    void findAll_WhenDoNotExistsComplaints_ShouldReturnEmptyList() {

        List<Complaint> emptyList = List.of();
        when(complaintRepository.findAll()).thenReturn(emptyList);


        List<Complaint> result = complaintService.findAll();


        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(complaintRepository).findAll();
    }

    @Test
    void findAll_WhenOccursException_ShouldThrowException() {

        when(complaintRepository.findAll()).thenThrow(new RuntimeException("Error de base de datos"));


        assertThrows(RuntimeException.class, () -> complaintService.findAll());
        verify(complaintRepository).findAll();
    }

    @Test
    void registerComplaint_ShouldReturnNull() {

        Complaint result = complaintService.registerComplaint(game, accuser, accused, "Motivo test");


        assertNull(result);
        // No hay interacciones con el repositorio ya que el método no está implementado
        verifyNoInteractions(complaintRepository);
    }

    @Test
    void getComplaintsByGame_ShouldReturnEmptyList() {

        List<Complaint> result = complaintService.getComplaintsByGame(game);


        assertNotNull(result);
        assertTrue(result.isEmpty());
        // No hay interacciones con el repositorio ya que el método no está implementado
        verifyNoInteractions(complaintRepository);
    }

    // Clase auxiliar para crear instancias concretas de JugadorBase (ya que es abstracta)
    private static class ConcretePlayer extends BasePlayer {
        public ConcretePlayer(int id, String name, int availableArmies) {
            super();
            this.setId(id);
            this.setName(name);
            this.setAvailableArmies(availableArmies);
        }
    }
}