package com.ase.angelos_kb_backend.dto.angelos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AngelosAddWebsiteRequest {
    private Long id;
    private String title;
    private String link;
    private List<Long> studyProgramIds;
    private String content;
    private String type;
}
