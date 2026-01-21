package goatHeaven.highLog.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@Getter
@RequiredArgsConstructor
public class CustomUserPrincipal implements Principal {

    private final Long userId;
    private final String email;

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
