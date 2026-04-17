package com.uniquindio.backend.dto;

import jakarta.validation.constraints.*;

public record DireccionRequest(

        @NotBlank(message = "El nombre de la dirección es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(min = 10, max = 500, message = "La dirección debe tener entre 10 y 500 caracteres")
        String direccion,

        boolean predeterminada
) {
}
