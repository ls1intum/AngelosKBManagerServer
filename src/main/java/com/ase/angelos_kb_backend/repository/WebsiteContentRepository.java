package com.ase.angelos_kb_backend.repository;

import com.ase.angelos_kb_backend.model.WebsiteContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebsiteContentRepository extends JpaRepository<WebsiteContent, UUID> {
    List<WebsiteContent> findByOrganisationOrgID(Long orgId);
    Optional<WebsiteContent> findByLink(String link);
}