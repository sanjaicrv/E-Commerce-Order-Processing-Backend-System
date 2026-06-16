package com.example.ecommerce.user;

import java.util.List;

public interface UserServiceInterface {
    UserDTO registerUser(UserDTO userDTO);
    String loginUser(String email, String password);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO updateUser(Long id, UserDTO userDTO);
    void deleteUser(Long id);
}
