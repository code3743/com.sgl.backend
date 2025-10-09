package com.sgl.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.sgl.backend.dto.LoginRequest;
import com.sgl.backend.dto.LoginResponse;
import com.sgl.backend.dto.SiraLoginRequest;
import com.sgl.backend.dto.SiraLoginResponse;
import com.sgl.backend.dto.SiraUserInfo;
import com.sgl.backend.entity.Role;
import com.sgl.backend.entity.User;
import com.sgl.backend.exception.SglAuthException;
import com.sgl.backend.repository.RoleRepository;
import com.sgl.backend.repository.UserRepository;
import com.sgl.backend.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(restTemplate, userRepository, roleRepository, passwordEncoder, jwtService);
        setField(authService, "siraBaseUrl", "http://localhost:3000/api");
    }

    @Test
    void authenticate_admin_success() {
        LoginRequest request = new LoginRequest("admin_code", "admin_password");
        Role adminRole = Role.builder().id(1L).name("ADMIN").build();
        User admin = User.builder().code("admin_code").password("hashed_password").role(adminRole).build();
        when(userRepository.findById("admin_code")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin_password", "hashed_password")).thenReturn(true);
        when(jwtService.generateToken(admin)).thenReturn("jwt_admin_token");

        LoginResponse response = authService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("jwt_admin_token");
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void authenticate_admin_invalidPassword_throwsException() {
        LoginRequest request = new LoginRequest("admin_code", "wrong_password");
        Role adminRole = Role.builder().id(1L).name("ADMIN").build();
        User admin = User.builder().code("admin_code").password("hashed_password").role(adminRole).build();
        when(userRepository.findById("admin_code")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        assertThrows(SglAuthException.class, () -> {
            authService.authenticate(request);
        }, "Invalid admin credentials");
    }

    @Test
    void authenticate_student_success() {
        LoginRequest request = new LoginRequest("12345", "password");
        SiraLoginResponse siraResponse = new SiraLoginResponse("sira_token", true);
        SiraUserInfo userInfo = new SiraUserInfo("Ingeniería", "John Doe", "12345", "john@correounivalle.edu.co", "123456789");
        Role studentRole = Role.builder().id(1L).name("ESTUDIANTE").build();
        User user = User.builder().code("12345").name("John Doe").email("john@correounivalle.edu.co").document("123456789").role(studentRole).build();

        when(restTemplate.postForEntity(eq("http://localhost:3000/api/auth"), any(SiraLoginRequest.class), eq(SiraLoginResponse.class)))
                .thenReturn(ResponseEntity.ok(siraResponse));
        when(restTemplate.exchange(eq("http://localhost:3000/api/student/info"), eq(HttpMethod.GET), any(HttpEntity.class), eq(SiraUserInfo.class)))
                .thenReturn(ResponseEntity.ok(userInfo));
        when(userRepository.findById("12345")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ESTUDIANTE")).thenReturn(Optional.of(studentRole));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt_student_token");

        LoginResponse response = authService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("jwt_student_token");
        assertThat(response.getRole()).isEqualTo("ESTUDIANTE");
        verify(userRepository).save(any(User.class));
    }  

    @Test
    void authenticate_siraInvalidCredentials_throwsException() {
        LoginRequest request = new LoginRequest("12345", "wrong_password");
        when(restTemplate.postForEntity(eq("http://localhost:3000/api/auth"), any(SiraLoginRequest.class), eq(SiraLoginResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        
                assertThrows(SglAuthException.class, () -> {
            authService.authenticate(request);
        }, "SIRA authentication failed: Invalid credentials");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
