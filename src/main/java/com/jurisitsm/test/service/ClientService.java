package com.jurisitsm.test.service;

import com.jurisitsm.test.exception.ClientManagerException;
import com.jurisitsm.test.model.Client;
import com.jurisitsm.test.repository.ClientRepository;
import com.jurisitsm.test.web.dto.ClientRequest;
import com.jurisitsm.test.web.dto.ClientUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client getClientById(String id) throws ClientManagerException {
        return clientRepository.findById(id)
                .orElseThrow(() ->
                        new ClientManagerException("User with given id could not be found.", HttpStatus.NOT_FOUND));
    }

    public Collection<Client> findAll(){
        return clientRepository.findAll();
    }

    public void deleteById(String id) {
        clientRepository.deleteById(id);
    }

    public void createClient(ClientRequest clientRequest){
        clientRepository.save(new Client(
                clientRequest.email(),
                clientRequest.name(),
                clientRequest.dateOfBirth()
        ));
    }

    public void updateClient(String id, ClientUpdateRequest clientRequest) throws ClientManagerException {
        Client client = getClientById(id);
        if (clientRequest.dateOfBirth() != null)
            client.setDateOfBirth(clientRequest.dateOfBirth());
        if (clientRequest.email() != null)
            client.setEmail(clientRequest.email());
        if (clientRequest.name() != null)
            client.setName(clientRequest.name());
        clientRepository.save(client);
    }

    public BigDecimal getAverageAgeOfClients(){
        return clientRepository.calculateAverageAge();
    }

    public List<Client> getClientsInAgeRange(){
        return clientRepository.findAll().stream()
                .filter(client -> {
                    int age = client.getAge();
                    return age >= 18 && age <= 40;
                })
                .collect(Collectors.toList());
    }
}
