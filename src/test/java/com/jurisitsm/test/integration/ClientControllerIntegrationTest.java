package com.jurisitsm.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jurisitsm.test.model.Client;
import com.jurisitsm.test.repository.ClientRepository;
import com.jurisitsm.test.web.dto.ClientRequest;
import com.jurisitsm.test.web.dto.ClientUpdateRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"spring.profiles.active=test"})
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username="test@example.com", roles={"USER"})
public class ClientControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ClientRepository clientRepository;
    private final ObjectMapper objectMapper;
    private Client client;

    @Autowired
    public ClientControllerIntegrationTest(MockMvc mockMvc, ClientRepository clientRepository) {
        this.mockMvc = mockMvc;
        this.clientRepository = clientRepository;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    public void setup() {
        clientRepository.deleteAll();
        client = new Client("testclient@example.com", "Test Client",
                LocalDate.now().minusYears(55));
        clientRepository.save(client);
    }

    @Test
    public void testGetClientById() throws Exception {
        mockMvc.perform(get("/client/" + client.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(client.getId()));
    }

    @Test
    public void testGetAllClients() throws Exception {
        mockMvc.perform(get("/client/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(client.getId()));
    }

    @Test
    public void testDeleteClientById() throws Exception {
        mockMvc.perform(delete("/client/" + client.getId()))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateClient() throws Exception {
        ClientRequest clientRequest = new ClientRequest("new client", "newclient@example.com",
                LocalDate.of(2000, 5, 15));
        mockMvc.perform(post("/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testCreateClientWithInvalidEmail() throws Exception {
        ClientRequest clientRequest = new ClientRequest("newclient", "invalid email",
                LocalDate.of(2000, 5, 15));
        mockMvc.perform(post("/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateClient() throws Exception {
        ClientUpdateRequest clientUpdateRequest = new ClientUpdateRequest("updatedclient@example.com",
                "Updated Client", LocalDate.of(1995, 12, 12));
        mockMvc.perform(put("/client/" + client.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clientUpdateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetAverageAgeOfClients() throws Exception {
        Client youngClient = new Client("young@example.com", "Young Client", LocalDate.now().minusYears(10));
        Client oldClient = new Client("old@example.com", "Old Client", LocalDate.now().minusYears(50));
        clientRepository.save(youngClient);
        clientRepository.save(oldClient);

        mockMvc.perform(get("/client/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(38));
    }

    @Test
    public void testGetClientsInAgeRange() throws Exception {
        Client client15 = new Client("client18@example.com", "Client Age 15", LocalDate.now().minusYears(15));
        Client client25 = new Client("client25@example.com", "Client Age 25", LocalDate.now().minusYears(25));
        Client client40 = new Client("client40@example.com", "Client Age 40", LocalDate.now().minusYears(40));
        Client client50 = new Client("client50@example.com", "Client Age 50", LocalDate.now().minusYears(50));
        clientRepository.save(client15);
        clientRepository.save(client25);
        clientRepository.save(client40);
        clientRepository.save(client50);

        mockMvc.perform(get("/client/age-range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
