package com.deepblue.rescue.domain;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rescue_centers")
public class RescueCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String code;


    @Column(nullable = false)
    private String name;


    @Column(nullable = false)
    private String city;


    @OneToMany(mappedBy = "rescueCenter")
    private List<RescueCase> cases = new ArrayList<>();


    public RescueCenter() {
    }


    public void addCase(RescueCase rescueCase) {
        cases.add(rescueCase);
        rescueCase.setRescueCenter(this);
    }

    public Long getId() {
        return id;
    }


    public void setCode(String code) {
        this.code = code;
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setCity(String city) {
        this.city = city;
    }


    public String getCode() {
        return code;
    }


    public List<RescueCase> getCases() {
        return cases;
    }

}