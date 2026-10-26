package com.deepblue.rescue.controller;

import com.deepblue.rescue.domain.TreatmentType;
import com.deepblue.rescue.dto.request.CreateTreatmentRequest;
import com.deepblue.rescue.dto.response.TreatmentResponse;
import com.deepblue.rescue.exception.BusinessRuleException;
import com.deepblue.rescue.exception.GlobalExceptionHandler;
import com.deepblue.rescue.exception.ResourceNotFoundException;
import com.deepblue.rescue.service.TreatmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TreatmentController.class)
@Import(GlobalExceptionHandler.class)
class TreatmentControllerTest {

    private static final String VALID_PAYLOAD = """
            {
              "animalCode": "AN-001",
              "specialistCode": "SPEC-001",
              "performedAt": "2026-08-21T09:00:00",
              "type": "WOUND_CARE",
              "description": "Cleaning and treatment of flipper injury."
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TreatmentService service;

    @Test
    void shouldCreateTreatment() throws Exception {
        TreatmentResponse response = new TreatmentResponse(
                100L,
                "AN-001",
                "SPEC-001",
                LocalDateTime.of(2026, 8, 21, 9, 0),
                TreatmentType.WOUND_CARE,
                "Cleaning and treatment of flipper injury."
        );

        when(service.register(any(CreateTreatmentRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.animalCode").value("AN-001"))
                .andExpect(jsonPath("$.specialistCode").value("SPEC-001"))
                .andExpect(jsonPath("$.type").value("WOUND_CARE"))
                .andExpect(jsonPath("$.description")
                        .value("Cleaning and treatment of flipper injury."));

        verify(service).register(any(CreateTreatmentRequest.class));
    }

    @Test
    void shouldReturn400WhenPayloadIsInvalid() throws Exception {
        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "animalCode": "",
                                  "specialistCode": "",
                                  "type": null,
                                  "description": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.details.animalCode")
                        .value("Animal code is required"))
                .andExpect(jsonPath("$.details.specialistCode")
                        .value("Specialist code is required"))
                .andExpect(jsonPath("$.details.performedAt")
                        .value("Treatment date is required"))
                .andExpect(jsonPath("$.details.type")
                        .value("Treatment type is required"))
                // description incumple @NotBlank y @Size a la vez; el handler
                // conserva uno solo, así que solo se verifica que exista.
                .andExpect(jsonPath("$.details.description").exists());

        verify(service, never()).register(any());
    }

    @Test
    void shouldReturn404WhenAnimalDoesNotExist() throws Exception {
        when(service.register(any(CreateTreatmentRequest.class)))
                .thenThrow(new ResourceNotFoundException(
                        "Animal not found: AN-999"));

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD.replace("AN-001", "AN-999")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Animal not found: AN-999"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).register(any(CreateTreatmentRequest.class));
    }

    @Test
    void shouldReturn409WhenBusinessRuleIsViolated() throws Exception {
        when(service.register(any(CreateTreatmentRequest.class)))
                .thenThrow(new BusinessRuleException(
                        "Released animals cannot receive treatments"));

        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Released animals cannot receive treatments"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).register(any(CreateTreatmentRequest.class));
    }

    @Test
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"animalCode\": \"AN-001\", "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Malformed or invalid JSON request"))
                .andExpect(jsonPath("$.details.body")
                        .value("Check JSON syntax and enum values"));

        verify(service, never()).register(any());
    }

    @Test
    void shouldReturn400WhenTreatmentTypeEnumIsInvalid() throws Exception {
        mockMvc.perform(post("/api/treatments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD.replace("WOUND_CARE", "FLYING")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Malformed or invalid JSON request"))
                .andExpect(jsonPath("$.details.body")
                        .value("Check JSON syntax and enum values"));

        verify(service, never()).register(any());
    }
}
