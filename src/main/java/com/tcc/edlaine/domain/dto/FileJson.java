package com.tcc.edlaine.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileJson {
    private String id;
    private String fileName;
}
