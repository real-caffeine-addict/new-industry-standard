package com.psw.documents.controller;

import com.psw.documents.model.InsertDocumentApiContract;
import com.psw.documents.service.DocumentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class DocumentsController {
    private final DocumentService service;

    public DocumentsController (DocumentService service) { this.service = service; }

    @PostMapping("/create-document")
    public String createNewDocument (@RequestBody InsertDocumentApiContract input) {
        Long tempUser = 1L; //TODO: replace with actual jwt data
        return service.createNewDocument(input, tempUser);
    }
}
