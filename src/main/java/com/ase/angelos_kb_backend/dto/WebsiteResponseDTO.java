package com.ase.angelos_kb_backend.dto;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WebsiteResponseDTO {
    private Long id;
    private String title;
    private String link;
    private List<StudyProgramDTO> studyPrograms;
}
