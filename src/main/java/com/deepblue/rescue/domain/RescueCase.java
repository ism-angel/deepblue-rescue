package com.deepblue.rescue.domain;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "rescue_cases")
public class RescueCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "case_code", nullable = false, unique = true)
    private String caseCode;


    @Column(name = "rescue_date", nullable = false)
    private LocalDate rescueDate;


    @Column(name = "rescue_location", nullable = false)
    private String rescueLocation;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RescueStatus status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescue_center_id", nullable = false)
    private RescueCenter rescueCenter;


    public RescueCase() {
    }


    public void setRescueCenter(RescueCenter rescueCenter) {
        this.rescueCenter = rescueCenter;
    }

    @OneToOne(
        mappedBy = "rescueCase",
        cascade = CascadeType.ALL,
        orphanRemoval = true
)

private Animal animal;

public void assignAnimal(Animal animal) {
    this.animal = animal;
    animal.setRescueCase(this);
}

    public Long getId() {
        return id;
    }


    public void setCaseCode(String caseCode) {
        this.caseCode = caseCode;
    }


    public void setRescueDate(LocalDate rescueDate) {
        this.rescueDate = rescueDate;
    }


    public void setRescueLocation(String rescueLocation) {
        this.rescueLocation = rescueLocation;
    }


    public void setStatus(RescueStatus status) {
        this.status = status;
    }


    public RescueCenter getRescueCenter() {
        return rescueCenter;
    }


    public Animal getAnimal() {
        return animal;
    }

}