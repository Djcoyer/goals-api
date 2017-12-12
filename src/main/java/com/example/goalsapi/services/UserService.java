package com.example.goalsapi.services;

import com.example.goalsapi.Exceptions.ConflictException;
import com.example.goalsapi.Exceptions.InvalidInputException;
import com.example.goalsapi.Exceptions.NotFoundException;
import com.example.goalsapi.models.User;
import com.example.goalsapi.models.auth.AuthTokens;
import com.example.goalsapi.models.auth.CreateUserRequest;
import com.example.goalsapi.models.auth.LoginInfo;
import com.example.goalsapi.models.auth.UpdateUserRequest;
import com.example.goalsapi.models.dao.UserDao;
import com.example.goalsapi.repositories.UserRepository;
import com.example.goalsapi.transformers.UserTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private UserRepository userRepository;
    private AuthService authService;

    @Autowired
    public UserService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    public User createUser(CreateUserRequest request) {
        User user = new User();
        String customerId = UUID.randomUUID().toString();
        user.setEmailAddress(request.getEmailAddress());
        user.setLastName(request.getLastName());
        user.setFirstName(request.getFirstName());
        user.setUserId(customerId);
        if (userRepository.existsByEmailAddress(request.getEmailAddress()))
            throw new ConflictException();
        String auth0Id = authService.createAuth0User(user, request.getPassword());
        user.setAuth0Id(auth0Id);
        UserDao userDao = UserTransformer.transform(user);
        userRepository.insert(userDao);

        return user;
    }

    public User updateUser(UpdateUserRequest request, String userId) {
        UserDao userDao = userRepository.findOne(userId);
        if(userDao == null)
            throw new NotFoundException();
        String auth0Id = userDao.getAuth0Id();
        String firstName = request.getFirstName();
        String lastName = request.getLastName();
        String emailAddress = request.getEmailAddress();

        if(emailAddress != null && ! emailAddress.equalsIgnoreCase(""))
            userDao.setEmailAddress(emailAddress);
        else request.setEmailAddress(null);
        if(firstName != null && !firstName.equalsIgnoreCase(""))
            userDao.setFirstName(firstName);
        else request.setFirstName(null);
        if(lastName != null && !lastName.equalsIgnoreCase(""))
            userDao.setLastName(lastName);
        else request.setLastName(null);
        authService.updateUser(request, auth0Id);

        userRepository.save(userDao);

        User user = UserTransformer.transform(userDao);
        return user;
    }

    public AuthTokens login(LoginInfo loginInfo) {
        AuthTokens authTokens = authService.login(loginInfo);
        String userId = authService.getCustomerIdFromJwt(authTokens.getId_token());
        UserDao userDao = userRepository.findOne(userId);
        userDao.setRefreshToken(authTokens.getRefresh_token());
        userRepository.save(userDao);
        return authTokens;
    }

    public void logout(String authHeader) {
        String userId = authService.getCustomerIdFromAuthorizationHeader(authHeader);
        if(userId == null)
            throw new InvalidInputException();
        UserDao userDao = userRepository.findOne(userId);
        String refreshToken = userDao.getRefreshToken();
        if(refreshToken == null || refreshToken.equalsIgnoreCase(""))
            return;
        authService.revokeAuthRefreshToken(refreshToken);
        userDao.setRefreshToken(null);
        userRepository.save(userDao);
    }

    //region GET

    public User getCustomer(String customerId) {
        UserDao userDao = userRepository.findOne(customerId);
        if(userDao == null)
            throw new NotFoundException();
        return UserTransformer.transform(userDao);
    }

    //endregion

    //region DELETE
    public void deleteUser(String userId) {
        if(!userRepository.exists(userId))
            throw new NotFoundException();
        UserDao userDao = userRepository.getAuth0IdByUserId(userId);
        String auth0Id = userDao.getAuth0Id();
        authService.deleteUser(auth0Id);
        userRepository.delete(userId);
    }

    //endregion

    //region HELPERS
    public boolean userExists(String userId) {
        return userRepository.exists(userId);
    }

    //endregion

}
