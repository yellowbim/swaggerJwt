package org.jjuni.swaggerjwt.auth.repository;

import org.jjuni.swaggerjwt.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    // Member에 있는 id(seq) 로 조회하는 쿼리
    Optional<RefreshTokenEntity> findById(Long id);
}
