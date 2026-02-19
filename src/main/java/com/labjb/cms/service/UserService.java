package com.labjb.cms.service;

import com.labjb.cms.domain.model.User;
import com.labjb.cms.domain.model.UserRole;
import com.labjb.cms.repository.UserRepository;
import com.labjb.cms.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRoleRepository userRoleRepository;

    public Optional<User> findByEmailAndPassword(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }

    public User createUser(String email, String password, String name) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);

        User savedUser = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(UserRole.RoleEnum.USER);
        userRoleRepository.save(userRole);

        return savedUser;
    }
}
