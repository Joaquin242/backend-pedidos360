package com.example.demo.controller;

import com.example.demo.model.Cliente;
import com.example.demo.model.Pedido;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.repository.PedidoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
@PreAuthorize("hasAuthority('SCOPE_Pedidos.Access')")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    public PedidoController(PedidoRepository pedidoRepository, ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtener(@PathVariable Long id) {
        return pedidoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@Valid @RequestBody Pedido pedido) {
        pedido.setId(null);

        // si el cliente ya existe (mismo email) se reutiliza, si no se crea
        Cliente datos = pedido.getCliente();
        Cliente cliente = clienteRepository.findByEmail(datos.getEmail()).orElseGet(() -> {
            datos.setId(null);
            return clienteRepository.save(datos);
        });
        pedido.setCliente(cliente);

        return pedidoRepository.save(pedido);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Pedido> cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String estado = body.get("estado");
        if (estado == null || estado.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return pedidoRepository.findById(id).map(p -> {
            p.setEstado(estado);
            return ResponseEntity.ok(pedidoRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    // el @PreAuthorize del metodo reemplaza al de la clase, por eso lleva las dos condiciones
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_Pedidos.Access') and hasRole('Admin')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pedidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
