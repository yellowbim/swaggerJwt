package org.jjuni.swaggerjwt.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jjuni.swaggerjwt.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Comment("사용자별 Refresh Token 테이블")
@Table(name = "tb_user_refresh_token")
public class RefreshTokenEntity {

    @Id
    @Comment("Refresh token seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @MapsId
    @Comment("아이디")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", unique = true)
    private Member member;

    @Comment("리프레시 토큰 정보")
    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String refreshToken;

    @Comment("리프레시 토큰 만료일")
    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String refreshTokenExpireDate;

    @Comment("생성일")
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created;

    @Comment("수정일")
    @UpdateTimestamp
    @Column(updatable = false)
    private LocalDateTime updated;

    public RefreshTokenEntity(Member member, String refreshToken) {
        this.member = member;
        this.refreshToken = refreshToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
