package com.example.batch.domain;

import com.example.batch.domain.enums.Grade;
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
@Table(name = "STUDY_USER")
@Entity
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STUDY_USER_SEQ",nullable = false, unique = true, updatable = false)
    private Long idx;

    @Column(name = "NAME")
    private String name;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "SOCIAL_TYPE")
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "GRADE")
    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Builder
    public User(String name,
                String email,
                SocialType socialType,
                UserStatus status,
                Grade grade,
                LocalDateTime createdDate,LocalDateTime updatedDate) {
        this.idx = idx;
        this.name = name;
        this.email = email;
        this.socialType = socialType;
        this.status = status;
        this.grade = grade;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public User setInactive(){
        this.status = UserStatus.INACTIVE;
        return this;
    }
}
