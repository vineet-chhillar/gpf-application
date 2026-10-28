
package com.example.backend.dto;

import java.util.List;

public class GpfEmployeeDetailsDTO {

    private Long empCode;
    private String empName;
    private String desgCode;
    private String designation;
    private List<FunctionalRoleDTO> functionalRoles;

    public Long getEmpCode() {
        return empCode;
    }

    public void setEmpCode(Long empCode) {
        this.empCode = empCode;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getDesgCode() {
        return desgCode;
    }

    public void setDesgCode(String desgCode) {
        this.desgCode = desgCode;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public List<FunctionalRoleDTO> getFunctionalRoles() {
        return functionalRoles;
    }

    public void setFunctionalRoles(
            List<FunctionalRoleDTO> functionalRoles) {
        this.functionalRoles = functionalRoles;
    }
}
