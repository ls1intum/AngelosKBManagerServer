package com.ase.angelos_kb_backend.model;


import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Data;

@Entity
@Data
public class StudyProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long spID;

    private String name;

    @ManyToMany(mappedBy = "studyPrograms")
    private List<Organisation> organisations;

    @ManyToMany(mappedBy = "studyPrograms")
    private List<WebsiteContent> websites;

    @ManyToMany(mappedBy = "studyPrograms")
    private List<DocumentContent> documents;
}
