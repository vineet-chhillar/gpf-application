package com.example.backend.entity;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import jakarta.persistence.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "gpfwithdrawlmaster")

public class GpfWithdrawlMaster {
public GpfWithdrawlMaster() {
}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empcode", nullable = false, unique = true, length = 20)
    private String empcode;

    @Column(name = "empname", nullable = false, length = 100)
    private String empname;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "empdivision", length = 100)
    private String empdivision;

    @Column(name = "empmobileno", length = 15)
    private String empmobileno;

    @Column(name = "empemailid", length = 150)
    private String empemailid;

    @Column(name = "dateofjoining", nullable = false)
    private LocalDate dateofjoining;

    @Column(name = "dateofsuperannuation", nullable = false)
    private LocalDate dateofsuperannuation;

    

     
   @Column(name = "createdat",nullable = false,updatable = false,insertable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "master", cascade = CascadeType.ALL)
    private List<GpfWithdrawlDetails> details;


    // ✅ Getters & Setters (generate via IDE)
    public Long getId() {
    return id;
}


    public String getEmpcode() { return empcode; }
    public void setEmpcode(String empcode) { this.empcode = empcode; }

    public String getEmpname() { return empname; }
    public void setEmpname(String empname) { this.empname = empname; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmpdivision() { return empdivision; }
    public void setEmpdivision(String empdivision) { this.empdivision = empdivision; }

    public String getEmpmobileno() { return empmobileno; }
    public void setEmpmobileno(String empmobileno) { this.empmobileno = empmobileno; }

    public String getEmpemailid() { return empemailid; }
    public void setEmpemailid(String empemailid) { this.empemailid = empemailid; }

    public LocalDate getDateofjoining() { return dateofjoining; }
    public void setDateofjoining(LocalDate dateofjoining) { this.dateofjoining = dateofjoining; }

    public LocalDate getDateofsuperannuation() { return dateofsuperannuation; }
    public void setDateofsuperannuation(LocalDate dateofsuperannuation) {
        this.dateofsuperannuation = dateofsuperannuation;
    }

    
}


