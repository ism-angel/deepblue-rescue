package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.response.AnimalResponse;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.service.AnimalService;
import com.deepblue.rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnimalController.class)
@Import(GlobalExceptionHandler.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @MockitoBean
    private TreatmentService treatmentService;

    private static AnimalResponse animal(
            Long id,
            String animalCode,
            String commonName,
            String scientificName) {
        return new AnimalResponse(
                id,
                animalCode,
                commonName,
                scientificName,
                AnimalSex.FEMALE,
                "RES-2026-001",
                RescueStatus.IN_REHABILITATION
        );
    }

    // ---------------------------------------------------------------
    // GET /api/animals/{animalCode}
    // ---------------------------------------------------------------

    @Test
    void shouldReturnAnimalByCode() throws Exception {
        when(animalService.findByCode("AN-001"))
                .thenReturn(animal(1L, "AN-001",
                        "Green Sea Turtle", "Chelonia mydas"));

        mockMvc.perform(get("/api/animals/{animalCode}", "AN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.animalCode").value("AN-001"))
                .andExpect(jsonPath("$.commonName").value("Green Sea Turtle"))
                .andExpect(jsonPath("$.scientificName").value("Chelonia mydas"))
                .andExpect(jsonPath("$.sex").value("FEMALE"))
                .andExpect(jsonPath("$.caseCode").value("RES-2026-001"))
                .andExpect(jsonPath("$.rescueStatus").value("IN_REHABILITATION"));

        verify(animalService).findByCode("AN-001");
    }

    @Test
    void shouldReturn404WhenAnimalDoesNotExist() throws Exception {
        when(animalService.findByCode("AN-999"))
                .thenThrow(new ResourceNotFoundException(
                        "Animal not found: AN-999"));

        mockMvc.perform(get("/api/animals/{animalCode}", "AN-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Animal not found: AN-999"))
                .andExpect(jsonPath("$.details").isMap());

        verify(animalService).findByCode("AN-999");
    }

    // ---------------------------------------------------------------
    // GET /api/animals/in-rehabilitation
    // ---------------------------------------------------------------

    @Test
    void shouldReturnAnimalsInRehabilitation() throws Exception {
        AnimalResponse animal1 = animal(1L, "AN-001",
                "Green Sea Turtle", "Chelonia mydas");
        AnimalResponse animal2 = animal(2L, "AN-002",
                "Hawksbill Turtle", "Eretmochelys imbricata");

        when(animalService.findAnimalsInRehabilitation())
                .thenReturn(List.of(animal1, animal2));

        mockMvc.perform(get("/api/animals/in-rehabilitation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].animalCode").value("AN-001"))
                .andExpect(jsonPath("$[1].animalCode").value("AN-002"));

        verify(animalService).findAnimalsInRehabilitation();
        // "in-rehabilitation" no debe interpretarse como {animalCode}
        verify(animalService, never()).findByCode(anyString());
    }

    // ---------------------------------------------------------------
    // GET /api/animals/{animalCode}/treatments
    // ---------------------------------------------------------------

    @Test
    void shouldReturnAnimalTreatments() throws Exception {
        TreatmentResponse treatment = new TreatmentResponse(
                100L,
                "AN-001",
                "SPEC-001",
                LocalDateTime.of(2026, 8, 21, 9, 0),
                TreatmentType.WOUND_CARE,
                "Cleaning and treatment of flipper injury."
        );

        when(treatmentService.findByAnimalCode("AN-001"))
                .thenReturn(List.of(treatment));

        mockMvc.perform(get("/api/animals/{animalCode}/treatments", "AN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].animalCode").value("AN-001"))
                .andExpect(jsonPath("$[0].type").value("WOUND_CARE"));

        verify(treatmentService).findByAnimalCode("AN-001");
        verifyNoInteractions(animalService);
    }

    // ---------------------------------------------------------------
    // GET /api/animals/{animalCode}/treatment-eligibility
    // ---------------------------------------------------------------

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnTreatmentEligibility(boolean eligible) throws Exception {
        when(animalService.canReceiveTreatment("AN-001"))
                .thenReturn(eligible);

        mockMvc.perform(get(
                        "/api/animals/{animalCode}/treatment-eligibility",
                        "AN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalCode").value("AN-001"))
                .andExpect(jsonPath("$.eligible").value(eligible));

        verify(animalService).canReceiveTreatment("AN-001");
    }
}
