package com.ase.angelos_kb_backend.service;

import org.springframework.stereotype.Service;

import com.ase.angelos_kb_backend.dto.OrganisationDTO;
import com.ase.angelos_kb_backend.exception.ResourceNotFoundException;
import com.ase.angelos_kb_backend.model.Organisation;
import com.ase.angelos_kb_backend.repository.OrganisationRepository;

@Service
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    public OrganisationService(OrganisationRepository organisationRepository) {
        this.organisationRepository = organisationRepository;
    }

    public Organisation getOrganisationById(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation not found with ID: " + id));
    }

    public OrganisationDTO addOrganisation(String name) {
        Organisation organisation = new Organisation();
        organisation.setName(name);

        Organisation savedOrganisation = organisationRepository.save(organisation);
        return convertToDto(savedOrganisation);
    }

    public boolean removeOrganisation(Long orgId) {
        if (organisationRepository.existsById(orgId)) {
            organisationRepository.deleteById(orgId);
            return true;
        } else {
            throw new ResourceNotFoundException("Organisation not found with id " + orgId);
        }
    }

    private OrganisationDTO convertToDto(Organisation organisation) {
        OrganisationDTO dto = new OrganisationDTO();
        dto.setId(organisation.getOrgID());
        dto.setName(organisation.getName());
        return dto;
    }
}
