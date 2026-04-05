package ar.edu.utn.frc.tup.piii.services.impl;

import ar.edu.utn.frc.tup.piii.model.entities.Complaint;
import ar.edu.utn.frc.tup.piii.model.entities.Game;
import ar.edu.utn.frc.tup.piii.model.entities.BasePlayer;
import ar.edu.utn.frc.tup.piii.model.entities.CommunicationType;
import ar.edu.utn.frc.tup.piii.model.repository.ComplaintRepository;
import ar.edu.utn.frc.tup.piii.services.interfaces.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de denuncias.
 *
 * Se encarga de persistir y recuperar denuncias realizadas por jugadores dentro de una partida.
 * Utiliza el repositorio de Denuncia para interactuar con la base de datos.
 *
 * Este servicio apoya el sistema de control de comunicación, permitiendo registrar
 * y revisar reclamos contra jugadores que hayan incumplido el tipo de comunicación establecido.
 *
 * @see Complaint
 * @see ComplaintRepository
 * @see CommunicationType
 *
 * @author Ismael Ceballos
 */
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;

    @Override
    public boolean save(Complaint complaint){
        Complaint complaintSave = complaintRepository.save(complaint);
        return complaintSave.getId() > 0;
    }

    @Override
    public boolean update(Complaint complaint) {
        if (complaintRepository.existsById(complaint.getId())) {
            complaintRepository.save(complaint);
            return true;
        }
        return false;
    }

    @Override
    public Complaint findById(int id) {
        return complaintRepository.findById(id).orElse(null);
    }

    @Override
    public List<Complaint> findAll() {
        return complaintRepository.findAll();
    }

    @Override
    public Complaint registerComplaint(Game game, BasePlayer accuser, BasePlayer accused, String reason) {
        return null;
    }

    @Override
    public List<Complaint> getComplaintsByGame(Game game) {
        return List.of();
    }

}
