package com.example.batch.domain;

import com.example.batch.domain.enums.SocialType;
import com.example.batch.domain.enums.UserStatus;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by gavinkim at 2018-12-09
 */
@Getter
@EqualsAndHashCode(of = {"idx","email"})
@NoArgsConstructor
@Table
@Entity
public class User implements Serializable {

    @Id
    @Column
    @GeneratedValue
    private Long idx;

    private String name;

    private String email;

    private String principal;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    @Builder
    public User(String name, String email, String principal, SocialType socialType, UserStatus status,LocalDateTime createdDate,LocalDateTime updatedDate) {
        this.name = name;
        this.email = email;
        this.principal = principal;
        this.socialType = socialType;
        this.status = status;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public User setInactive(){
        this.status = UserStatus.INACTIVE;
        return this;
    }
}
