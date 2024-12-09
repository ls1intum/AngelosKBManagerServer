package com.ase.angelos_kb_backend.controller;

import com.ase.angelos_kb_backend.dto.OrganisationDTO;
import com.ase.angelos_kb_backend.service.OrganisationService;
import com.ase.angelos_kb_backend.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organisations")
public class OrganisationController {

    private final OrganisationService organisationService;
    private final JwtUtil jwtUtil;

    public OrganisationController(OrganisationService organisationService, JwtUtil jwtUtil) {
        this.organisationService = organisationService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Add a new organisation. Only accessible by system admin.
     */
    @PostMapping
    public ResponseEntity<OrganisationDTO> addOrganisation(
            @RequestHeader("Authorization") String token,
            @RequestParam String name) {
        // Verify system admin access
        if (!jwtUtil.extractIsSystemAdmin(token.replace("Bearer ", ""))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        OrganisationDTO newOrganisation = organisationService.addOrganisation(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(newOrganisation);
    }

    /**
     * Remove an organisation by ID. Only accessible by system admin.
     */
    @DeleteMapping("/{orgId}")
    public ResponseEntity<String> removeOrganisation(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orgId) {
        // Verify system admin access
        if (!jwtUtil.extractIsSystemAdmin(token.replace("Bearer ", ""))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied.");
        }

        boolean isRemoved = organisationService.removeOrganisation(orgId);
        if (isRemoved) {
            return ResponseEntity.ok("Organisation removed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Organisation not found.");
        }
    }
}
