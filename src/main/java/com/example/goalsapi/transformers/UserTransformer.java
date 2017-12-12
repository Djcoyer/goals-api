package com.example.goalsapi.transformers;

import com.example.goalsapi.models.User;
import com.example.goalsapi.models.dao.UserDao;

public class UserTransformer {

    public static User transform(UserDao userDao) {
        User user = new User();
        user.setAuth0Id(userDao.getAuth0Id());
        user.setUserId(userDao.getUserId());
        user.setEmailAddress(userDao.getEmailAddress());
        user.setFirstName(userDao.getFirstName());
        user.setLastName(userDao.getLastName());
        return user;
    }

    public static UserDao transform(User user) {
        UserDao userDao = new UserDao();
        userDao.setAuth0Id(user.getAuth0Id());
        userDao.setUserId(user.getUserId());
        userDao.setEmailAddress(user.getEmailAddress());
        userDao.setFirstName(user.getFirstName());
        userDao.setLastName(user.getLastName());
        return userDao;
    }

}
