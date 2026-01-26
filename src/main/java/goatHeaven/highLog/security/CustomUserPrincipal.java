package goatHeaven.highLog.security;

import goatHeaven.highLog.domain.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

@Getter
@RequiredArgsConstructor
public class CustomUserPrincipal implements Principal {

    private final Long userId;
    private final String email;
    private final Role role;

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
