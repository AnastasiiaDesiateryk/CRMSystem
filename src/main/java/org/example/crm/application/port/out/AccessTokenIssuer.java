package org.example.crm.application.port.out;



import org.example.crm.domain.model.User;

public interface AccessTokenIssuer {
    String issue(User user);
    long accessTokenTtlSeconds();
}
