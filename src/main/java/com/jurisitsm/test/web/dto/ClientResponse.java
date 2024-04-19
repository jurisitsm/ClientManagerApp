package com.jurisitsm.test.web.dto;

import com.jurisitsm.test.model.Client;

import java.time.LocalDate;

public record ClientResponse(
        String id,
        String name,
        String email,
        LocalDate dateOfBirth
) {
    public ClientResponse(Client client){
        this(client.getId(), client.getName(), client.getEmail(), client.getDateOfBirth());
    }
}
