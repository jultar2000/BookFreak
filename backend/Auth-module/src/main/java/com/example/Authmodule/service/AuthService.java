package com.example.Authmodule.service;

import com.example.Authmodule.dao.AuthUserDao;
import com.example.Authmodule.dto.AuthUserResponse;
import com.example.Authmodule.dto.LoginUserRequest;
import com.example.Authmodule.dto.RefreshTokenRequest;
import com.example.Authmodule.entity.AuthUser;
import com.example.Authmodule.entity.VerificationEmail;
import com.example.Authmodule.entity.VerificationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.UUID;

import static com.example.Authmodule.security.Role.USER;

@Service
@Slf4j
public class AuthService {

    private final VerificationTokenService verificationTokenService;

    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

    private final AuthUserDao authUserDao;

    private final JwtService jwtService;

    @Autowired
    public AuthService(VerificationTokenService verificationTokenService,
                       AuthenticationManager authenticationManager,
                       RefreshTokenService refreshTokenService,
                       PasswordEncoder passwordEncoder,
                       MailService mailService,
                       AuthUserDao authUserDao,
                       JwtService jwtService) {
        this.verificationTokenService = verificationTokenService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.authUserDao = authUserDao;
        this.jwtService = jwtService;
    }

    /*
    * TODO> Encrypt user password, resolve verification email problem
    *
     */
    public void signup(AuthUser user) {
        if (authUserDao.isUserPresent(user.getUsername())) {
            throw new IllegalArgumentException(
                    MessageFormat.format("Username '{}' already taken!", user.getUsername()));
        } else {
            authUserDao.insertUser(user);
            String token = generateVerificationToken(user);
            mailService.sendMail(new VerificationEmail(
                    "Activate acccount",
                    user.getEmail(),
                    "http://localhost:8080/api/v1/auth/verification/" + token));
        }
    }

    public String generateVerificationToken(AuthUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .build();
        verificationTokenService.insertVerificationToken(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenService.findByToken(token);
        registerUser(verificationToken);
    }

    public void registerUser(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        AuthUser user = authUserDao.findUserByUsername(username);
        user.setEnabled(true);
        user.setRole(USER);
        authUserDao.insertUser(user);
    }

    public AuthUserResponse login(LoginUserRequest request) {
        Authentication authenticate =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                        (request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token = jwtService.generateToken(authenticate);
        return AuthUserResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                .expiresAt(Instant.now().plusSeconds(jwtService.getExpirationTime()))
                .username(request.getUsername())
                .build();
    }

    public AuthUserResponse refreshToken(RefreshTokenRequest request) {
        refreshTokenService.validateRefreshToken(request.getRefreshToken());
        String token = jwtService.generateTokenWithUsername(request.getUsername());
        return AuthUserResponse.builder()
                .authenticationToken(token)
                .refreshToken(request.getRefreshToken())
                .expiresAt(Instant.now().plusSeconds(jwtService.getExpirationTime()))
                .username(request.getUsername())
                .build();
    }

    public AuthUser getCurrentUser() {
        var principal = (org.springframework.security.core.userdetails.User)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return authUserDao.findUserByUsername(principal.getUsername());
    }
}