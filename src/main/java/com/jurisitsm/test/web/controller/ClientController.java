package com.jurisitsm.test.web.controller;

import com.jurisitsm.test.exception.ClientManagerException;
import com.jurisitsm.test.service.ClientService;
import com.jurisitsm.test.web.dto.ClientRequest;
import com.jurisitsm.test.web.dto.ClientResponse;
import com.jurisitsm.test.web.dto.ClientUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/client")
public class ClientController {

    private final ClientService clientService;

    @Autowired
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClientById(@PathVariable String id) throws ClientManagerException {
        return ResponseEntity.ok(new ClientResponse(clientService.getClientById(id)));
    }

    @GetMapping("/all")
    public ResponseEntity<Collection<ClientResponse>> getAllClients() {
        return ResponseEntity.ok(clientService.findAll().stream().map(ClientResponse::new)
                .collect(Collectors.toList()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClientById(@PathVariable String id) {
        clientService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Void> createClient(@RequestBody ClientRequest clientRequest) {
        clientService.createClient(clientRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateClient(@PathVariable String id, @RequestBody ClientUpdateRequest clientRequest) throws ClientManagerException {
        clientService.updateClient(id, clientRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/average")
    public ResponseEntity<BigDecimal> getAverageAgeOfClients() {
        return ResponseEntity.ok(clientService.getAverageAgeOfClients());
    }

    @GetMapping("/age-range")
    public ResponseEntity<List<ClientResponse>> getClientsInAgeRange() {
        return ResponseEntity.ok(clientService.getClientsInAgeRange().stream().map(ClientResponse::new)
                .collect(Collectors.toList()));
    }
}
