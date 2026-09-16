package com.deepblue.rescue.service;

import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.mapper.TreatmentMapper;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.service.impl.TreatmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private TreatmentMapper mapper;

    @InjectMocks
    private TreatmentServiceImpl service;

    private Animal buildAnimal(RescueStatus status) {
        RescueCase rescueCase = new RescueCase();
        rescueCase.setCaseCode("RES-2026-100");
        rescueCase.setRescueDate(LocalDate.of(2026, 8, 20));
        rescueCase.setRescueLocation("Playa Norte");
        rescueCase.setStatus(status);

        Animal animal = new Animal();
        animal.setAnimalCode("AN-001");
        animal.setCommonName("Green Sea Turtle");
        animal.setRescueCase(rescueCase);
        return animal;
    }

    private Specialist buildSpecialist(boolean active) {
        Specialist specialist = new Specialist();
        specialist.setProfessionalCode("SPEC-001");
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setEmail("elena.vargas@deepblue.org");
        specialist.setActive(active);
        return specialist;
    }

    // TEST 5: Tratamiento válido -> save()
    @Test
    void shouldRegisterTreatmentWhenValid() {
        Animal animal = buildAnimal(RescueStatus.IN_REHABILITATION);
        Specialist specialist = buildSpecialist(true);
        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001", "SPEC-001",
                LocalDateTime.of(2026, 8, 21, 9, 0),
                TreatmentType.WOUND_CARE,
                "Cleaning of left front flipper injury."
        );
        TreatmentResponse response = new TreatmentResponse(
                1L, "AN-001", "SPEC-001",
                request.performedAt(), TreatmentType.WOUND_CARE,
                request.description()
        );

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));
        when(treatmentRepository.save(any(Treatment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any(Treatment.class)))
                .thenReturn(response);

        TreatmentResponse result = service.register(request);

        assertThat(result).isEqualTo(response);
        verify(treatmentRepository).save(any(Treatment.class));
    }

    // TEST 6: Especialista inactivo -> BusinessRuleException, nunca save()
    @Test
    void shouldThrowWhenSpecialistIsInactive() {
        Animal animal = buildAnimal(RescueStatus.IN_REHABILITATION);
        Specialist specialist = buildSpecialist(false);
        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001", "SPEC-001",
                LocalDateTime.of(2026, 8, 21, 9, 0),
                TreatmentType.WOUND_CARE,
                "Cleaning of left front flipper injury."
        );

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any());
    }

    // TEST 7: Caso RELEASED -> BusinessRuleException, nunca save()
    @Test
    void shouldThrowWhenRescueCaseIsReleased() {
        Animal animal = buildAnimal(RescueStatus.RELEASED);
        Specialist specialist = buildSpecialist(true);
        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001", "SPEC-001",
                LocalDateTime.of(2026, 8, 22, 9, 0),
                TreatmentType.OBSERVATION,
                "Routine observation."
        );

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any());
    }

    // Extra: fecha de tratamiento anterior a la fecha de rescate
    @Test
    void shouldThrowWhenTreatmentDateIsBeforeRescueDate() {
        Animal animal = buildAnimal(RescueStatus.IN_REHABILITATION);
        Specialist specialist = buildSpecialist(true);
        CreateTreatmentRequest request = new CreateTreatmentRequest(
                "AN-001", "SPEC-001",
                LocalDateTime.of(2026, 8, 15, 9, 0),
                TreatmentType.WOUND_CARE,
                "Invalid date treatment."
        );

        when(animalRepository.findByAnimalCode("AN-001"))
                .thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(treatmentRepository, never()).save(any());
    }
}
