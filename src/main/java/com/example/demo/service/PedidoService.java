package com.example.demo.service;

import com.example.demo.model.Cliente;
import com.example.demo.model.Pedido;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Logica de negocio de pedidos. El controller solo delega aqui.
 * (EP3: aqui se engancha el productor de mensajes RabbitMQ al crear / cambiar estado.)
 */
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> obtener(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional
    public Pedido crear(Pedido pedido) {
        pedido.setId(null);
        Cliente datos = pedido.getCliente();
        Cliente cliente = clienteRepository.findByEmail(datos.getEmail()).orElseGet(() -> {
            datos.setId(null);
            return clienteRepository.save(datos);
        });
        pedido.setCliente(cliente);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Optional<Pedido> cambiarEstado(Long id, String estado) {
        return pedidoRepository.findById(id).map(p -> {
            p.setEstado(estado);
            return pedidoRepository.save(p);
        });
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            return false;
        }
        pedidoRepository.deleteById(id);
        return true;
    }
}