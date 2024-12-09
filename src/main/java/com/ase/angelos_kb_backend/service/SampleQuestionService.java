package com.ase.angelos_kb_backend.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ase.angelos_kb_backend.dto.SampleQuestionDTO;
import com.ase.angelos_kb_backend.dto.StudyProgramDTO;
import com.ase.angelos_kb_backend.exception.ResourceNotFoundException;
import com.ase.angelos_kb_backend.exception.UnauthorizedException;
import com.ase.angelos_kb_backend.model.Organisation;
import com.ase.angelos_kb_backend.model.SampleQuestion;
import com.ase.angelos_kb_backend.model.StudyProgram;
import com.ase.angelos_kb_backend.repository.SampleQuestionRepository;

@Service
public class SampleQuestionService {
    private final SampleQuestionRepository sampleQuestionRepository;
    private final OrganisationService organisationService;
    private final StudyProgramService studyProgramService;
    private final AngelosService angelosService;

    public SampleQuestionService(SampleQuestionRepository sampleQuestionRepository,
                                 OrganisationService organisationService,
                                 StudyProgramService studyProgramService,
                                 AngelosService angelosService) {
        this.sampleQuestionRepository = sampleQuestionRepository;
        this.organisationService = organisationService;
        this.studyProgramService = studyProgramService;
        this.angelosService = angelosService;
    }

    public List<SampleQuestionDTO> getAllSampleQuestionsByOrgId(Long orgId) {
        return sampleQuestionRepository.findByOrganisationOrgID(orgId).stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public SampleQuestionDTO getSampleQuestionById(Long id) {
        SampleQuestion sampleQuestion = sampleQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SampleQuestion not found with id " + id));
        return this.convertToDto(sampleQuestion);
    }

    @Transactional
    public SampleQuestionDTO addSampleQuestion(Long orgId, SampleQuestionDTO sampleQuestionDTO) {
        // Fetch Organisation
        Organisation organisation = organisationService.getOrganisationById(orgId);

        // Map DTO to Entity
        SampleQuestion sampleQuestion = new SampleQuestion();
        sampleQuestion.setTopic(sampleQuestionDTO.getTopic());
        sampleQuestion.setQuestion(sampleQuestionDTO.getQuestion());
        sampleQuestion.setAnswer(sampleQuestionDTO.getAnswer());
        sampleQuestion.setOrganisation(organisation);

        // Fetch Study Programs
        List<StudyProgram> studyPrograms = studyProgramService.getStudyProgramsByIds(
                sampleQuestionDTO.getStudyPrograms().stream().map(StudyProgramDTO::getId).collect(Collectors.toList())
        );
        sampleQuestion.setStudyPrograms(studyPrograms);

        boolean success = angelosService.sendSampleQuestionAddRequest(sampleQuestionDTO);
        if (!success) {
            throw new RuntimeException("Failed to send add request to Angelos RAG system.");
        }
        // Save to database
        SampleQuestion savedSampleQuestion = sampleQuestionRepository.save(sampleQuestion);

        return convertToDto(savedSampleQuestion);
    }

    @Transactional
    public SampleQuestionDTO editSampleQuestion(Long orgId, Long sampleQuestionId, SampleQuestionDTO sampleQuestionDTO) {
        // Fetch the existing SampleQuestion by ID
        SampleQuestion existingSampleQuestion = sampleQuestionRepository.findById(sampleQuestionId)
                .orElseThrow(() -> new ResourceNotFoundException("SampleQuestion not found with id " + sampleQuestionId));
    
        // Ensure the sample question belongs to the organisation
        if (!existingSampleQuestion.getOrganisation().getOrgID().equals(orgId)) {
            throw new UnauthorizedException("You are not authorized to edit this sample question.");
        }

        // Update fields
        existingSampleQuestion.setTopic(sampleQuestionDTO.getTopic());
        existingSampleQuestion.setQuestion(sampleQuestionDTO.getQuestion());
        existingSampleQuestion.setAnswer(sampleQuestionDTO.getAnswer());

        // Update Study Programs
        List<StudyProgram> newStudyPrograms = studyProgramService.getStudyProgramsByIds(
                sampleQuestionDTO.getStudyPrograms().stream().map(StudyProgramDTO::getId).collect(Collectors.toList())
        );

        Set<Long> existingStudyProgramIds = existingSampleQuestion.getStudyPrograms().stream()
                .map(StudyProgram::getSpID)
                .collect(Collectors.toSet());

        Set<Long> newStudyProgramIds = newStudyPrograms.stream()
                .map(StudyProgram::getSpID)
                .collect(Collectors.toSet());

        if (!existingStudyProgramIds.equals(newStudyProgramIds)) {
            existingSampleQuestion.setStudyPrograms(newStudyPrograms);
        }

        boolean success = angelosService.sendSampleQuestionEditRequest(sampleQuestionDTO);
        if (!success) {
            throw new RuntimeException("Failed to send update request to Angelos RAG system.");
        }

        // Save the updated entity to the database
        SampleQuestion updatedSampleQuestion = sampleQuestionRepository.save(existingSampleQuestion);

        // Map Entity to Response DTO
        return convertToDto(updatedSampleQuestion);
    }

    public void deleteSampleQuestion(Long id, Long orgId) {
        if (sampleQuestionRepository.existsById(id)) {
            SampleQuestion existingSampleQuestion = sampleQuestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SampleQuestion not found with id " + id));
            if (!existingSampleQuestion.getOrganisation().getOrgID().equals(orgId)) {
                throw new UnauthorizedException("You are not authorized to delete this sample question.");
            } else {
                boolean success = angelosService.sendSampleQuestionDeleteRequest(id);
                if (!success) {
                    throw new RuntimeException("Failed to send delete request to Angelos RAG system.");
                }
                sampleQuestionRepository.deleteById(id);
            }
        } else {
            throw new ResourceNotFoundException("SampleQuestion not found with id " + id);
        }
    }

    public SampleQuestionDTO convertToDto(SampleQuestion sampleQuestion) {
        SampleQuestionDTO dto = new SampleQuestionDTO();
        dto.setId(sampleQuestion.getSqID());
        dto.setTopic(sampleQuestion.getTopic());
        dto.setQuestion(sampleQuestion.getQuestion());
        dto.setAnswer(sampleQuestion.getAnswer());

        // Map study programs to DTOs (assuming StudyProgramDTO and conversion exist)
        List<StudyProgramDTO> studyProgramDTOs = sampleQuestion.getStudyPrograms().stream()
                .map(sp -> new StudyProgramDTO(sp.getSpID(), sp.getName()))
                .collect(Collectors.toList());
        dto.setStudyPrograms(studyProgramDTOs);

        return dto;
    }
}
