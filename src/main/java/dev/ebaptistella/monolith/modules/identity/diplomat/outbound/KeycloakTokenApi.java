package dev.ebaptistella.monolith.modules.identity.diplomat.outbound;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface KeycloakTokenApi {

    @PostExchange(contentType = "application/x-www-form-urlencoded")
    ResponseEntity<String> exchangeToken(@RequestBody MultiValueMap<String, String> form);
}
