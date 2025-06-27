package com.nhlstenden.foodorder.persistence;

import com.nhlstenden.foodorder.model.User;

import java.util.List;

public interface UserRepository {
    void save(User user);
    void update(User user);
    void delete(String username);
    User findByUsername(String username);
    List<User> findAll();
}
