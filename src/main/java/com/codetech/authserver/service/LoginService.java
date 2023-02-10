package com.codetech.authserver.service;

import com.codetech.authserver.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class LoginService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private String issueUrl;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.oauth2-client-credentials.authorization-grant-type}")
    private String grantType;

    /**
     * @param loginRequest - логин и пароль, передается POST запросом
     * {
     *     "username" : "max",
     *     "password" : "max"
     * }
     * @return - возвращает json: access токен и refresh токен и мх expires
     */
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", grantType);
        map.add("username", loginRequest.getUsername());
        map.add("password", loginRequest.getPassword());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(tokenUri, httpEntity, LoginResponse.class);

        consoleLog(response); // todo: should remove
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    /**
     *
     * @param request - refresh_token
     * @return - true + сообщение или false
     */
    public ResponseEntity<Response> logout(TokenRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", request.getToken());

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
//        String endSession = "http://localhost:8180/realms/auth_proba/protocol/openid-connect/logout";
        String endSession = "https://keycloak.mydomain.com:8443/realms/mo-auth-test/protocol/openid-connect/logout";
        ResponseEntity<Response> response = restTemplate.postForEntity(endSession, httpEntity, Response.class);

        Response result = new Response();
        if (response.getStatusCode().is2xxSuccessful()) {
            result.setStatus(true);
            result.setMessage("Logged out successfully");
        }
        return ResponseEntity.ok(result);
    }

    /**
     *
     * @param request - значение access token
     * @return - true = пользователь активен, false = не активен в системе (просрочен)
     */
    public ResponseEntity<IntrospectResponse> introspect(TokenRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("token", request.getToken());


        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map,headers);
//        String interS = "http://localhost:8180/realms/auth_proba/protocol/openid-connect/token/introspect";
        String interS = "https://keycloak.mydomain.com:8443/realms/mo-auth-test/protocol/openid-connect/token/introspect";
        ResponseEntity<IntrospectResponse> response = restTemplate.postForEntity(interS, httpEntity, IntrospectResponse.class);
        return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
    }

    public void consoleLog(ResponseEntity<LoginResponse> response) {

        if (response.getStatusCode().is2xxSuccessful()) {
            LoginResponse log = response.getBody();
            assert log != null;
            System.out.println("\nПолучено:");
            System.out.println("Token type: " + log.getToken_type());
            System.out.println("Token access: " + log.getAccess_token());
            System.out.println("Token expires sec: " + log.getExpires_in());
            System.out.println("\nRefresh token: " + log.getRefresh_token());
            System.out.println("Refresh token expires sec: " + log.getRefresh_expires_in());
        }
    }


}
