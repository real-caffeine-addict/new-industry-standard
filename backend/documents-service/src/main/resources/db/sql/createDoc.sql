INSERT INTO documents(
    project_id,
    dossier_id,
    title,
    document_number,
    type,
    originating_company_id
    ) VALUES (
        :projectId,
        :dossierId,
        :title,
        :documentNumber,
        :type,
        :originatingCompanyId
    );
