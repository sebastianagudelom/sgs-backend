package com.uniquindio.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String imagenUrl,
        boolean activo,
        Long categoriaId,
        String categoriaNombre,
        LocalDateTime fechaCreacion
) {
}
