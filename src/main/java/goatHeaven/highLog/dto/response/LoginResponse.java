package goatHeaven.highLog.dto.response;

import goatHeaven.highLog.jooq.tables.pojos.Users;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserInfo user;

    @Getter
    @Builder
    public static class UserInfo {
        private Long id;
        private String email;
        private String name;
    }

    public static LoginResponse of(String accessToken, String refreshToken, Users user) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .build())
                .build();
    }
}
