package com.example.tickdeal.domain.user.entity;

import com.example.tickdeal.common.entity.BaseEntity;
import com.example.tickdeal.common.enums.UserRoleEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "users"
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(
            max = 4,
            message = "이름은 최대 4글자까지 입력할 수 있습니다."
    )
    @Column(nullable = false, length = 4)
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(
            min = 8,
            message = "비밀전호는 최소 8글자 이상입니다."
    )
    @Column(nullable = false, length = 8, columnDefinition = "TEXT")
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRoleEnum roleEnum;


    /*
     * 일반 회원가입
     *
     * role은 사용자가 선택하지 않고
     * 자동으로 USER가 부여된다.
     */
    public User(
            String name,
            String email,
            String password
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roleEnum = UserRoleEnum.NORMAL;
    }


    /*
     * 관리자 생성 등
     * 서버 내부에서 명시적으로 권한을 지정해야 할 경우 사용
     */
    public User(
            String name,
            String email,
            String password,
            UserRoleEnum roleEnum
    ) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.roleEnum = roleEnum;
    }


    public void update(
            String name,
            String email,
            String password
    ) {
        this.name =
                name == null
                        ? this.name
                        : name;

        this.email =
                email == null
                        ? this.email
                        : email;

        this.password =
                password == null
                        ? this.password
                        : password;
    }
}
