package com.ase.angelos_kb_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ase.angelos_kb_backend.model.StudyProgram;

public interface StudyProgramRepository extends JpaRepository<StudyProgram, Long> {
     List<StudyProgram> findByOrganisationsOrgID(Long orgId);
     boolean existsByName(String name);
     
     @Query("SELECT COUNT(sp) > 0 FROM StudyProgram sp JOIN sp.organisations org WHERE sp.name = :name AND org.orgID = :orgId")
     boolean existsByNameAndOrganisation(@Param("name") String name, @Param("orgId") Long orgId);
}
