package goatHeaven.highLog.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailConfirmResponse {

    private boolean verified;
    private String message;

    public static EmailConfirmResponse success() {
        return EmailConfirmResponse.builder()
                .verified(true)
                .message("인증이 완료되었습니다.")
                .build();
    }
}
