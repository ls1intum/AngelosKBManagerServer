package com.ase.angelos_kb_backend.dto.eunomnia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailResponseRequestDTO {
    Long org_id;
    String message;
    String study_program;
    String language;
}
