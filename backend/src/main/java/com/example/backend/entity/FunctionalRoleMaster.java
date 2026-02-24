package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "functional_role_master")
public class FunctionalRoleMaster {

    @Id
    @Column(name = "role_code")
    private Long roleCode;

    @Column(name = "role_name")
    private String roleName;

    public Long getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(Long roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}