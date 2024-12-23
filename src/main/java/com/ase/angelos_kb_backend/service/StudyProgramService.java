package com.ase.angelos_kb_backend.service;

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
    private final OrganisationService organisationService;

    public StudyProgramService(StudyProgramRepository studyProgramRepository, OrganisationRepository organisationRepository, OrganisationService organisationService) {
        this.studyProgramRepository = studyProgramRepository;
        this.organisationRepository = organisationRepository;
        this.organisationService = organisationService;
    }

    public List<StudyProgramDTO> getAllStudyProgramsByOrgId(Long orgId) {
        // Check if the organization is "System Organisation"
        Organisation organisation = organisationService.getOrganisationById(orgId);
        if ("System Organisation".equals(organisation.getName())) {
            // Fetch all study programs if the organization is "System Organisation"
            return studyProgramRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
        }
        // Otherwise, filter by organization ID
        return studyProgramRepository.findByOrganisationOrgID(orgId).stream().map(this::convertToDto).collect(Collectors.toList());
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
    public StudyProgramDTO createStudyProgram(String studyProgramName, Long orgId) {
        // Fetch the Organisation
        Organisation organisation = organisationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found with id " + orgId));

        // Check if the study program already exists for the organisation
        boolean exists = studyProgramRepository.existsByNameAndOrganisation(studyProgramName, organisation.getOrgID());
        if (exists) {
            throw new IllegalArgumentException("Study program with the name '" + studyProgramName + "' already exists for this organisation.");
        }

        // Create a new StudyProgram
        StudyProgram newStudyProgram = new StudyProgram();
        newStudyProgram.setName(studyProgramName);
        newStudyProgram.setOrganisation(organisation); // Set the owning Organisation

        // Save the StudyProgram
        StudyProgram savedStudyProgram = studyProgramRepository.save(newStudyProgram);

        // Optional: Add the study program to the organisation's list of study programs
        organisation.getStudyPrograms().add(savedStudyProgram);

        return convertToDto(savedStudyProgram);
    }

    public List<StudyProgram> getStudyProgramsByName(String name) {
        return studyProgramRepository.findByName(name);
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
