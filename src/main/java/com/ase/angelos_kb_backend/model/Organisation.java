package com.ase.angelos_kb_backend.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long orgID;

    private String name;

    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users;

    @ManyToMany
    @JoinTable(
        name = "OrgaStudyPrograms",
        joinColumns = @JoinColumn(name = "org_id"),
        inverseJoinColumns = @JoinColumn(name = "sp_id")
    )
    private List<StudyProgram> studyPrograms;

    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WebsiteContent> websites;

    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentContent> documents;

    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SampleQuestion> sampleQuestions;
}
