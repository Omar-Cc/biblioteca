package com.biblioteca.models.contenido;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutorObraId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long obraId;
    private Long autorId;
}