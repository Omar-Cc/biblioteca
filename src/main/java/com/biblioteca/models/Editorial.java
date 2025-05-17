package com.biblioteca.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Editorial {
    private Long id;
    private String nombre;
    private String pais;
    private String website;
}
