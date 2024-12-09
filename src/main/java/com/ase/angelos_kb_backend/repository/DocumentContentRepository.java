package com.ase.angelos_kb_backend.repository;

import com.ase.angelos_kb_backend.model.DocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentContentRepository extends JpaRepository<DocumentContent, Long> {
    List<DocumentContent> findByOrganisationOrgID(Long orgId);
}