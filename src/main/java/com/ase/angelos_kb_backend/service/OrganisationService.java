package com.ase.angelos_kb_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ase.angelos_kb_backend.dto.OrganisationDTO;
import com.ase.angelos_kb_backend.dto.eunomnia.MailCredentialsDTO;
import com.ase.angelos_kb_backend.exception.ResourceNotFoundException;
import com.ase.angelos_kb_backend.model.Organisation;
import com.ase.angelos_kb_backend.repository.OrganisationRepository;
import com.ase.angelos_kb_backend.util.MailStatus;

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

    public List<OrganisationDTO> getAllOrganisations() {
        List<Organisation> organisations = organisationRepository.findAll();
        return organisations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public void setCredentials(Long orgId, MailCredentialsDTO credentialsDTO) {
        Organisation org = this.getOrganisationById(orgId);
        
        org.setMailAccount(credentialsDTO.getMailAccount());
        org.setMailStatus(MailStatus.INACTIVE);

        organisationRepository.save(org);
    }

    public String getCredentials(Long orgId) {
        Organisation org = this.getOrganisationById(orgId);
        return org.getMailAccount();
    }

    public void setMailStatus(Long orgId, MailStatus status) {
        Organisation org = this.getOrganisationById(orgId);
        org.setMailStatus(status);

        organisationRepository.save(org);
    }

    public List<OrganisationDTO> getOrganisationsForMail() {
        List<OrganisationDTO> result = organisationRepository.findAll().stream()
            .filter(a -> !a.getMailAccount().equals(null))
            .map(this::convertToDto)
            .collect(Collectors.toList());
        return result;
    }
         

    private OrganisationDTO convertToDto(Organisation organisation) {
        OrganisationDTO dto = new OrganisationDTO();
        dto.setId(organisation.getOrgID());
        dto.setName(organisation.getName());
        return dto;
    }
}
