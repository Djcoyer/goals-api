package com.example.goalsapi.services;

import com.example.goalsapi.Exceptions.InvalidInputException;
import com.example.goalsapi.Exceptions.NotFoundException;
import com.example.goalsapi.models.User;
import com.example.goalsapi.models.auth.AuthTokens;
import com.example.goalsapi.models.auth.CreateUserRequest;
import com.example.goalsapi.models.auth.LoginInfo;
import com.example.goalsapi.models.auth.UpdateUserRequest;
import com.example.goalsapi.models.dao.UserDao;
import com.example.goalsapi.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserServiceTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    private UserService userService;

    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;
    private LoginInfo loginInfo;
    private AuthTokens authTokens;

    private User user;
    private UserDao userDao;

    private String userId = "userId";
    private String auth0Id = "auth0|someId";
    private String emailAddress = "testuser@test.com";
    private String firstName = "Test";
    private String lastName = "User";
    private String refreshToken = "refreshtokenwithinterestingcharacters";
    private String idToken = "idtokenwithsomeinfo";
    private String accessToken = "accesstokenwithotherstuff";
    private String password = "pword";

    @Before
    public void init(){
        userService = new UserService(userRepository, authService);

        userDao = new UserDao(userId,firstName,lastName,auth0Id,emailAddress,null);

    }

    //region CREATE_USER

    @Test
    public void createUser_returnsUser_validRequest(){
        //arrange
        createUserRequest = new CreateUserRequest(emailAddress,password, firstName,lastName, false);

        user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailAddress(emailAddress);

        when(userRepository.existsByEmailAddress(emailAddress)).thenReturn(false);
        when(authService.createAuth0User(Mockito.any(User.class), Mockito.anyString(), Mockito.anyBoolean())).thenReturn(auth0Id);

        //act
        User createdUser = userService.createUser(createUserRequest);

        //assert
        assertNotNull(createdUser);
        assertEquals(auth0Id, createdUser.getAuth0Id());
        assertEquals(firstName, createdUser.getFirstName());

    }

    @Test(expected = DataIntegrityViolationException.class)
    public void createUser_throwsDataIntegrityViolation_emailExists(){
        //arrange
        createUserRequest = new CreateUserRequest(emailAddress,password,firstName,lastName, false);
        when(userRepository.existsByEmailAddress(emailAddress)).thenReturn(true);


        //act
        userService.createUser(createUserRequest);

        //assert
    }


    //endregion

    //region UPDATE_USER

    @Test
    public void updateUser_returnsUser_validRequest(){
        //arrange
        String newFirstName = "FirstName";
        String newLastName = "LastName";
        updateUserRequest = new UpdateUserRequest(newFirstName,newLastName,null);
        when(userRepository.findOne(userId)).thenReturn(userDao);

        //act
        User updatedUser = userService.updateUser(updateUserRequest, userId);

        //assert
        assertNotNull(updatedUser);
        assertEquals(newFirstName, updatedUser.getFirstName());
        assertNotNull(updatedUser.getEmailAddress());
    }
    
    @Test(expected = NotFoundException.class)
    public void updateUser_throwsNotFound_invalidUserId(){
        //arrange
        updateUserRequest = new UpdateUserRequest(firstName, lastName,emailAddress);

        when(userRepository.findOne(userId)).thenReturn(null);

        //act
        User updatedUser = userService.updateUser(updateUserRequest, userId);

        //assert
    }

    @Test
    public void updateUser_setsFieldsNull_emptyFields(){
        //arrange
        updateUserRequest = new UpdateUserRequest(null, null, emailAddress);

        when(userRepository.findOne(userId)).thenReturn(userDao);

        //act
        User updatedUser = userService.updateUser(updateUserRequest, userId);

        //assert
        assertNotNull(updatedUser);
        assertEquals(emailAddress, updatedUser.getEmailAddress());
        assertNotNull(updatedUser.getFirstName());
        assertNotNull(updatedUser.getLastName());
    }

    //endregion

    //region LOGIN

    @Test
    public void login_returnsAuthTokens_validRequest(){
        //arrange
        loginInfo = new LoginInfo(emailAddress,password);
        authTokens = new AuthTokens(idToken,accessToken,refreshToken,100);
        when(authService.login(loginInfo)).thenReturn(authTokens);
        when(authService.getUserIdFromJwt(idToken)).thenReturn(userId);
        when(userRepository.exists(userId)).thenReturn(true);
        when(userRepository.findOne(userId)).thenReturn(userDao);

        //act
        AuthTokens loginTokens = userService.login(loginInfo);

        //assert
        assertNotNull(loginTokens);
        assertEquals(idToken, loginTokens.getId_token());
        assertEquals(refreshToken, loginTokens.getRefresh_token());
    }

    @Test(expected = NotFoundException.class)
    public void login_throwsNotFound_badIdFromToken(){
        //arrange
        loginInfo = new LoginInfo(emailAddress,password);
        authTokens = new AuthTokens(idToken,accessToken,refreshToken,100);
        when(authService.login(loginInfo)).thenReturn(authTokens);
        when(userRepository.exists(userId)).thenReturn(false);
        when(authService.getUserIdFromJwt(idToken)).thenReturn(userId);
        when(userRepository.findOne(userId)).thenReturn(userDao);

        //act
        AuthTokens loginTokens = userService.login(loginInfo);

        //assert
    }

    //endregion

    //region LOGOUT

    @Test
    public void logout_runsSuccessfully_validRequest(){
        //arrange
        String authHeader = "AuthHeader";
        when(authService.getUserIdFromAuthorizationHeader(authHeader)).thenReturn(userId);
        when(userRepository.exists(userId)).thenReturn(true);
        when(userRepository.findOne(userId)).thenReturn(userDao);
        doNothing().when(authService).revokeAuthRefreshToken(refreshToken);
        //act
        userService.logout(authHeader);

        //assert
    }

    @Test(expected = InvalidInputException.class)
    public void logout_throwsInvalidInput_badAuthHeader(){
        //arrange
        String authHeader = "badauthheader!";
        when(authService.getUserIdFromAuthorizationHeader(authHeader)).thenReturn(null);

        //act
        userService.logout(authHeader);

        //assert
    }

    @Test
    public void logout_returns_refereshTokenNull(){
        //arrange
        userDao.setRefreshToken(null);
        String authHeader = "authheader";
        when(authService.getUserIdFromAuthorizationHeader(authHeader)).thenReturn(userId);
        when(userRepository.exists(userId)).thenReturn(true);
        when(userRepository.findOne(userId)).thenReturn(userDao);

        //act
        userService.logout(authHeader);
        //assert
    }

    @Test(expected = NotFoundException.class)
    public void logout_throwsNotFound_badId(){
        //arrange
        String authHeader = "authHeader";
        when(authService.getUserIdFromAuthorizationHeader(authHeader)).thenReturn(userId);
        when(userRepository.exists(userId)).thenReturn(false);

        //act
        userService.logout(authHeader);

        //assert
    }

    //endregion

    //region GET_USER

    @Test
    public void getUser_returnsUser_validRequest(){
        //arrange
        when(userRepository.findOne(userId)).thenReturn(userDao);

        //act
        User returnedUser = userService.getUser(userId);

        //assert
        assertNotNull(returnedUser);
        assertEquals(userDao.getAuth0Id(), returnedUser.getAuth0Id());
    }
    
    @Test(expected = NotFoundException.class)
    public void getUser_throwsNotFound_invalidId(){
        //arrange
        when(userRepository.findOne(userId)).thenReturn(null);
        //act
        User returnedUser = userService.getUser(userId);
        //assert
    }

    //endregion

    //region DELETE_USER

    @Test
    public void deleteUser_runsSuccessfully_validId(){
        //arrange
        when(userRepository.exists(userId)).thenReturn(true);
        when(userRepository.findOne(userId)).thenReturn(userDao);

        //act
        userService.deleteUser(userId);

        //assert
    }

    @Test(expected = NotFoundException.class)
    public void deleteUser_throwsNotFound_badId(){
        //arrange
        when(userRepository.exists(userId)).thenReturn(false);

        //act
        userService.deleteUser(userId);

        //assert
    }

    //endregion

    //region SERVICE_HELPERS

    @Test
    public void userExists_returnsTrue_validUserId(){
        //arrange
        when(userRepository.exists(userId)).thenReturn(true);
        //act
        boolean exists = userService.userExists(userId);
        //assert
        assertEquals(true, exists);
    }

    //endregion


}
