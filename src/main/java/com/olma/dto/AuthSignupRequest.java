package com.olma.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class AuthSignupRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
            message = "비밀번호는 영문 , 숫자 , 특수문자를 포함한 8자 이상이어야 합니다.")
    private String password;

    @NotBlank
    private String nickname;

    private Long experienceLevelId;

    private Long jobCategoryId;

    private List<Long> certificateTypeIds;
}
