package com.example.backend.entity;



import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;



@Entity
@Table(name = "gpfadvancemaster")
public class GpfAdvanceMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String empcode;
    private String empname;
    private String designation;
    private String empdivision;
    private String empmobileno;
    private String empemailid;

    private LocalDate dateofjoining;
    private LocalDate dateofsuperannuation;

    private String concernedofficername;

    private LocalDateTime createdat = LocalDateTime.now();

    /* GETTERS */

    public Long getId() { return id; }

    public String getEmpcode() { return empcode; }

    public String getEmpname() { return empname; }

    public String getDesignation() { return designation; }

    public String getEmpdivision() { return empdivision; }

    public String getEmpmobileno() { return empmobileno; }

    public String getEmpemailid() { return empemailid; }

    public LocalDate getDateofjoining() { return dateofjoining; }

    public LocalDate getDateofsuperannuation() { return dateofsuperannuation; }

    public String getConcernedofficername() { return concernedofficername; }

    public LocalDateTime getCreatedat() { return createdat; }

    /* SETTERS */

    public void setId(Long id) { this.id = id; }

    public void setEmpcode(String empcode) { this.empcode = empcode; }

    public void setEmpname(String empname) { this.empname = empname; }

    public void setDesignation(String designation) { this.designation = designation; }

    public void setEmpdivision(String empdivision) { this.empdivision = empdivision; }

    public void setEmpmobileno(String empmobileno) { this.empmobileno = empmobileno; }

    public void setEmpemailid(String empemailid) { this.empemailid = empemailid; }

    public void setDateofjoining(LocalDate dateofjoining) { this.dateofjoining = dateofjoining; }

    public void setDateofsuperannuation(LocalDate dateofsuperannuation) { this.dateofsuperannuation = dateofsuperannuation; }

    public void setConcernedofficername(String concernedofficername) { this.concernedofficername = concernedofficername; }

    public void setCreatedat(LocalDateTime createdat) { this.createdat = createdat; }
}
