package com.jurisitsm.test.repository;

import com.jurisitsm.test.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {

    @Query("SELECT AVG(YEAR(CURRENT_DATE) - YEAR(c.dateOfBirth)) FROM Client c")
    BigDecimal calculateAverageAge();

}
