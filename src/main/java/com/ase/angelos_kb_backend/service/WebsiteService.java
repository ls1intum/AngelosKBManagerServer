package com.ase.angelos_kb_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ase.angelos_kb_backend.dto.StudyProgramDTO;
import com.ase.angelos_kb_backend.dto.WebsiteRequestDTO;
import com.ase.angelos_kb_backend.dto.WebsiteResponseDTO;
import com.ase.angelos_kb_backend.exception.ResourceNotFoundException;
import com.ase.angelos_kb_backend.exception.UnauthorizedException;
import com.ase.angelos_kb_backend.model.Organisation;
import com.ase.angelos_kb_backend.model.StudyProgram;
import com.ase.angelos_kb_backend.model.WebsiteContent;
import com.ase.angelos_kb_backend.repository.WebsiteContentRepository;
import com.ase.angelos_kb_backend.util.ParseResult;


@Service
public class WebsiteService {

    private final WebsiteContentRepository websiteContentRepository;
    private final OrganisationService organisationService;
    private final StudyProgramService studyProgramService;
    private final AngelosService angelosService;
    private final ParsingService parsingService;

    public WebsiteService(WebsiteContentRepository websiteContentRepository,
                          OrganisationService organisationService,
                          StudyProgramService studyProgramService,
                          AngelosService angelosService,
                          ParsingService parsingService) {
        this.websiteContentRepository = websiteContentRepository;
        this.organisationService = organisationService;
        this.studyProgramService = studyProgramService;
        this.angelosService = angelosService;
        this.parsingService = parsingService;
    }

    public List<WebsiteResponseDTO> getAllWebsitesByOrgId(Long orgId) {
        List<WebsiteContent> websites = websiteContentRepository.findByOrganisationOrgID(orgId);
        return websites.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    public WebsiteResponseDTO getWebsiteById(Long id) {
        WebsiteContent websiteContent = websiteContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Website not found with id " + id));
        return this.convertToDto(websiteContent);
    }

    @Transactional
    public WebsiteResponseDTO addWebsite(Long orgId, WebsiteRequestDTO websiteRequestDTO) {
        // Fetch Organisation
        Organisation organisation = organisationService.getOrganisationById(orgId);

        // Map DTO to Entity
        WebsiteContent websiteContent = new WebsiteContent();
        websiteContent.setTitle(websiteRequestDTO.getTitle());
        websiteContent.setLink(websiteRequestDTO.getLink());
        websiteContent.setOrganisation(organisation);

        // Fetch Study Programs
        List<StudyProgram> studyPrograms = studyProgramService.getStudyProgramsByIds(websiteRequestDTO.getStudyProgramIds());
        websiteContent.setStudyPrograms(studyPrograms);

        // Parse website content
        ParseResult parseResult = parsingService.parseWebsite(websiteContent.getLink());
        String parsedContent = parseResult.getContent();
        String websiteType = parseResult.getParserType();

        // Compute content hash
        String contentHash = parsingService.computeContentHash(parsedContent);
        websiteContent.setContentHash(contentHash);

        // Save to database
        WebsiteContent savedWebsite = websiteContentRepository.save(websiteContent);

        // Send add request to Angelos RAG
        boolean success = angelosService.sendWebsiteAddRequest(savedWebsite.getId(), websiteRequestDTO, parsedContent, websiteType);

        if (!success) {
            throw new RuntimeException("Failed to send add request to Angelos RAG system.");
        }

        // Map Entity to Response DTO
        return convertToDto(savedWebsite);
    }

    @Transactional
    public WebsiteResponseDTO editWebsite(Long orgId, Long websiteId, WebsiteRequestDTO websiteRequestDTO) {
        // Fetch the existing WebsiteContent by ID
        WebsiteContent existingWebsite = websiteContentRepository.findById(websiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Website not found with id " + websiteId));
    
        // Ensure the website belongs to the organisation
        if (!existingWebsite.getOrganisation().getOrgID().equals(orgId)) {
            throw new UnauthorizedException("You are not authorized to edit this website.");
        }

        String existingTitle = existingWebsite.getTitle();
        existingWebsite.setTitle(websiteRequestDTO.getTitle());

        List<StudyProgram> newStudyPrograms = studyProgramService.getStudyProgramsByIds(websiteRequestDTO.getStudyProgramIds());

        // Compare existing and new StudyProgram IDs
        Set<Long> existingStudyProgramIds = existingWebsite.getStudyPrograms().stream().map(StudyProgram::getSpID).collect(Collectors.toSet());
        Set<Long> newStudyProgramIds = newStudyPrograms.stream().map(StudyProgram::getSpID).collect(Collectors.toSet());

        if (!existingTitle.equals(websiteRequestDTO.getTitle()) || !existingStudyProgramIds.equals(newStudyProgramIds)) {
            // Convert StudyProgram entities to DTOs
            boolean success = angelosService.sendWebsiteUpdateRequest(websiteId, websiteRequestDTO.getTitle(), new ArrayList<Long>(newStudyProgramIds));
            if (!success) {
                throw new RuntimeException("Failed to send update request to Angelos RAG system.");
            }
            existingWebsite.setStudyPrograms(newStudyPrograms);
        }
    
        // Parse website content
        String parsedContent = parsingService.parseWebsite(existingWebsite.getLink()).getContent();

        // Compute content hash
        String contentHash = parsingService.computeContentHash(parsedContent);
            
        // Check if content has actually changed
        if (!contentHash.equals(existingWebsite.getContentHash())) {
            // Content has changed, send update request to Angelos RAG
            boolean success = angelosService.sendWebsiteRefreshRequest(existingWebsite.getId(), parsedContent);
            if (!success) {
                throw new RuntimeException("Failed to send update request to Angelos RAG system.");
            }
            existingWebsite.setContentHash(contentHash);
        }
        // Save the updated entity to the database
        WebsiteContent updatedWebsite = websiteContentRepository.save(existingWebsite);
    
        // Map Entity to Response DTO
        return convertToDto(updatedWebsite);
    }

    public void deleteWebsite(Long id, Long orgId) {
        if (websiteContentRepository.existsById(id)) {
            WebsiteContent existingWebsite = websiteContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Website not found with id " + id));
            if (!existingWebsite.getOrganisation().getOrgID().equals(orgId)) {
                throw new UnauthorizedException("You are not authorized to edit this website.");
            } else {
                angelosService.sendWebsiteDeleteRequest(id);
                websiteContentRepository.deleteById(id);
            }
        } else {
            throw new ResourceNotFoundException("Website not found with id " + id);
        }
    }

    public WebsiteResponseDTO convertToDto(WebsiteContent websiteContent) {
        WebsiteResponseDTO dto = new WebsiteResponseDTO();
        dto.setId(websiteContent.getId());
        dto.setTitle(websiteContent.getTitle());
        dto.setLink(websiteContent.getLink());
        dto.setLastUpdated(websiteContent.getUpdatedAt());

        // Map study programs to DTOs (assuming StudyProgramDTO and conversion exist)
        List<StudyProgramDTO> studyProgramDTOs = websiteContent.getStudyPrograms().stream()
                .map(sp -> new StudyProgramDTO(sp.getSpID(), sp.getName()))
                .collect(Collectors.toList());
        dto.setStudyPrograms(studyProgramDTOs);

        return dto;
    }
}
