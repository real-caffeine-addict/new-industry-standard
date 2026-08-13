package com.psw.incidents.controller;

import com.psw.common.dto.IncidentDto;
import com.psw.incidents.repository.IncidentsRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incidents")
public class IncidentController {
    private final IncidentsRepository repo;

    public IncidentController (IncidentsRepository repo) { this.repo = repo; }

    @PostMapping
    public Long insertIncident (@RequestBody IncidentDto data){ return repo.insertIncident(data); }
}
