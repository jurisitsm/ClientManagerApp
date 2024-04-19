package com.jurisitsm.test.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record ClientRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank LocalDate dateOfBirth
){}
