package com.ase.angelos_kb_backend.repository;

import com.ase.angelos_kb_backend.model.SampleQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SampleQuestionRepository extends JpaRepository<SampleQuestion, UUID> {
    List<SampleQuestion> findByOrganisationOrgID(Long orgId);
}
