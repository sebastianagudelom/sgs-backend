package com.uniquindio.backend.repository;

import com.uniquindio.backend.model.Pedido;
import com.uniquindio.backend.model.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Pedido> findByEstadoOrderByFechaCreacionDesc(EstadoPedido estado);

    List<Pedido> findAllByOrderByFechaCreacionDesc();

    long countByUsuarioId(Long usuarioId);
}
