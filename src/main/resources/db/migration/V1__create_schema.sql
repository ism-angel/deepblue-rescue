CREATE TABLE rescue_centers (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL
);

CREATE TABLE rescue_cases (
    id BIGSERIAL PRIMARY KEY,
    case_code VARCHAR(50) UNIQUE NOT NULL,
    rescue_date DATE NOT NULL,
    rescue_location VARCHAR(150) NOT NULL,
    status VARCHAR(50) NOT NULL,
    rescue_center_id BIGINT NOT NULL,

    CONSTRAINT chk_rescue_case_status
        CHECK (
            status IN (
                'ADMITTED',
                'UNDER_EVALUATION',
                'IN_REHABILITATION',
                'READY_FOR_RELEASE',
                'RELEASED',
                'CLOSED'
            )
        ),

    CONSTRAINT fk_rescue_case_center
        FOREIGN KEY (rescue_center_id)
        REFERENCES rescue_centers(id)
);

CREATE TABLE animals (
    id BIGSERIAL PRIMARY KEY,
    animal_code VARCHAR(50) UNIQUE NOT NULL,
    common_name VARCHAR(100) NOT NULL,
    scientific_name VARCHAR(150),
    sex VARCHAR(20),
    rescue_case_id BIGINT UNIQUE,

    CONSTRAINT fk_animal_rescue_case
    FOREIGN KEY (rescue_case_id)
    REFERENCES rescue_cases(id)
);


CREATE TABLE medical_records (
    id BIGSERIAL PRIMARY KEY,
    animal_id BIGINT UNIQUE NOT NULL,
    initial_weight DECIMAL(10,2),
    initial_condition VARCHAR(100),
    injuries TEXT,
    observations TEXT,

    CONSTRAINT fk_medical_record_animal
        FOREIGN KEY (animal_id)
        REFERENCES animals(id)
);


CREATE TABLE specialists (
    id BIGSERIAL PRIMARY KEY,
    professional_code VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    active BOOLEAN NOT NULL
);


CREATE TABLE expertise (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);


CREATE TABLE specialist_expertise (
    specialist_id BIGINT NOT NULL,
    expertise_id BIGINT NOT NULL,

    PRIMARY KEY (specialist_id, expertise_id),

    CONSTRAINT fk_specialist_expertise_specialist
        FOREIGN KEY (specialist_id)
        REFERENCES specialists(id),

    CONSTRAINT fk_specialist_expertise_expertise
        FOREIGN KEY (expertise_id)
        REFERENCES expertise(id)
);


CREATE TABLE treatments (
    id BIGSERIAL PRIMARY KEY,
    animal_id BIGINT NOT NULL,
    specialist_id BIGINT NOT NULL,
    performed_at TIMESTAMP NOT NULL,
    type VARCHAR(100) NOT NULL,
    description TEXT,

    CONSTRAINT fk_treatment_animal
        FOREIGN KEY (animal_id)
        REFERENCES animals(id),

    CONSTRAINT fk_treatment_specialist
        FOREIGN KEY (specialist_id)
        REFERENCES specialists(id)
);

CREATE INDEX idx_rescue_cases_center
ON rescue_cases(rescue_center_id);


CREATE INDEX idx_rescue_cases_status
ON rescue_cases(status);


CREATE INDEX idx_rescue_cases_date
ON rescue_cases(rescue_date);


CREATE INDEX idx_treatments_animal
ON treatments(animal_id);


CREATE INDEX idx_treatments_specialist
ON treatments(specialist_id);


CREATE INDEX idx_treatments_performed_at
ON treatments(performed_at);