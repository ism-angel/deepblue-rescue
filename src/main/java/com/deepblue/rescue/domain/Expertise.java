package com.deepblue.rescue.domain;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "expertise")
public class Expertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String name;


    @ManyToMany(mappedBy = "expertiseAreas")
    private Set<Specialist> specialists = new HashSet<>();


    public Expertise() {
    }

    public Set<Specialist> getSpecialists() {
        return specialists;
    }

}