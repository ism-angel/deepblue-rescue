package com.deepblue.rescue;

import com.deepblue.rescue.domain.RescueCenter;
import com.deepblue.rescue.domain.RescueCase;
import com.deepblue.rescue.domain.RescueStatus;
import com.deepblue.rescue.domain.Animal;
import com.deepblue.rescue.domain.AnimalSex;
import com.deepblue.rescue.domain.Expertise;
import com.deepblue.rescue.domain.Specialist;
import com.deepblue.rescue.repository.RescueCenterRepository;
import com.deepblue.rescue.repository.RescueCaseRepository;
import com.deepblue.rescue.repository.AnimalRepository;
import com.deepblue.rescue.repository.SpecialistRepository;
import com.deepblue.rescue.repository.ExpertiseRepository;
import com.deepblue.rescue.repository.TreatmentRepository;
import com.deepblue.rescue.domain.MedicalRecord;
import com.deepblue.rescue.domain.Treatment;
import com.deepblue.rescue.domain.TreatmentType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;

import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PersistenceIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("deepblue_test")
                    .withUsername("deepblue")
                    .withPassword("deepblue");


    @Autowired
    private RescueCenterRepository rescueCenterRepository;

    @Autowired
    private RescueCaseRepository rescueCaseRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private SpecialistRepository specialistRepository;

    @Autowired
    private ExpertiseRepository expertiseRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;


    @Autowired
    private JdbcTemplate jdbcTemplate;

@Test
void flywayMigrationsShouldBeExecuted() {

    Integer count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version IN ('1', '2')
            """,
            Integer.class
    );

    assertThat(count).isEqualTo(2);
}

@Test
void inheritedRepositoryMethodsShouldWork() {

    RescueCenter center = new RescueCenter();

    center.setCode("DB-CAR");
    center.setName("DeepBlue Caribbean Center");
    center.setCity("Santa Marta");


    RescueCenter savedCenter = rescueCenterRepository.save(center);


    assertThat(savedCenter.getId()).isNotNull();


    assertThat(
            rescueCenterRepository.findById(savedCenter.getId())
    ).isPresent();


    assertThat(
            rescueCenterRepository.existsById(savedCenter.getId())
    ).isTrue();


    assertThat(
            rescueCenterRepository.count()
    ).isGreaterThanOrEqualTo(1);

}

@Test
void rescueCenterShouldHaveManyRescueCases() {

    RescueCenter center = new RescueCenter();

    center.setCode("DB-CAR");
    center.setName("DeepBlue Caribbean Center");
    center.setCity("Santa Marta");


    RescueCase rescueCase1 = new RescueCase();

    rescueCase1.setCaseCode("CASE-001");
    rescueCase1.setRescueDate(LocalDate.now());
    rescueCase1.setRescueLocation("Playa Blanca");
    rescueCase1.setStatus(RescueStatus.ADMITTED);


    RescueCase rescueCase2 = new RescueCase();

    rescueCase2.setCaseCode("CASE-002");
    rescueCase2.setRescueDate(LocalDate.now());
    rescueCase2.setRescueLocation("Taganga");
    rescueCase2.setStatus(RescueStatus.IN_REHABILITATION);


    rescueCase1.setRescueCenter(center);
    rescueCase2.setRescueCenter(center);


    rescueCenterRepository.save(center);

    rescueCaseRepository.save(rescueCase1);
    rescueCaseRepository.save(rescueCase2);


    RescueCase savedCase1 =
            rescueCaseRepository.findById(rescueCase1.getId())
                    .orElseThrow();


    RescueCase savedCase2 =
            rescueCaseRepository.findById(rescueCase2.getId())
                    .orElseThrow();


    assertThat(savedCase1.getRescueCenter().getId())
            .isEqualTo(savedCase2.getRescueCenter().getId());

}

@Test
void rescueCaseShouldHaveOneAnimal() {

    RescueCenter center = new RescueCenter();

    center.setCode("DB-CAR");
    center.setName("DeepBlue Caribbean Center");
    center.setCity("Santa Marta");


    RescueCase rescueCase = new RescueCase();

    rescueCase.setCaseCode("RES-2026-001");
    rescueCase.setRescueDate(LocalDate.now());
    rescueCase.setRescueLocation("Caribbean Sea");
    rescueCase.setStatus(RescueStatus.ADMITTED);
    rescueCase.setRescueCenter(center);


    Animal animal = new Animal();

    animal.setAnimalCode("AN-2026-001");
    animal.setCommonName("Green Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);


    rescueCase.assignAnimal(animal);


    rescueCenterRepository.save(center);
    rescueCaseRepository.save(rescueCase);
    animalRepository.save(animal);


    RescueCase savedCase =
            rescueCaseRepository.findById(rescueCase.getId())
                    .orElseThrow();


    Animal savedAnimal =
            animalRepository.findById(animal.getId())
                    .orElseThrow();


    assertThat(savedCase.getAnimal())
            .isNotNull();


    assertThat(savedAnimal.getRescueCase())
            .isNotNull();


    assertThat(savedCase.getAnimal().getId())
            .isEqualTo(savedAnimal.getId());


    assertThat(savedAnimal.getRescueCase().getId())
            .isEqualTo(savedCase.getId());

}

@Test
void animalShouldHaveOneMedicalRecord() {

    Animal animal = new Animal();

    animal.setAnimalCode("AN-2026-002");
    animal.setCommonName("Green Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);


    MedicalRecord medicalRecord = new MedicalRecord();

    medicalRecord.setInitialWeight(
            new BigDecimal("28.40")
    );

    medicalRecord.setInitialCondition("STABLE");

    medicalRecord.setInjuries(
            "Left front flipper injury"
    );


    animal.assignMedicalRecord(medicalRecord);


    animalRepository.save(animal);


    Animal savedAnimal =
            animalRepository.findById(animal.getId())
                    .orElseThrow();


    MedicalRecord savedRecord =
            savedAnimal.getMedicalRecord();


    assertThat(savedAnimal.getId())
            .isNotNull();


    assertThat(savedRecord)
            .isNotNull();


    assertThat(savedRecord.getId())
            .isNotNull();

}

@Test
void specialistShouldHaveManyExpertiseAreas() {

    Expertise trauma =
            expertiseRepository.findByNameIgnoreCase("Trauma")
                    .orElseThrow();


    Expertise rehabilitation =
            expertiseRepository.findByNameIgnoreCase("Rehabilitation")
                    .orElseThrow();


    Specialist specialist = new Specialist();

    specialist.setProfessionalCode("SP-2026-001");
    specialist.setFirstName("Elena");
    specialist.setLastName("Vargas");
    specialist.setEmail("elena.vargas@test.com");
    specialist.setActive(true);


    specialist.addExpertise(trauma);
    specialist.addExpertise(rehabilitation);


    specialistRepository.save(specialist);


    Specialist savedSpecialist =
            specialistRepository.findById(specialist.getId())
                    .orElseThrow();


    assertThat(savedSpecialist.getExpertiseAreas())
            .hasSize(2);

}

@Test
void shouldFindRescueCasesByStatus() {

    RescueCenter center = new RescueCenter();

    center.setCode("TEST-CENTER");
    center.setName("Test Center");
    center.setCity("Santa Marta");


    rescueCenterRepository.save(center);


    RescueCase case1 = new RescueCase();

    case1.setCaseCode("RES-001");
    case1.setRescueDate(LocalDate.now());
    case1.setRescueLocation("Santa Marta");
    case1.setStatus(RescueStatus.IN_REHABILITATION);
    case1.setRescueCenter(center);


    RescueCase case2 = new RescueCase();

    case2.setCaseCode("RES-002");
    case2.setRescueDate(LocalDate.now());
    case2.setRescueLocation("Cartagena");
    case2.setStatus(RescueStatus.READY_FOR_RELEASE);
    case2.setRescueCenter(center);


    RescueCase case3 = new RescueCase();

    case3.setCaseCode("RES-003");
    case3.setRescueDate(LocalDate.now());
    case3.setRescueLocation("Barranquilla");
    case3.setStatus(RescueStatus.IN_REHABILITATION);
    case3.setRescueCenter(center);


    rescueCaseRepository.save(case1);
    rescueCaseRepository.save(case2);
    rescueCaseRepository.save(case3);


    List<RescueCase> results =
            rescueCaseRepository.findByStatusOrderByRescueDateAsc(
                    RescueStatus.IN_REHABILITATION
            );


    assertThat(results)
            .hasSize(2);

}

@Test
void shouldFindAnimalsByRescueCenterCode() {

    RescueCenter carCenter = new RescueCenter();

    carCenter.setCode("DB-CAR");
    carCenter.setName("Caribbean Rescue Center");
    carCenter.setCity("Santa Marta");


    RescueCenter pacCenter = new RescueCenter();

    pacCenter.setCode("DB-PAC");
    pacCenter.setName("Pacific Rescue Center");
    pacCenter.setCity("Buenaventura");


    rescueCenterRepository.save(carCenter);
    rescueCenterRepository.save(pacCenter);



    RescueCase carCase = new RescueCase();

    carCase.setCaseCode("CASE-CAR-001");
    carCase.setRescueDate(LocalDate.now());
    carCase.setRescueLocation("Caribbean Sea");
    carCase.setStatus(RescueStatus.IN_REHABILITATION);
    carCase.setRescueCenter(carCenter);



    RescueCase pacCase = new RescueCase();

    pacCase.setCaseCode("CASE-PAC-001");
    pacCase.setRescueDate(LocalDate.now());
    pacCase.setRescueLocation("Pacific Ocean");
    pacCase.setStatus(RescueStatus.IN_REHABILITATION);
    pacCase.setRescueCenter(pacCenter);



    rescueCaseRepository.save(carCase);
    rescueCaseRepository.save(pacCase);



    Animal carAnimal = new Animal();

    carAnimal.setAnimalCode("AN-CAR-001");
    carAnimal.setCommonName("Green Sea Turtle");
    carAnimal.setScientificName("Chelonia mydas");
    carAnimal.setSex(AnimalSex.UNKNOWN);

    carCase.assignAnimal(carAnimal);



    Animal pacAnimal = new Animal();

    pacAnimal.setAnimalCode("AN-PAC-001");
    pacAnimal.setCommonName("Pacific Turtle");
    pacAnimal.setScientificName("Chelonia agassizii");
    pacAnimal.setSex(AnimalSex.UNKNOWN);

    pacCase.assignAnimal(pacAnimal);



    animalRepository.save(carAnimal);
    animalRepository.save(pacAnimal);



    List<Animal> results =
            animalRepository.findByRescueCase_RescueCenter_Code(
                    "DB-CAR"
            );



    assertThat(results)
            .hasSize(1);


    assertThat(results.get(0).getAnimalCode())
            .isEqualTo("AN-CAR-001");

}

@Test
void shouldFindActiveSpecialistsByExpertise() {

    Expertise trauma =
            expertiseRepository.findByNameIgnoreCase("Trauma")
                    .orElseThrow();

    Expertise rehabilitation =
            expertiseRepository.findByNameIgnoreCase("Rehabilitation")
                    .orElseThrow();

    Expertise marineMammals =
            expertiseRepository.findByNameIgnoreCase("Marine Mammals")
                    .orElseThrow();

    Expertise marineBirds =
            expertiseRepository.findByNameIgnoreCase("Marine Birds")
                    .orElseThrow();



    Specialist elena = new Specialist();

    elena.setProfessionalCode("SP-ELENA");
    elena.setFirstName("Elena");
    elena.setLastName("Vargas");
    elena.setEmail("elena@test.com");
    elena.setActive(true);


    elena.addExpertise(trauma);
    elena.addExpertise(rehabilitation);



    Specialist mateo = new Specialist();

    mateo.setProfessionalCode("SP-MATEO");
    mateo.setFirstName("Mateo");
    mateo.setLastName("Gomez");
    mateo.setEmail("mateo@test.com");
    mateo.setActive(true);


    mateo.addExpertise(marineMammals);
    mateo.addExpertise(rehabilitation);



    Specialist sofia = new Specialist();

    sofia.setProfessionalCode("SP-SOFIA");
    sofia.setFirstName("Sofia");
    sofia.setLastName("Rodriguez");
    sofia.setEmail("sofia@test.com");
    sofia.setActive(true);


    sofia.addExpertise(marineBirds);
    sofia.addExpertise(trauma);



    specialistRepository.save(elena);
    specialistRepository.save(mateo);
    specialistRepository.save(sofia);



    List<Specialist> results =
            specialistRepository.findActiveByExpertise("Trauma");



    assertThat(results)
            .hasSize(2);


    assertThat(results)
            .extracting(Specialist::getFirstName)
            .containsExactlyInAnyOrder(
                    "Elena",
                    "Sofia"
            );

}

@Test
void shouldCreateTreatmentsForAnimal() {

    RescueCenter center = new RescueCenter();

    center.setCode("TEST-TREAT");
    center.setName("Treatment Center");
    center.setCity("Santa Marta");


    rescueCenterRepository.save(center);



    RescueCase rescueCase = new RescueCase();

    rescueCase.setCaseCode("CASE-TREAT-001");
    rescueCase.setRescueDate(LocalDate.now());
    rescueCase.setRescueLocation("Caribbean Sea");
    rescueCase.setStatus(RescueStatus.IN_REHABILITATION);
    rescueCase.setRescueCenter(center);



    Animal animal = new Animal();

    animal.setAnimalCode("AN-TREAT-001");
    animal.setCommonName("Green Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);


    rescueCase.assignAnimal(animal);


    rescueCaseRepository.save(rescueCase);
    animalRepository.save(animal);



    Specialist elena = new Specialist();

    elena.setProfessionalCode("SP-ELENA-T");
    elena.setFirstName("Elena");
    elena.setLastName("Vargas");
    elena.setEmail("elena.treatment@test.com");
    elena.setActive(true);



    Specialist mateo = new Specialist();

    mateo.setProfessionalCode("SP-MATEO-T");
    mateo.setFirstName("Mateo");
    mateo.setLastName("Gomez");
    mateo.setEmail("mateo.treatment@test.com");
    mateo.setActive(true);



    specialistRepository.save(elena);
    specialistRepository.save(mateo);



    Treatment treatment1 = new Treatment();

    treatment1.setAnimal(animal);
    treatment1.setSpecialist(elena);
    treatment1.setPerformedAt(LocalDateTime.now());
    treatment1.setType(TreatmentType.WOUND_CARE);
    treatment1.setDescription("Flipper wound care");



    Treatment treatment2 = new Treatment();

    treatment2.setAnimal(animal);
    treatment2.setSpecialist(elena);
    treatment2.setPerformedAt(LocalDateTime.now().plusHours(1));
    treatment2.setType(TreatmentType.HYDRATION);
    treatment2.setDescription("Hydration therapy");



    Treatment treatment3 = new Treatment();

    treatment3.setAnimal(animal);
    treatment3.setSpecialist(mateo);
    treatment3.setPerformedAt(LocalDateTime.now().plusHours(2));
    treatment3.setType(TreatmentType.OBSERVATION);
    treatment3.setDescription("General observation");



    treatmentRepository.save(treatment1);
    treatmentRepository.save(treatment2);
    treatmentRepository.save(treatment3);


    assertThat(treatmentRepository.count())
            .isEqualTo(3);

}

@Test
void shouldFindAnimalTreatmentsOrderedByDate() {

    Animal animal = new Animal();

    animal.setAnimalCode("AN-ORDER-001");
    animal.setCommonName("Green Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);

    animalRepository.save(animal);



    Specialist specialist = new Specialist();

    specialist.setProfessionalCode("SP-ORDER-001");
    specialist.setFirstName("Elena");
    specialist.setLastName("Vargas");
    specialist.setEmail("order@test.com");
    specialist.setActive(true);

    specialistRepository.save(specialist);



    Treatment treatment1 = new Treatment();

    treatment1.setAnimal(animal);
    treatment1.setSpecialist(specialist);
    treatment1.setPerformedAt(
            LocalDateTime.of(2026, 8, 1, 10, 0)
    );
    treatment1.setType(TreatmentType.WOUND_CARE);



    Treatment treatment2 = new Treatment();

    treatment2.setAnimal(animal);
    treatment2.setSpecialist(specialist);
    treatment2.setPerformedAt(
            LocalDateTime.of(2026, 8, 10, 10, 0)
    );
    treatment2.setType(TreatmentType.HYDRATION);



    Treatment treatment3 = new Treatment();

    treatment3.setAnimal(animal);
    treatment3.setSpecialist(specialist);
    treatment3.setPerformedAt(
            LocalDateTime.of(2026, 8, 20, 10, 0)
    );
    treatment3.setType(TreatmentType.OBSERVATION);



    treatmentRepository.save(treatment1);
    treatmentRepository.save(treatment2);
    treatmentRepository.save(treatment3);



    List<Treatment> treatments =
            treatmentRepository.findByAnimal_IdOrderByPerformedAtAsc(
                    animal.getId()
            );



    assertThat(treatments)
            .hasSize(3);


    assertThat(treatments.get(0).getType())
            .isEqualTo(TreatmentType.WOUND_CARE);


    assertThat(treatments.get(1).getType())
            .isEqualTo(TreatmentType.HYDRATION);


    assertThat(treatments.get(2).getType())
            .isEqualTo(TreatmentType.OBSERVATION);

}

@Test
void shouldFindTreatmentsBetweenDates() {

    Animal animal = new Animal();

    animal.setAnimalCode("AN-DATE-001");
    animal.setCommonName("Sea Turtle");
    animal.setScientificName("Chelonia mydas");
    animal.setSex(AnimalSex.UNKNOWN);


    animalRepository.save(animal);



    Specialist specialist = new Specialist();

    specialist.setProfessionalCode("SP-DATE-001");
    specialist.setFirstName("Elena");
    specialist.setLastName("Vargas");
    specialist.setEmail("date@test.com");
    specialist.setActive(true);


    specialistRepository.save(specialist);



    Treatment treatment1 = new Treatment();

    treatment1.setAnimal(animal);
    treatment1.setSpecialist(specialist);
    treatment1.setPerformedAt(
            LocalDateTime.of(2026, 8, 1, 10, 0)
    );
    treatment1.setType(TreatmentType.WOUND_CARE);



    Treatment treatment2 = new Treatment();

    treatment2.setAnimal(animal);
    treatment2.setSpecialist(specialist);
    treatment2.setPerformedAt(
            LocalDateTime.of(2026, 8, 10, 10, 0)
    );
    treatment2.setType(TreatmentType.HYDRATION);



    Treatment treatment3 = new Treatment();

    treatment3.setAnimal(animal);
    treatment3.setSpecialist(specialist);
    treatment3.setPerformedAt(
            LocalDateTime.of(2026, 8, 20, 10, 0)
    );
    treatment3.setType(TreatmentType.OBSERVATION);



    treatmentRepository.save(treatment1);
    treatmentRepository.save(treatment2);
    treatmentRepository.save(treatment3);



    List<Treatment> results =
            treatmentRepository.findBetweenDates(
                    LocalDateTime.of(2026, 8, 5, 0, 0),
                    LocalDateTime.of(2026, 8, 15, 23, 59)
            );



    assertThat(results)
            .hasSize(1);


    assertThat(results.get(0).getPerformedAt())
            .isEqualTo(
                    LocalDateTime.of(2026, 8, 10, 10, 0)
            );

}

@Test
void shouldRejectDuplicateAnimalCode() {

    Animal animal1 = new Animal();

    animal1.setAnimalCode("AN-100");
    animal1.setCommonName("Green Sea Turtle");
    animal1.setScientificName("Chelonia mydas");
    animal1.setSex(AnimalSex.UNKNOWN);


    animalRepository.saveAndFlush(animal1);



    Animal animal2 = new Animal();

    animal2.setAnimalCode("AN-100");
    animal2.setCommonName("Another Turtle");
    animal2.setScientificName("Caretta caretta");
    animal2.setSex(AnimalSex.UNKNOWN);



    assertThatThrownBy(() ->
            animalRepository.saveAndFlush(animal2)
    )
    .isInstanceOf(DataIntegrityViolationException.class);

}

@Test
void shouldRejectInvalidForeignKey() {

    assertThatThrownBy(() ->
            jdbcTemplate.update("""
                INSERT INTO rescue_cases
                (
                    case_code,
                    rescue_date,
                    rescue_location,
                    status,
                    rescue_center_id
                )
                VALUES
                (
                    'BAD-FK-001',
                    '2026-08-01',
                    'Unknown',
                    'ADMITTED',
                    999999
                )
                """)
    )
    .isInstanceOf(DataIntegrityViolationException.class);

}

    @Test
    void shouldPersistCompleteTurtleRescueScenario() {

        RescueCenter center = new RescueCenter();

        center.setCode("DB-CAR");
        center.setName("DeepBlue Caribbean");
        center.setCity("Santa Marta");


        RescueCase rescueCase = new RescueCase();

        rescueCase.setCaseCode("RES-2026-100");
        rescueCase.setRescueDate(
                LocalDate.of(2026, 8, 18)
        );
        rescueCase.setRescueLocation("Bahía Concha");
        rescueCase.setStatus(
                RescueStatus.IN_REHABILITATION
        );


        center.addCase(rescueCase);


        Animal animal = new Animal();

        animal.setAnimalCode("AN-2026-100");
        animal.setCommonName("Green Sea Turtle");
        animal.setScientificName("Chelonia mydas");
        animal.setSex(AnimalSex.FEMALE);


        rescueCase.assignAnimal(animal);


        MedicalRecord record = new MedicalRecord();

        record.setInitialWeight(
                new BigDecimal("27.80")
        );

        record.setInitialCondition("STABLE");
        record.setInjuries(
                "Injury caused by fishing net"
        );

        record.setObservations(
                "Possible plastic ingestion"
        );


        animal.assignMedicalRecord(record);


        Expertise reptiles =
                expertiseRepository.findByNameIgnoreCase(
                        "Marine Reptiles"
                ).orElseThrow();


        Expertise trauma =
                expertiseRepository.findByNameIgnoreCase(
                        "Trauma"
                ).orElseThrow();


        Expertise rehabilitation =
                expertiseRepository.findByNameIgnoreCase(
                        "Rehabilitation"
                ).orElseThrow();


        Specialist specialist = new Specialist();

        specialist.setProfessionalCode("SPEC-001");
        specialist.setFirstName("Elena");
        specialist.setLastName("Vargas");
        specialist.setEmail("elena@deepblue.org");
        specialist.setActive(true);


        specialist.addExpertise(reptiles);
        specialist.addExpertise(trauma);
        specialist.addExpertise(rehabilitation);


        Treatment treatment1 = new Treatment();

        treatment1.setAnimal(animal);
        treatment1.setSpecialist(specialist);

        treatment1.setPerformedAt(
                LocalDateTime.of(2026, 8, 18, 10, 0)
        );

        treatment1.setType(
                TreatmentType.WOUND_CARE
        );

        treatment1.setDescription(
                "Cleaning of left front flipper"
        );


        Treatment treatment2 = new Treatment();

        treatment2.setAnimal(animal);
        treatment2.setSpecialist(specialist);

        treatment2.setPerformedAt(
                LocalDateTime.of(2026, 8, 18, 12, 0)
        );

        treatment2.setType(
                TreatmentType.HYDRATION
        );

        treatment2.setDescription(
                "Subcutaneous fluid therapy"
        );

        rescueCenterRepository.save(center);

        rescueCaseRepository.save(rescueCase);

        animalRepository.save(animal);

        specialistRepository.save(specialist);

        treatmentRepository.save(treatment1);
        treatmentRepository.save(treatment2);

        assertThat(
                rescueCaseRepository.findByCaseCode("RES-2026-100")
        ).isPresent();


        List<Animal> result =
                animalRepository.findAnimalsByStatusAndExpertise(
                        RescueStatus.IN_REHABILITATION,
                        "Trauma"
                );


        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(Animal::getAnimalCode)
                .isEqualTo("AN-2026-100");

    }

}