package com.uniquindio.backend.service;

import com.uniquindio.backend.dto.*;
import com.uniquindio.backend.model.*;
import com.uniquindio.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InventarioService inventarioService;

    /**
     * Crea un pedido a partir del carrito del cliente.
     * Valida stock, descuenta unidades y calcula totales.
     */
    @Transactional
    public PedidoResponse crearPedido(String emailUsuario, PedidoRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (ItemCarritoRequest item : request.items()) {
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Producto no encontrado: ID " + item.productoId()));

            if (!producto.isActivo()) {
                throw new RuntimeException("El producto '" + producto.getNombre() + "' no está disponible");
            }

            if (producto.getStock() < item.cantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para '" + producto.getNombre() +
                                "'. Disponible: " + producto.getStock() +
                                ", solicitado: " + item.cantidad());
            }

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.cantidad()));

            DetallePedido detalle = DetallePedido.builder()
                    .producto(producto)
                    .cantidad(item.cantidad())
                    .precioUnitario(producto.getPrecio())
                    .subtotal(subtotal)
                    .build();

            detalles.add(detalle);
            total = total.add(subtotal);

            // Descontar stock
            producto.setStock(producto.getStock() - item.cantidad());
            productoRepository.save(producto);
            inventarioService.sincronizarProducto(producto);
        }

        Pedido pedido = Pedido.builder()
                .usuario(usuario)
                .estado(EstadoPedido.PENDIENTE)
                .total(total)
                .direccionEnvio(request.direccionEnvio())
                .build();

        // Guardar primero sin detalles para obtener el ID
        pedido = pedidoRepository.save(pedido);

        // Asociar cada detalle al pedido
        for (DetallePedido detalle : detalles) {
            detalle.setPedido(pedido);
        }
        pedido.setDetalles(detalles);
        pedido = pedidoRepository.save(pedido);

        return toResponse(pedido);
    }

    /**
     * Lista los pedidos del usuario autenticado.
     */
    public List<PedidoResponse> listarMisPedidos(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream().map(this::toResponse).toList();
    }

    /**
     * Lista todos los pedidos (solo admin).
     */
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAllByOrderByFechaCreacionDesc()
                .stream().map(this::toResponse).toList();
    }

    /**
     * Obtiene un pedido por ID.
     */
    public PedidoResponse obtenerPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        return toResponse(pedido);
    }

    /**
     * Actualiza el estado de un pedido (solo admin).
     */
    @Transactional
    public PedidoResponse actualizarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        EstadoPedido estado;
        try {
            estado = EstadoPedido.valueOf(nuevoEstado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + nuevoEstado);
        }

        // Si se cancela, devolver stock
        if (estado == EstadoPedido.CANCELADO && pedido.getEstado() != EstadoPedido.CANCELADO) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
                inventarioService.sincronizarProducto(producto);
            }
        }

        pedido.setEstado(estado);
        return toResponse(pedidoRepository.save(pedido));
    }

    /**
     * Genera la factura de un pedido pagado.
     */
    public FacturaResponse obtenerFactura(String emailUsuario, Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!pedido.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permiso para ver esta factura");
        }

        if (pedido.getEstado() != EstadoPedido.PAGADO
                && pedido.getEstado() != EstadoPedido.CONFIRMADO
                && pedido.getEstado() != EstadoPedido.ENVIADO
                && pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new RuntimeException("La factura solo está disponible para pedidos pagados");
        }

        List<DetallePedidoResponse> detallesDto = pedido.getDetalles() != null
                ? pedido.getDetalles().stream().map(d -> new DetallePedidoResponse(
                d.getId(),
                d.getProducto().getId(),
                d.getProducto().getNombre(),
                d.getProducto().getImagenUrl(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getSubtotal()
        )).toList()
                : List.of();

        Usuario usuario = pedido.getUsuario();
        return new FacturaResponse(
                pedido.getId(),
                usuario.getNombre() + " " + usuario.getApellido(),
                usuario.getEmail(),
                usuario.getCedula(),
                usuario.getTelefono(),
                pedido.getDireccionEnvio(),
                pedido.getEstado().name(),
                pedido.getMercadoPagoPaymentId(),
                pedido.getTotal(),
                detallesDto,
                pedido.getFechaActualizacion(),
                pedido.getFechaCreacion()
        );
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<DetallePedidoResponse> detallesDto = pedido.getDetalles() != null
                ? pedido.getDetalles().stream().map(d -> new DetallePedidoResponse(
                d.getId(),
                d.getProducto().getId(),
                d.getProducto().getNombre(),
                d.getProducto().getImagenUrl(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getSubtotal()
        )).toList()
                : List.of();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getEstado().name(),
                pedido.getTotal(),
                pedido.getDireccionEnvio(),
                pedido.getUsuario().getNombre() + " " + pedido.getUsuario().getApellido(),
                pedido.getUsuario().getEmail(),
                detallesDto,
                pedido.getFechaCreacion(),
                pedido.getFechaActualizacion()
        );
    }
}
