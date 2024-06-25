package org.jjuni.swaggerjwt.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jjuni.swaggerjwt.auth.enums.RoleType;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Builder
@Table(name = "tb_user")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // jpa 자동생성
    private Long id;

    @Comment("아이디")
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private String userId;

    @Comment("비밀번호")
    @Column(nullable = false)
    private String password;

    @Comment("이름")
    @Column(columnDefinition = "varchar(10)", nullable = false)
    private String name;

    @Comment("전화번호")
    @Column(columnDefinition = "varchar(14)")
    private String phoneNum;

    @Comment("이메일")
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private String email;

    // JWT 사용자 권한 추가
    @Comment("권한")
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50)", nullable = false)
    private RoleType role;

    @Comment("생성일")
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created;

    @Comment("수정일")
    @UpdateTimestamp
    @Column(updatable = false)
    private LocalDateTime updated;
}

