package com.psw.projects.model;

import com.psw.projects.enums.Discipline;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dossier {
    private Long id;
    private Long projectId;
    private String name;
    private String code;
    private Discipline discipline;
    private String system;
    private String subsystem;
    private String area;
}
