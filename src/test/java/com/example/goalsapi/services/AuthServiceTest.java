package com.example.goalsapi.services;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.client.mgmt.UsersEntity;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.AuthRequest;
import com.auth0.net.Request;
import com.example.goalsapi.Exceptions.ForbiddenException;
import com.example.goalsapi.Exceptions.InternalServerException;
import com.example.goalsapi.Exceptions.InvalidInputException;
import com.example.goalsapi.models.CustomInfo;
import com.example.goalsapi.models.auth.AuthProperties.AppMetadata;
import com.example.goalsapi.models.auth.AuthTokens;
import com.example.goalsapi.models.auth.LoginInfo;
import com.example.goalsapi.models.auth.UpdateUserRequest;
import com.mongodb.util.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.omg.CORBA.DynAnyPackage.Invalid;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class AuthServiceTest {


    private AuthService authService;

    @Mock
    private AuthAPI authAPI;

    @Mock
    private ManagementAPI managementAPI;

    @Mock
    private UsersEntity usersEntity;

    private User auth0User;

    @Mock
    private User testAuth0User;

    @Mock
    private Request<User> request;

    @Mock
    private Request<Void> voidRequest;

    private String connection = "Username-Password-Authentication";


    private com.example.goalsapi.models.User user;

    private String emailAddress = "testuser@test.com";
    private String password = "pword";
    private String firstName = "Test";
    private String lastName = "User";
    private String userId = "754c7f3b-e3ea-4e83-b0ab-62472aeae5d2";

    private AppMetadata appMetadata;
    private String[] roles;
    private CustomInfo customInfo;
    private String auth0Id = "auth0|123456";

    private AuthTokens authTokens;
    private String idToken  ="someidtoken";
    private String accessToken = "someaccesstoken";
    private long expiresIn = 12345;
    private String refreshToken = "somerefreshtoken";
    private boolean isAdmin;
    private String jwt;

    @Mock
    private AuthRequest authRequest;

    @Mock
    private TokenHolder tokenHolder;

    private LoginInfo loginInfo;

    @Before
    public void init(){
        authService = new AuthService(authAPI,managementAPI);
        initUser();
    }

    //region LOGIN

    @Test
    public void login_returnsAuthTokens_validRequest() throws Auth0Exception {
        //arrange
        when(tokenHolder.getAccessToken()).thenReturn(accessToken);
        when(tokenHolder.getExpiresIn()).thenReturn(expiresIn);
        when(tokenHolder.getIdToken()).thenReturn(idToken);
        when(tokenHolder.getRefreshToken()).thenReturn(refreshToken);

        authTokens = new AuthTokens(idToken,accessToken,refreshToken,expiresIn);
        loginInfo = new LoginInfo(emailAddress,password);
        when(authAPI.login(emailAddress,password, connection)).thenReturn(authRequest);
        when(authRequest.execute()).thenReturn(tokenHolder);

        //act
        AuthTokens returnedTokens = authService.login(loginInfo);

        //assert
        assertNotNull(returnedTokens);
        assertEquals(accessToken, returnedTokens.getAccess_token());
        assertEquals(refreshToken, returnedTokens.getRefresh_token());

    }

    @Test(expected = InvalidInputException.class)
    public void login_throwsInvalidInput_emptyUsername() throws Auth0Exception {
        //arrange
        when(tokenHolder.getAccessToken()).thenReturn(accessToken);
        when(tokenHolder.getExpiresIn()).thenReturn(expiresIn);
        when(tokenHolder.getIdToken()).thenReturn(idToken);
        when(tokenHolder.getRefreshToken()).thenReturn(refreshToken);

        authTokens = new AuthTokens(idToken,accessToken,refreshToken,expiresIn);
        loginInfo = new LoginInfo("", password);

        when(authAPI.login(emailAddress,password, connection)).thenReturn(authRequest);
        when(authRequest.execute()).thenReturn(tokenHolder);

        //act
        AuthTokens returnedTokens = authService.login(loginInfo);

        //assert
    }

    @Test(expected = InternalServerException.class)
    public void login_throwsInternalServer_failedLogin() throws Auth0Exception {
        //arrange
        when(tokenHolder.getAccessToken()).thenReturn(accessToken);
        when(tokenHolder.getExpiresIn()).thenReturn(expiresIn);
        when(tokenHolder.getIdToken()).thenReturn(idToken);
        when(tokenHolder.getRefreshToken()).thenReturn(refreshToken);

        authTokens = new AuthTokens(idToken,accessToken,refreshToken,expiresIn);
        loginInfo = new LoginInfo(emailAddress, password);

        when(authAPI.login(emailAddress,password, connection)).thenReturn(authRequest);
        when(authRequest.execute()).thenThrow(new Auth0Exception("FAILED!!!!!"));

        //act
        AuthTokens returnedTokens = authService.login(loginInfo);

        //assert
    }

    //endregion

    //region CREATE_USER

    @Test
    public void createAuth0User_returnsAuth0Id_validRequest() throws Auth0Exception {
        //arrange
        when(managementAPI.users()).thenReturn(usersEntity);
        when(usersEntity.create(Mockito.any(User.class))).thenReturn(request);
        when(request.execute()).thenReturn(testAuth0User);
        when(testAuth0User.getId()).thenReturn(auth0Id);
        user = new com.example.goalsapi.models.User(userId,firstName,lastName,"",emailAddress,null);
        isAdmin = false;

        //act
        String returnedAuth0Id = authService.createAuth0User(user,password, isAdmin);

        //assert
        assertNotNull(returnedAuth0Id);
        assertEquals(auth0Id,returnedAuth0Id);
    }

    @Test(expected = InvalidInputException.class)
    public void createAuth0User_throwsInvalidInput_nullFirstName() throws Auth0Exception {
        //arrange
        when(managementAPI.users()).thenReturn(usersEntity);
        when(usersEntity.create(Mockito.any(User.class))).thenReturn(request);
        when(request.execute()).thenReturn(testAuth0User);
        when(testAuth0User.getId()).thenReturn(auth0Id);
        user = new com.example.goalsapi.models.User(userId,"",lastName,"",emailAddress,null);
        isAdmin = false;

        //act
        String returnedAuth0Id = authService.createAuth0User(user,password,isAdmin);

        //assert
    }

    @Test(expected = InternalServerException.class)
    public void createAuth0User_throwsInternalServer_auth0Error() throws Auth0Exception {
        //arrange
        when(managementAPI.users()).thenReturn(usersEntity);
        when(usersEntity.create(Mockito.any(User.class))).thenReturn(request);
        when(request.execute()).thenThrow(new Auth0Exception("FAILED!!!!"));
        when(testAuth0User.getId()).thenReturn(auth0Id);
        user = new com.example.goalsapi.models.User(userId,firstName,lastName,"",emailAddress,null);
        isAdmin = false;

        //act
        String returnedAuth0Id = authService.createAuth0User(user,password,isAdmin);

        //assert
    }

    //endregion

    //region UPDATE_USER

    @Test
    public void updateUser_runsSuccessfully_validRequest() throws Auth0Exception {
        //arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest(firstName,lastName,null);
        User _user = new User();
        AppMetadata appMetadata = new AppMetadata();
        appMetadata.put("FirstName", firstName);
        appMetadata.put("LastName", lastName);
        _user.setAppMetadata(appMetadata);
        when(managementAPI.users()).thenReturn(usersEntity);
        when(usersEntity.update(Mockito.anyString(), Mockito.any(User.class))).thenReturn(request);
        when(request.execute()).thenReturn(auth0User);

        //act
        authService.updateUser(updateRequest, auth0Id);

        //assert

    }

    @Test(expected = InternalServerException.class)
    public void updateUser_throwsInternalServer_auth0Error() throws Auth0Exception {
        //arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest(firstName,lastName,null);
        User _user = new User();
        AppMetadata appMetadata = new AppMetadata();
        appMetadata.put("FirstName", firstName);
        appMetadata.put("LastName", lastName);
        _user.setAppMetadata(appMetadata);
        when(managementAPI.users()).thenReturn(usersEntity);
        when(usersEntity.update(Mockito.anyString(), Mockito.any(User.class))).thenReturn(request);
        when(request.execute()).thenThrow(new Auth0Exception(""));

        //act
        authService.updateUser(updateRequest,auth0Id);

        //assert
    }

    @Test(expected = InvalidInputException.class)
    public void updateUser_throwsInvalidInput_nullAuth0Id() throws Auth0Exception {
        //arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest(firstName,lastName,null);
        User _user = new User();
        AppMetadata appMetadata = new AppMetadata();
        appMetadata.put("FirstName", firstName);
        appMetadata.put("LastName", lastName);
        _user.setAppMetadata(appMetadata);
        when(managementAPI.users()).thenReturn(usersEntity);
        when(usersEntity.update(Mockito.anyString(), Mockito.any(User.class))).thenReturn(request);
        when(request.execute()).thenReturn(auth0User);

        //act
        authService.updateUser(updateRequest,null);

        //assert
    }

    @Test(expected = InvalidInputException.class)
    public void updateUser_throwsInvalidInput_noUpdateValues() throws Auth0Exception {
        //arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest("","",null);
        User _user = new User();
        AppMetadata appMetadata = new AppMetadata();
        appMetadata.put("FirstName", firstName);
        appMetadata.put("LastName", lastName);
        _user.setAppMetadata(appMetadata);
        when(managementAPI.users()).thenReturn(usersEntity);
        when(usersEntity.update(Mockito.anyString(), Mockito.any(User.class))).thenReturn(request);
        when(request.execute()).thenReturn(auth0User);

        //act
        authService.updateUser(updateRequest,auth0Id);

        //assert
    }

    //endregion

    //region DELETE_USER

    @Test
    public void deleteUser_runsSuccessfully_validRequest() throws Auth0Exception {
        //arrange
        when(managementAPI.users()).thenReturn(usersEntity);
        when(usersEntity.delete(auth0Id)).thenReturn(request);
        when(request.execute()).thenReturn(null);

        //act
        authService.deleteUser(auth0Id);

        //assert
    }

    @Test(expected = InternalServerException.class)
    public void deleteUser_throwsInternalServer_auth0Error() throws Auth0Exception {
        when(managementAPI.users()).thenReturn(usersEntity);
        when(usersEntity.delete(auth0Id)).thenReturn(request);
        when(request.execute()).thenThrow(new Auth0Exception("FAIL!!"));

        //act
        authService.deleteUser(auth0Id);

        //assert
    }

    //endregion

    //region GET_CUSTOMER_ID

    @Test
    public void getUserIdFromHeader_returnsId_validRequest(){
        //arrange
        initJwt();
        String authHeader = "Bearer " + jwt;

        //act
        String returnedId = authService.getUserIdFromAuthorizationHeader(authHeader);

        //assert
        assertNotNull(returnedId);
        assertEquals(userId, returnedId);
    }

    @Test(expected = InvalidInputException.class)
    public void getUserIdFromHeader_throwsInvalidInput_nullAuthHeader(){
        //arrange

        //act
        String returnedId = authService.getUserIdFromAuthorizationHeader(null);
        //assert
    }

    @Test(expected = ForbiddenException.class)
    public void getUserIdFromHeader_throwsForbidden_badHeader(){
        //arrange
        initJwt();

        //act
        String returnedId = authService.getUserIdFromAuthorizationHeader(jwt);
        //assert
    }

    //endregion

    //region GET_ID_JWT

    @Test
    public void getUserIdFromJwt_returnsId_validRequest(){
        //arrange
        initJwt();

        //act
        String returnedId = authService.getUserIdFromJwt(jwt);

        //assert
        assertNotNull(returnedId);
        assertEquals(userId, returnedId);
    }

    @Test(expected = ForbiddenException.class)
    public void getUserIdFromJwt_throwsForbidden_noClaim(){
        //arrange
        String token = Jwts.builder().setAudience("default").setId(auth0Id).compact();

        //act
        String returedId = authService.getUserIdFromJwt(token);

        //assert
    }

    @Test(expected = ForbiddenException.class)
    public void getUserIdFromJwt_throwsForbidden_noUserId(){
        //arrange
        CustomInfo customInfo = new CustomInfo(firstName,lastName,null,null);
        HashMap<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("http://customerInfo", customInfo);
        String token = Jwts.builder().addClaims(jwtBody).compact();
        //act
        String returnedId = authService.getUserIdFromJwt(token);

        //assert
    }

    //endregion

    //region REVOKE_REFRESH_TOKEN

    @Test
    public void revokeRefreshToken_runsSuccessfully_validRequest() throws Auth0Exception {
        //arrange
        when(authAPI.revokeToken(refreshToken)).thenReturn(voidRequest);
        when(voidRequest.execute()).thenReturn(null);

        //act
        authService.revokeAuthRefreshToken(refreshToken);

        //assert
    }

    @Test(expected = InternalServerException.class)
    public void revokeRefreshToken_throwsInternal_auth0Error() throws Auth0Exception {
        //arrange
        when(authAPI.revokeToken(refreshToken)).thenReturn(voidRequest);
        when(voidRequest.execute()).thenThrow(new Auth0Exception("FAIL"));

        //act
        authService.revokeAuthRefreshToken(refreshToken);

        //assert
    }
    //endregion

    //region HELPERS
    private void initUser(){
        auth0User = new User(connection);
        auth0User.setEmail(emailAddress);
        auth0User.setConnection(connection);
        auth0User.setPassword(password);

        roles = new String[2];
        roles[0] = "user";
        customInfo = new CustomInfo(firstName,lastName,userId, roles);

        appMetadata = new AppMetadata();
        appMetadata.put("customerInfo", customInfo);
        auth0User.setAppMetadata(appMetadata);
    }

    private void initJwt() {
        roles = new String[2];
        roles[0] = "user";
        CustomInfo customInfo = new CustomInfo(firstName,lastName,userId,roles);
        HashMap<String, Object> jwtBody = new HashMap<>();
        jwtBody.put("http://customerInfo", customInfo);
        jwt = Jwts.builder().addClaims(jwtBody).compact();
    }

    //endregion
}
