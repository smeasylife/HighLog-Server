package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignupResponse {

    private Long userId;
    private String email;
    private String name;
    private LocalDateTime createdAt;

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
