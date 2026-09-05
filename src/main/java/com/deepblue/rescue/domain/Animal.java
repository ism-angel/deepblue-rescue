package com.deepblue.rescue.domain;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "animals")
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "animal_code", nullable = false, unique = true)
    private String animalCode;


    @Column(name = "common_name", nullable = false)
    private String commonName;


    @Column(name = "scientific_name")
    private String scientificName;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnimalSex sex;


    // Nuevo campo agregado en V3
    @Column(
            name = "tracking_device_code",
            length = 50,
            unique = true
    )
    private String trackingDeviceCode;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "rescue_case_id",
            unique = true
    )
    private RescueCase rescueCase;


    public Animal() {
    }


    public void setRescueCase(RescueCase rescueCase) {
        this.rescueCase = rescueCase;
    }


    public RescueCase getRescueCase() {
        return rescueCase;
    }


    @OneToOne(
            mappedBy = "animal",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private MedicalRecord medicalRecord;


    public void assignMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
        medicalRecord.setAnimal(this);
    }


    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }


    @OneToMany(mappedBy = "animal")
    private List<Treatment> treatments;


    public String getTrackingDeviceCode() {
        return trackingDeviceCode;
    }


    public void setTrackingDeviceCode(String trackingDeviceCode) {
        this.trackingDeviceCode = trackingDeviceCode;
    }

    public Long getId() {
        return id;
    }


    public void setAnimalCode(String animalCode) {
        this.animalCode = animalCode;
    }


    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }


    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }


    public void setSex(AnimalSex sex) {
        this.sex = sex;
    }


    public String getAnimalCode() {
        return animalCode;
    }


    public List<Treatment> getTreatments() {
        return treatments;
    }

}