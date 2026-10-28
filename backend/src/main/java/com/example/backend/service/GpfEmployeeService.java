
package com.example.backend.service;

import com.example.backend.dto.FunctionalRoleDTO;
import com.example.backend.dto.GpfEmployeeDetailsDTO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;

@Service
public class GpfEmployeeService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public GpfEmployeeService(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper) {

        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }
public Optional<Long> getLinkedEmployeeCode(Long empCode) {


    String sql = """
        SELECT link_emp_code
        FROM functional_designation
        WHERE emp_code = ?
          AND link_emp_code IS NOT NULL
          AND post_detail_id = (
              SELECT MAX(a.post_detail_id)
              FROM functional_designation a
              WHERE a.emp_code = ?
                AND a.link_emp_code IS NOT NULL
          )
        """;

    List<Long> results = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> rs.getLong("link_emp_code"),
            empCode,
            empCode
    );

    return results.stream().findFirst();
}
    private static final String EMPLOYEE_QUERY = """
        select ed.emp_code,
       emp_name empname,
       desg_code, desg_desc designation,
       coalesce(x.functional_roles, '[]'::json) as functional_roles
from public.employee_data ed
         left join (select  emp_code,
                            json_agg(json_build_object(
                                    'post_id', fd.post_id,
                                    'post_name', fp.post_name_en,
                                    'div_code', dm.div_code,
                                    'div_name', dm.div_name
                                )) as functional_roles
                    from public.functional_designation fd
                             left join public.functional_post fp on fd.post_id = fp.post_id and fd.is_active
                             left join public.div_mast dm on fd.div_code = dm.div_code
                    where fd.is_active
                    group by fd.emp_code) x on ed.emp_code = x.emp_code
        WHERE ed.emp_status IS NULL
          AND ed.emp_code = ?
        """;

    public Optional<GpfEmployeeDetailsDTO> getEmployeeDetails(
            Long empCode) {

        List<GpfEmployeeDetailsDTO> results =
                jdbcTemplate.query(
                        EMPLOYEE_QUERY,
                        (rs, rowNum) -> mapEmployee(rs),
                        empCode
                );

        return results.stream().findFirst();
    }

    private GpfEmployeeDetailsDTO mapEmployee(ResultSet rs)
            throws java.sql.SQLException {

        GpfEmployeeDetailsDTO dto =
                new GpfEmployeeDetailsDTO();

        dto.setEmpCode(rs.getLong("emp_code"));
        dto.setEmpName(rs.getString("empname"));
        dto.setDesgCode(rs.getString("desg_code"));
        dto.setDesignation(rs.getString("designation"));

        String rolesJson =
                rs.getString("functional_roles");

        try {
            List<FunctionalRoleDTO> roles =
                    objectMapper.readValue(
                            rolesJson,
                            new TypeReference<List<FunctionalRoleDTO>>() {}
                    );

            dto.setFunctionalRoles(roles);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to parse employee functional roles", ex);
        }

        return dto;
    }
}
