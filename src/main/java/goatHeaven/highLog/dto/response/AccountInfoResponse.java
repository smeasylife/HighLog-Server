package goatHeaven.highLog.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountInfoResponse {

    private String userName;
    private String registDate;
    private String email;

    public static AccountInfoResponse of(String userName, String registDate, String email) {
        return AccountInfoResponse.builder()
                .userName(userName)
                .registDate(registDate)
                .email(email)
                .build();
    }
}
