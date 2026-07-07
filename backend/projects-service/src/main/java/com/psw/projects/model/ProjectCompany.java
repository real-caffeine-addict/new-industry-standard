package com.psw.projects.model;

import com.psw.projects.enums.CompanyRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCompany {
    private Long id;
    private Long companyId;
    private Long projectId;
    private CompanyRole companyRole;
}
