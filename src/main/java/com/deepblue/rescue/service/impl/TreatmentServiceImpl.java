package com.deepblue.rescue.service.impl;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.service.TreatmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TreatmentServiceImpl implements TreatmentService {

    private final AnimalRepository animalRepository;
    private final SpecialistRepository specialistRepository;
    private final TreatmentRepository treatmentRepository;
    private final TreatmentMapper mapper;

    public TreatmentServiceImpl(
            AnimalRepository animalRepository,
            SpecialistRepository specialistRepository,
            TreatmentRepository treatmentRepository,
            TreatmentMapper mapper) {
        this.animalRepository = animalRepository;
        this.specialistRepository = specialistRepository;
        this.treatmentRepository = treatmentRepository;
        this.mapper = mapper;
    }

    @Override
    public List<TreatmentResponse> findByAnimalCode(String animalCode) {
        Animal animal = animalRepository
                .findByAnimalCode(animalCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal not found: " + animalCode
                ));

        return treatmentRepository
                .findByAnimal_IdOrderByPerformedAtAsc(animal.getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TreatmentResponse register(CreateTreatmentRequest request) {

        // Regla 1: el animal debe existir.
        Animal animal = animalRepository
                .findByAnimalCode(request.animalCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal not found: " + request.animalCode()
                ));

        // Regla 2: el especialista debe existir.
        Specialist specialist = specialistRepository
                .findByProfessionalCode(request.specialistCode())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Specialist not found: " + request.specialistCode()
                ));

        // Regla 3: el especialista debe estar activo.
        if (!specialist.isActive()) {
            throw new BusinessRuleException(
                    "Cannot register treatment because the specialist "
                            + specialist.getProfessionalCode()
                            + " is not active."
            );
        }

        // Obtener el RescueCase asociado al animal.
        RescueCase rescueCase = animal.getRescueCase();

        // Regla 4: no se pueden agregar tratamientos si el caso
        // está RELEASED o CLOSED.
        if (rescueCase != null
                && (rescueCase.getStatus() == RescueStatus.RELEASED
                || rescueCase.getStatus() == RescueStatus.CLOSED)) {
            throw new BusinessRuleException(
                    "Cannot register treatment because the animal "
                            + animal.getAnimalCode()
                            + " has already been released."
            );
        }

        // Regla 5: la fecha del tratamiento no puede ser anterior
        // a la fecha de rescate.
        if (rescueCase != null
                && request.performedAt().toLocalDate().isBefore(rescueCase.getRescueDate())) {
            throw new BusinessRuleException(
                    "Treatment date cannot be before the rescue date."
            );
        }

        // Crear y guardar el Treatment.
        Treatment treatment = new Treatment(
                animal,
                specialist,
                request.performedAt(),
                request.type(),
                request.description()
        );

        Treatment saved = treatmentRepository.save(treatment);

        return mapper.toResponse(saved);
    }
}
