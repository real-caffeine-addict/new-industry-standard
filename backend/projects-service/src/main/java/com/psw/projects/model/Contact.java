package com.psw.projects.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    private Long id;
    private Long companyId;
    private String givenName;
    private String surname;
    private String displayName;
    private String position;
    private String email;
    private String mobile;
}
