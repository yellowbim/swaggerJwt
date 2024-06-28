package org.jjuni.swaggerjwt.auth.repository;

import org.jjuni.swaggerjwt.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
}
