package org.wemightmove.movemap.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.wemightmove.movemap.global.entity.BaseTimeEntity;
import org.wemightmove.movemap.global.enums.RoleType;
import org.wemightmove.movemap.global.enums.SexType;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "kakao_id", unique = true)
    private Long kakaoId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private RoleType role;

    @Column(name = "school", length = 100)
    private String school;

    @Column(name = "nickname", length = 20, nullable = false)
    private String nickname;

    @Column(name = "region_cd", length = 20, nullable = false)
    private String regionCode;

    @Column(name = "uuid", length = 50, nullable = false, unique = true)
    private String uuid;

    @Column(name = "height")
    private double height;

    @Column(name = "weight")
    private double weight;

    @Column(name = "isDeleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "age", nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex", nullable = false)
    private SexType sex;

    @Builder
    public Member(RoleType role, String email, String password, String nickname, String regionCode, String uuid, double height, double weight, boolean isDeleted, int age, SexType sex, Long kakaoId) {
        this.role = role;
        this.email = email;
        this.password = password;
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.regionCode = regionCode;
        this.uuid = uuid;
        this.height = height;
        this.weight = weight;
        this.isDeleted = isDeleted;
        this.age = age;
        this.sex = sex;
    }


    public void withdraw() {
        this.isDeleted = true;
    }

    public void updateProfile(String nickname, String school, String regionCode,
                              SexType sex, Integer age, Double height, Double weight) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (school != null) {
            this.school = school;
        }
        if (regionCode != null) {
            this.regionCode = regionCode;
        }
        if (sex != null) {
            this.sex = sex;
        }
        if (age != null) {
            this.age = age;
        }
        if (height != null) {
            this.height = height;
        }
        if (weight != null) {
            this.weight = weight;
        }
    }
}
