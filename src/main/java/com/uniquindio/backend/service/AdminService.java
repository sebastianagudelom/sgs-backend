package com.uniquindio.backend.service;

import com.uniquindio.backend.dto.ClienteResponse;
import com.uniquindio.backend.model.Pedido;
import com.uniquindio.backend.model.Rol;
import com.uniquindio.backend.model.Usuario;
import com.uniquindio.backend.repository.PedidoRepository;
import com.uniquindio.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;

    public List<ClienteResponse> listarClientes() {
        List<Usuario> clientes = usuarioRepository.findByRolOrderByFechaCreacionDesc(Rol.CLIENTE);

        return clientes.stream().map(usuario -> {
            List<Pedido> pedidos = pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId());
            long totalPedidos = pedidos.size();
            BigDecimal totalGastado = pedidos.stream()
                    .map(Pedido::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new ClienteResponse(
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getEmail(),
                    usuario.getCedula(),
                    usuario.getTelefono(),
                    usuario.isActivo(),
                    totalPedidos,
                    totalGastado,
                    usuario.getFechaCreacion()
            );
        }).toList();
    }
}
