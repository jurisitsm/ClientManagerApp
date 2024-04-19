package com.jurisitsm.test.web.dto;

import java.time.LocalDate;

public record ClientUpdateRequest (
        String name,
        String email,
        LocalDate dateOfBirth
){}
