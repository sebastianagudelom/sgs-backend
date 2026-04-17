package com.uniquindio.backend.dto;

public record DireccionResponse(
        Long id,
        String nombre,
        String direccion,
        boolean predeterminada
) {
}
