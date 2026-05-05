package org.example.invoicemanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.invoicemanagement.dto.ClientRequestDTO;
import org.example.invoicemanagement.dto.ClientResponseDTO;
import org.example.invoicemanagement.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients", description = "APIs for creating, updating, deleting, searching, and fetching clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService){

        this.clientService = clientService;
    }

    @Operation(summary = "Create a new client")
    @PostMapping
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO clientRequestDTO){

        ClientResponseDTO createdClient = clientService.createClient(clientRequestDTO);

        return new ResponseEntity<>(createdClient, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all clients")
    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAllClients(){

        List<ClientResponseDTO> clients = clientService.getAllClients();

        return ResponseEntity.ok(clients);
    }

    @Operation(summary = "Get client by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable Long id){

        ClientResponseDTO client = clientService.getClientById(id);

        return ResponseEntity.ok(client);
    }

    @Operation(summary = "Update client by ID")
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> updateClientById(@PathVariable Long id, @Valid @RequestBody ClientRequestDTO requestDTO){

        ClientResponseDTO client = clientService.updateClientById(id,requestDTO);

        return ResponseEntity.ok(client);
    }

    @Operation(summary = "Delete client by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClientById(@PathVariable Long id){

        clientService.deleteClientById(id);

        return ResponseEntity.noContent().build();

    }

    @Operation(summary = "Search clients by name")
    @GetMapping("/search")
    public ResponseEntity<List<ClientResponseDTO>> searchClients(@RequestParam String name){

        List<ClientResponseDTO> clients = clientService.searchClient(name);

        return ResponseEntity.ok(clients);
    }

}
