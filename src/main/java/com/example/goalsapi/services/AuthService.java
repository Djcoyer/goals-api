package com.example.goalsapi.services;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.net.AuthRequest;
import com.auth0.net.Request;
import com.example.goalsapi.Exceptions.ForbiddenException;
import com.example.goalsapi.Exceptions.InternalServerException;
import com.example.goalsapi.Exceptions.InvalidInputException;
import com.example.goalsapi.models.CustomInfo;
import com.example.goalsapi.models.User;
import com.example.goalsapi.models.auth.AuthProperties.AppMetadata;
import com.example.goalsapi.models.auth.AuthTokens;
import com.example.goalsapi.models.auth.LoginInfo;
import com.example.goalsapi.models.auth.UpdateUserRequest;
import com.example.goalsapi.models.auth.management.AuthPost;
import com.example.goalsapi.models.auth.management.AuthResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private AuthAPI authApi;
    private ManagementAPI managementAPI;
    @Value("${auth0.domain}")
    private String domain;
    @Value("${auth0.clientId}")
    private String clientId;
    @Value("${auth0.clientSecret}")
    private String clientSecret;
    private String token;

    @Autowired
    public AuthService() {
    }

    public AuthService(AuthAPI authAPI, ManagementAPI managementAPI) {
        this.authApi = authAPI;
        this.managementAPI = managementAPI;
    }

    public AuthTokens login(LoginInfo loginInfo) {
        if (loginInfo.getUsername() == null || loginInfo.getUsername() == "" ||
                loginInfo.getPassword() == null || loginInfo.getPassword() == "") {
            throw new InvalidInputException();
        }

        authApi = new AuthAPI(domain, clientId, clientSecret);
        AuthRequest request = authApi.login(loginInfo.getUsername(), loginInfo.getPassword(),
                "Username-Password-Authentication").setScope("openid profile offline_access");

        try {
            TokenHolder holder = request.execute();
            AuthTokens tokens = getTokens(holder);
            return tokens;
        } catch (Auth0Exception e) {
            throw new InvalidInputException();
        }
    }

    private AuthTokens getTokens(TokenHolder holder) {
        AuthTokens tokens = new AuthTokens();
        tokens.setAccess_token(holder.getAccessToken());
        tokens.setRefresh_token(holder.getRefreshToken());
        tokens.setExpires_in(holder.getExpiresIn());
        tokens.setId_token(holder.getIdToken());
        return tokens;
    }

    public String createAuth0User(User user, String password, boolean isAdmin) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String emailAddress = user.getEmailAddress();
        String userId = user.getUserId();
        if (Arrays.asList(firstName, lastName, emailAddress, userId).contains(null))
            throw new InvalidInputException();
        com.auth0.json.mgmt.users.User auth0User = new com.auth0.json.mgmt.users.User();
        AppMetadata appMetadata = new AppMetadata();
        String[] roles = new String[2];
        roles[0] = "user";
        if(isAdmin) roles[0] = "admin";
        CustomInfo customInfo = new CustomInfo(firstName,lastName,userId,roles);
        appMetadata.put("customerInfo", customInfo);
        auth0User.setAppMetadata(appMetadata);
        auth0User.setPassword(password);
        auth0User.setEmail(emailAddress);
        auth0User.setConnection("Username-Password-Authentication");

        try {
            managementAPI = getManagementApi();
            Request<com.auth0.json.mgmt.users.User> createUserRequest = managementAPI.users().create(auth0User);
            com.auth0.json.mgmt.users.User createdUser = createUserRequest.execute();
            return createdUser.getId();
        } catch (Exception e) {
            throw new InternalServerException();
        }


    }

    public void updateUser(UpdateUserRequest request, String auth0Id) {
        com.auth0.json.mgmt.users.User userToUpdate = new com.auth0.json.mgmt.users.User();
        String firstName = request.getFirstName();
        String lastName = request.getLastName();
        String emailAddress = request.getLastName();

        AppMetadata appMetadata = new AppMetadata();


        if (firstName != null)
            appMetadata.replace("FirstName", request.getFirstName());
        if (lastName != null)
            appMetadata.replace("LastName", request.getLastName());
        userToUpdate.setAppMetadata(appMetadata);

        if (emailAddress != null)
            userToUpdate.setEmail(request.getEmailAddress());

        try {
            managementAPI = getManagementApi();
            Request updateUserRequest = managementAPI.users().update(auth0Id, userToUpdate);
            updateUserRequest.execute();
        } catch (IOException e) {
            throw new InternalServerException();
        }

    }

    public void deleteUser(String auth0Id) {
        try {
            managementAPI = getManagementApi();
            Request request = managementAPI.users().delete(auth0Id);
            request.execute();
        } catch (Exception e) {
            throw new InternalServerException();
        }
    }

    //region HELPERS

    private ManagementAPI getManagementApi() throws IOException {
        if (managementAPI != null) return managementAPI;
        String token = getClientToken();
        managementAPI = new ManagementAPI(domain, token);
        return managementAPI;
    }

    public String getClientToken() throws IOException {
        if (this.token == null) {
            URL url = new URL(String.format("https://%s/oauth/token", domain));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("content-type", "application/json");

            AuthPost authPost = new AuthPost();
            authPost.setGrantType("client_credentials");
            authPost.setClientId(this.clientId);
            authPost.setClientSecret(this.clientSecret);
            authPost.setAudience(String.format("https://%s/api/v2/", this.domain));
            String json = new ObjectMapper().writeValueAsString(authPost);

            conn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(json);
            wr.flush();
            wr.close();
            int responseCode = conn.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();
            while ((output = in.readLine()) != null) {
                response.append(output);
            }

            AuthResult authResult = new ObjectMapper().readValue(response.toString(), AuthResult.class);
            String token = authResult.access_token;
            this.token = token;
            return this.token;
        } else {
            return this.token;
        }
    }

    public String getCustomerIdFromAuthorizationHeader(String authHeader) {
        if (!authHeader.contains("Bearer"))
            throw new ForbiddenException();
        String[] authSections = authHeader.split(" ");
        String token = authSections[1];
        String customerId = getCustomerIdFromJwt(token);
        return customerId;
    }

    public String getCustomerIdFromJwt(String id_token) {
        DecodedJWT jwt = JWT.decode(id_token);
        Map<String, Claim> claims = jwt.getClaims();
        Claim claim = claims.get("http://customerInfo");
        if (claim == null) throw new ForbiddenException();
        CustomInfo info = claim.as(CustomInfo.class);
        String userId = info.getUserId();
        if (userId != null) return userId;
        else throw new ForbiddenException();
    }

    public void revokeAuthRefreshToken(String refreshToken) {
        try {
            authApi = new AuthAPI(domain, clientId, clientSecret);
            Request<Void> request = authApi.revokeToken(refreshToken);
            request.execute();
        } catch (Auth0Exception e) {
            throw new InternalServerException();
        }

    }



    //endregion

}
