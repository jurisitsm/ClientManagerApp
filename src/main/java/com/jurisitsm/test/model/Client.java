package com.jurisitsm.test.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name="client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, name = "date_of_birth")
    private LocalDate dateOfBirth;

    public Client(){}

    public Client(String email, String name, LocalDate dateOfBirth) {
        this.email = email;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }
    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear() -
                (LocalDate.now().getDayOfYear() < dateOfBirth.getDayOfYear() ? 1 : 0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
