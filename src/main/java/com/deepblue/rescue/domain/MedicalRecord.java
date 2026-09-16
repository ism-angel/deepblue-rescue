package com.deepblue.rescue.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "animal_id",
            nullable = false,
            unique = true
    )
    private Animal animal;


    @Column(name = "initial_weight")
    private BigDecimal initialWeight;


    @Column(name = "initial_condition")
    private String initialCondition;


    @Column(columnDefinition = "TEXT")
    private String injuries;


    @Column(columnDefinition = "TEXT")
    private String observations;


    public MedicalRecord() {
    }


    public void setAnimal(Animal animal) {
        this.animal = animal;
    }

    public Long getId() {
        return id;
    }


    public void setInitialWeight(BigDecimal initialWeight) {
        this.initialWeight = initialWeight;
    }


    public void setInitialCondition(String initialCondition) {
        this.initialCondition = initialCondition;
    }


    public void setInjuries(String injuries) {
        this.injuries = injuries;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

}