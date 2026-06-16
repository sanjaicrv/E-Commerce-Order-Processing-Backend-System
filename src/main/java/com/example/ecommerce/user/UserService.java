package com.example.ecommerce.user;

import com.example.ecommerce.cart.CartEntity;
import com.example.ecommerce.cart.CartJpa;
import com.example.ecommerce.exception.UserNotFoundException;
import com.example.ecommerce.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserServiceInterface {

    @Autowired
    private UserJpa userJpa;

    @Autowired
    @Lazy
    private CartJpa cartJpa;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        if (userJpa.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + userDTO.getEmail());
        }

        UserEntity userEntity = UserEntity.builder()
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .phone(userDTO.getPhone())
                .role(userDTO.getRole() != null ? userDTO.getRole() : Role.CUSTOMER)
                .isActive(true)
                .build();

        UserEntity savedUser = userJpa.save(userEntity);

        // Auto-create a Cart if the role is CUSTOMER
        if (savedUser.getRole() == Role.CUSTOMER) {
            CartEntity cart = CartEntity.builder()
                    .user(savedUser)
                    .build();
            cartJpa.save(cart);
        }

        return convertToDTO(savedUser);
    }

    @Override
    public String loginUser(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserEntity userEntity = userJpa.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return jwtUtils.generateToken(userDetails.getUsername(), userEntity.getRole().name());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userJpa.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        UserEntity user = userJpa.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        UserEntity user = userJpa.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setName(userDTO.getName());
        user.setPhone(userDTO.getPhone());
        if (userDTO.getIsActive() != null) {
            user.setIsActive(userDTO.getIsActive());
        }
        
        UserEntity updatedUser = userJpa.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        UserEntity user = userJpa.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userJpa.delete(user);
    }

    private UserDTO convertToDTO(UserEntity entity) {
        return UserDTO.builder()
                .userId(entity.getUserId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .role(entity.getRole())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
