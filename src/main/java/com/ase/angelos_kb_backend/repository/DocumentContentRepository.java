package com.ase.angelos_kb_backend.repository;

import com.ase.angelos_kb_backend.model.DocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentContentRepository extends JpaRepository<DocumentContent, UUID> {
    List<DocumentContent> findByOrganisationOrgID(Long orgId);
}