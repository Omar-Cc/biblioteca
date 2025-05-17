package com.biblioteca.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GeneroResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Long parentId;
    private Integer nivel;
    private List<GeneroResponseDTO> subgeneros;
}
