package com.jurisitsm.test.repository;

import com.jurisitsm.test.model.AppUser;
import com.jurisitsm.test.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUser(AppUser user);

    @Modifying
    void deleteByUser(AppUser user);
}
