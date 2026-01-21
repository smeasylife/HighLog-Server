package goatHeaven.highLog.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerifyResponse {

    private String message;
    private int expiresIn;

    public static EmailVerifyResponse of(int expiresIn) {
        return EmailVerifyResponse.builder()
                .message("인증 번호가 이메일로 전송되었습니다.")
                .expiresIn(expiresIn)
                .build();
    }
}
