package com.psw.projects.model;

import com.psw.projects.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMember {
    private Long id;
    private Long userId;
    private Long projectId;
    private ProjectRole projectRole;
}
