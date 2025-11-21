package com.competition.service;

import com.competition.entity.Registration;
import com.competition.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll().stream()
                .filter(registration -> !registration.getDeleted())
                .collect(Collectors.toList());
    }

    public Optional<Registration> getRegistrationById(Integer id) {
        return registrationRepository.findById(id);
    }

    public Registration createRegistration(Registration registration) {
        registration.setRegistrationStatus("pending");
        return registrationRepository.save(registration);
    }

    public Registration updateRegistrationStatus(Integer id, String status, Integer auditUserId) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        registration.setRegistrationStatus(status);
        registration.setAuditUserId(auditUserId);
        registration.setAuditTime(LocalDateTime.now());

        return registrationRepository.save(registration);
    }

    public List<Registration> getRegistrationsByCompetition(Integer competitionId) {
        return registrationRepository.findByCompetitionId(competitionId);
    }

    public List<Registration> getRegistrationsByUser(Integer userId) {
        return registrationRepository.findByUserId(userId);
    }

    public List<Registration> getRegistrationsByStatus(String status) {
        return registrationRepository.findByRegistrationStatus(status);
    }
}
