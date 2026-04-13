package com.uniquindio.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record PedidoRequest(

        @NotEmpty(message = "El pedido debe tener al menos un producto")
        @Valid
        List<ItemCarritoRequest> items,

        @Size(max = 500, message = "La dirección no puede superar los 500 caracteres")
        String direccionEnvio
) {
}
