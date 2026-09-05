# DeepBlue Rescue

## 1. Nombre del proyecto

**DeepBlue Rescue**

Sistema de gestión de rescates de animales marinos desarrollado con
Spring Boot, Spring Data JPA, PostgreSQL, Flyway y Testcontainers.

## 2. Descripción del proyecto

DeepBlue Rescue permite gestionar centros de rescate, casos de rescate,
animales, expedientes médicos, especialistas, áreas de experiencia y
tratamientos realizados durante la rehabilitación.

El proyecto implementa persistencia con Spring Data JPA y PostgreSQL,
control de versiones de base de datos mediante Flyway y pruebas de
integración utilizando Testcontainers.

## 3. Modelo de datos

Entidades principales:

-   RescueCenter
-   RescueCase
-   Animal
-   MedicalRecord
-   Specialist
-   Expertise
-   Treatment

### RescueCenter

Representa un centro de rescate.

Campos: - id - code - name - city

### RescueCase

Representa un caso de rescate.

Campos: - id - caseCode - rescueDate - rescueLocation - status

### Animal

Representa el animal rescatado.

Campos: - id - animalCode - commonName - scientificName - sex -
trackingDeviceCode

### MedicalRecord

Representa el expediente médico del animal.

Campos: - id - initialWeight - initialCondition - injuries -
observations

### Specialist

Representa un especialista encargado de tratamientos.

### Expertise

Representa las áreas de experiencia.

### Treatment

Representa tratamientos realizados.

## 4. Relaciones

-   RescueCenter 1:N RescueCase
-   RescueCase 1:1 Animal
-   Animal 1:1 MedicalRecord
-   Animal 1:N Treatment
-   Specialist 1:N Treatment
-   Specialist N:M Expertise

## 5. Instrucciones para ejecutar

Requisitos:

-   Java 21
-   Maven
-   Docker Desktop

Ejecutar aplicación:

``` bash
mvn spring-boot:run
```

## 6. Ejecución de tests

Ejecutar:

``` bash
mvn clean test
```

Las pruebas validan migraciones Flyway, persistencia JPA, relaciones,
Query Methods, JPQL y conexión con PostgreSQL mediante Testcontainers.

Resultado esperado:

    BUILD SUCCESS

## 7. Flyway

Flyway controla la evolución del esquema de base de datos mediante
migraciones.

Migraciones:

-   V1\_\_create_schema.sql: creación del esquema inicial.
-   V2\_\_insert_expertise_catalog.sql: inserción del catálogo de
    experiencias.
-   V3\_\_add_tracking_device_to_animal.sql: incorporación del código de
    dispositivo GPS.

## 8. Testcontainers

Testcontainers permite ejecutar pruebas usando PostgreSQL dentro de un
contenedor Docker temporal.

Durante los tests: 1. Se inicia PostgreSQL. 2. Spring Boot conecta
mediante ServiceConnection. 3. Flyway ejecuta las migraciones. 4. Los
repositorios son probados sobre una base real.

## 9. Query Methods implementados

RescueCenterRepository: - findByCode

RescueCaseRepository: - findByCaseCode -
findByStatusOrderByRescueDateAsc - findByRescueCenter_Code

AnimalRepository: - findByAnimalCode -
findByCommonNameContainingIgnoreCase - findByRescueCase_Status -
findByRescueCase_RescueCenter_Code

ExpertiseRepository: - findByNameIgnoreCase

TreatmentRepository: - findByAnimal_IdOrderByPerformedAtAsc

## 10. Consultas JPQL implementadas

-   Especialistas activos según experiencia utilizando JOIN, LOWER y
    parámetros nombrados.
-   Tratamientos entre fechas ordenados cronológicamente.
-   Tratamientos realizados en un centro determinado.
-   Tratamientos realizados por especialistas con una experiencia
    específica utilizando relaciones N:M.
