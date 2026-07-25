package com.psw.documents.controller;

import com.psw.documents.model.Document;
import com.psw.documents.model.InsertDocumentApiContract;
import com.psw.documents.service.DocumentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents")
public class DocumentsController {
    private final DocumentService service;

    public DocumentsController (DocumentService service) { this.service = service; }

    @PostMapping
    public String createNewDocument (@RequestBody InsertDocumentApiContract input) {
        Long tempUser = 1L; //TODO: replace with actual jwt data
        return service.createNewDocument(input, tempUser);
    }

    @GetMapping("/{id}")
    public Document readDocumentById (@PathVariable ("id") Long id) {
        return service.getDocumentById(id);
    }
}
