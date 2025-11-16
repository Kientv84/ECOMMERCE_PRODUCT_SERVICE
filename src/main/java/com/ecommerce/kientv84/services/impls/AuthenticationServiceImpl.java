package com.ecommerce.kientv84.services.impls;

import com.ecommerce.kientv84.commons.EnumError;
import com.ecommerce.kientv84.commons.SuccessEnum;
import com.ecommerce.kientv84.config.JWT.JwtUtil;
import com.ecommerce.kientv84.dtos.request.ChangePasswordRequest;
import com.ecommerce.kientv84.dtos.request.LoginRequest;
import com.ecommerce.kientv84.dtos.request.ResetPasswordRequest;
import com.ecommerce.kientv84.dtos.request.TokenRefreshRequest;
import com.ecommerce.kientv84.dtos.response.ChangePasswordResponse;
import com.ecommerce.kientv84.dtos.response.TokenRefreshResponse;
import com.ecommerce.kientv84.entites.UserEntity;
import com.ecommerce.kientv84.dtos.response.LoginResponse;
import com.ecommerce.kientv84.exceptions.ServiceException;
import com.ecommerce.kientv84.respositories.UserRepository;
import com.ecommerce.kientv84.services.AuthenticationService;
import com.ecommerce.kientv84.services.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final JavaMailSender mailSender; // Hỗ trợ send email

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            UserEntity user = userRepository.findByUserEmail(request.getEmail());

            if (user == null) {
                throw new ServiceException(EnumError.ACC_NOT_FOUND, "user.not.found");
            }

            Boolean isMatch = passwordEncoder.matches(request.getPassword(), user.getUserPassword());

            if (!isMatch) {
                throw new ServiceException(EnumError.ACC_ERR_INVALID_PASSWORD, "user.invalid.password");
            }

            String accessToken = jwtUtil.generateToken(user.getUserEmail());

            String refreshToken = jwtUtil.generateRefreshToken(user.getUserEmail());

            if (isMatch) {
                return new LoginResponse(SuccessEnum.SUCCESS.getCode(), "Login successfully", accessToken, refreshToken, user.getUserName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new LoginResponse(SuccessEnum.ERROR.getCode(), "Login fail");
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String oldRefreshToken = request.getRefreshToken();

        if (oldRefreshToken == null || oldRefreshToken.isEmpty()) {
            throw new ServiceException(EnumError.AUTH_ERR_INVALID_TOKEN, "auth.token.invalid");
        }

        String email = jwtUtil.validateRefreshToken(oldRefreshToken);

        String newAccess = jwtUtil.generateToken(email);

        String newRefresh = jwtUtil.generateRefreshToken(email);

        return new TokenRefreshResponse(newAccess, newRefresh);
    }

    @Override
    public ChangePasswordResponse changePassword(ChangePasswordRequest request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        log.info("email {}", email);
        log.info("SecurityContextHolder {}", SecurityContextHolder.getContext().getAuthentication());

        UserEntity user = userRepository.findByUserEmail(email);

        if (user == null) {
            throw new ServiceException(EnumError.ACC_NOT_FOUND, "user.not.found");
        }

        Boolean confirmOldPass = passwordEncoder.matches(request.getOldPassword(), user.getUserPassword());

        if (!confirmOldPass) {
            return new ChangePasswordResponse(SuccessEnum.ERROR.getCode(), "Entering password not match");
        }

        String newPassWord = passwordEncoder.encode(request.getNewPassword());
        user.setUserPassword(newPassWord);

        userRepository.save(user);

        return new ChangePasswordResponse(SuccessEnum.SUCCESS.getCode(), "Update password success");
    }

    @Override
    public void sendResetPasswordCode(String email) {
        UserEntity user = userRepository.findByUserEmail(email);
        if (user == null) {
            throw new ServiceException(EnumError.ACC_NOT_FOUND, "User not found");
        }

        // Tạo otp ngẫu nhiên 6 số
        String otp = String.format("%06d", (int)(Math.random() * 1000000));

        // Lưu otp vào Redis theo key: reset:password:{email}, TTL 10 phút
        redisService.setValue("reset:password:" + email, otp, 10 * 60);

        // Gửi otp qua email
        sendEmail(email, "Reset password code", "Your code is: " + otp);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String inputCode = request.getOtp();
        String key = "reset:password:" + email;

        String savedCode = redisService.getValue(key, String.class);

        if (savedCode == null) {
            throw new ServiceException(EnumError.AUTH_ERR_INVALID_TOKEN, "Reset code expired or invalid");
        }

        if (!savedCode.equals(inputCode)) {
            throw new ServiceException(EnumError.AUTH_ERR_INVALID_TOKEN, "Reset code is incorrect");
        }

        UserEntity user = userRepository.findByUserEmail(email);
        if (user == null) {
            throw new ServiceException(EnumError.ACC_NOT_FOUND, "User not found");
        }

        // Update password
        user.setUserPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xóa code sau khi dùng
        redisService.deleteByKey(key);
    }

    @Override
    @Async
    public void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}

