package com.labjb.cms.Controller;

import com.labjb.cms.domain.dto.in.LoginForm;
import com.labjb.cms.domain.dto.out.LoginResponseDto;
import com.labjb.cms.domain.dto.out.UsuarioDto;
import com.labjb.cms.service.JwtService;
import com.labjb.cms.service.UserService;
import com.labjb.cms.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação")
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Realizar login", description = "Autentica usuário e retorna token JWT")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginForm loginForm) {
        try {
            return userService.findByEmailAndPassword(loginForm.getEmail(), loginForm.getSenha())
                    .map(user -> {
                        String token = jwtService.generateToken(user);
                        return ResponseEntity.ok(new LoginResponseDto(user.getName(), user.getEmail(), token));
                    })
                    .orElseThrow(() -> new EntityNotFoundException("Nenhum usuário encontrado"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao efetuar login");
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário no sistema")
    public ResponseEntity<UsuarioDto> registerUser(@Valid @RequestBody LoginForm loginForm) {
        try {
            User newUser = userService.createUser(
                loginForm.getEmail(), 
                loginForm.getSenha(),
                loginForm.getNome() // Usando email como nome por enquanto
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioDto(newUser.getName(), newUser.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
