INSERT INTO document_revisions(
    document_id,
    revision_code,
    status,
    issued_at,
    file_location,
    number_of_files
    ) VALUES (
        :documentId,
        :revisionCode,
        :status,
        :issuedAt,
        :fileLocation,
        :numberOfFiles
    );
