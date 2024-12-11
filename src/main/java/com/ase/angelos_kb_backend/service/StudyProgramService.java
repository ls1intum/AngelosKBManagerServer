package com.ase.angelos_kb_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ase.angelos_kb_backend.dto.StudyProgramDTO;
import com.ase.angelos_kb_backend.exception.ResourceNotFoundException;
import com.ase.angelos_kb_backend.model.Organisation;
import com.ase.angelos_kb_backend.model.StudyProgram;
import com.ase.angelos_kb_backend.repository.OrganisationRepository;
import com.ase.angelos_kb_backend.repository.StudyProgramRepository;


@Service
public class StudyProgramService {
    private final StudyProgramRepository studyProgramRepository;
    private final OrganisationRepository organisationRepository;

    public StudyProgramService(StudyProgramRepository studyProgramRepository, OrganisationRepository organisationRepository) {
        this.studyProgramRepository = studyProgramRepository;
        this.organisationRepository = organisationRepository;
    }

    public List<StudyProgramDTO> getAllStudyProgramsByOrgId(Long orgId) {
        return studyProgramRepository.findByOrganisationsOrgID(orgId).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional
    public void addStudyProgramToOrg(Long spId, Long orgId) {
        Organisation organisation = organisationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found with id " + orgId));

        StudyProgram studyProgram = studyProgramRepository.findById(spId)
                .orElseThrow(() -> new ResourceNotFoundException("Study Program not found with id " + spId));

        organisation.getStudyPrograms().add(studyProgram);
        organisationRepository.save(organisation);
    }

    @Transactional
    public void removeStudyProgramFromOrg(Long spId, Long orgId) {
        Organisation organisation = organisationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found with id " + orgId));

        StudyProgram studyProgram = studyProgramRepository.findById(spId)
                .orElseThrow(() -> new ResourceNotFoundException("Study Program not found with id " + spId));

        organisation.getStudyPrograms().remove(studyProgram);
        organisationRepository.save(organisation);
    }

    @Transactional
    public StudyProgram createStudyProgram(String studyProgramName, Long orgId) {
        Organisation organisation = organisationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found with id " + orgId));

        // Check if the study program already exists for the organisation
        boolean exists = studyProgramRepository.existsByNameAndOrganisation(studyProgramName, orgId);
        if (exists) {
            throw new IllegalArgumentException("Study program with the name '" + studyProgramName + "' already exists for this organisation.");
        }

        // Create a new StudyProgram
        StudyProgram newStudyProgram = new StudyProgram();
        newStudyProgram.setName(studyProgramName);

        // Add the organisation to the list
        List<Organisation> organisations = new ArrayList<>();
        organisations.add(organisation);
        newStudyProgram.setOrganisations(organisations);

        return studyProgramRepository.save(newStudyProgram);
    }

    /**
     * Fetch a list of StudyPrograms by their IDs.
     * 
     * @param ids List of StudyProgram IDs.
     * @return List of StudyProgram entities.
     * @throws ResourceNotFoundException if any StudyProgram ID is not found.
     */
    public List<StudyProgram> getStudyProgramsByIds(List<Long> ids) {
        // Fetch all StudyPrograms matching the IDs
        List<StudyProgram> studyPrograms = studyProgramRepository.findAllById(ids);

        // Ensure all IDs are valid
        if (studyPrograms.size() != ids.size()) {
            List<Long> foundIds = studyPrograms.stream()
                                               .map(StudyProgram::getSpID)
                                               .toList();
            List<Long> missingIds = ids.stream()
                                       .filter(id -> !foundIds.contains(id))
                                       .toList();
            throw new ResourceNotFoundException("StudyProgram(s) not found for IDs: " + missingIds);
        }

        return studyPrograms;
    }

    public StudyProgramDTO convertToDto(StudyProgram studyProgram) {
        StudyProgramDTO dto = new StudyProgramDTO();
        dto.setId(studyProgram.getSpID());
        dto.setName(studyProgram.getName());
        return dto;
    }
}
