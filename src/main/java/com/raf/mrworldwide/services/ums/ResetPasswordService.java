package com.raf.mrworldwide.services.ums;

import com.raf.mrworldwide.dao.repositories.ResetPasswordRepository;
import com.raf.mrworldwide.domain.dto.user.ResetPasswordRequest;
import com.raf.mrworldwide.domain.entities.user.ResetPassword;
import com.raf.mrworldwide.domain.entities.user.User;
import com.raf.mrworldwide.exceptions.ValidationException;
import com.raf.mrworldwide.services.email.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResetPasswordService {

    private final AuthService authService;
    private final ResetPasswordRepository resetPasswordRepository;
    private final EmailTemplateService emailTemplateService;

    private static final int KEY_LENGTH = 6;
    @Value("${endpoint.fe:#{null}}")
    public String feEndpoint;

    public void forgotPassword(String email) {
        User user = authService.getUserByEmail(email);

        String secretKey = RandomStringUtils.secure().nextNumeric(KEY_LENGTH);
        ResetPassword resetPassword = ResetPassword.builder()
                .secretKey(DigestUtils.sha256Hex(secretKey))
                .expirationTime(ZonedDateTime.now().plusMinutes(30))
                .userEmail(user.getEmail())
                .used(false)
                .build();
        resetPassword = resetPasswordRepository.save(resetPassword);
        log.debug(String.format("Saved reset password request for user: %s", email));

        String confirmationLink = String.format("%s/reset-password?token=%s", feEndpoint, resetPassword.getId().toString());
        emailTemplateService.resetPassword(Collections.singletonList(user.getEmail()), user.getFirstName(), confirmationLink, secretKey);
    }

    public void resetPassword(ResetPasswordRequest request) {
        ResetPassword resetPassword = resetPasswordRepository.findById(request.token())
                .orElseThrow(() -> new ValidationException("Invalid token"));

        if (!DigestUtils.sha256Hex(request.secretKey()).equals(resetPassword.getSecretKey())) {  // check if secret keys matching
            throw new ValidationException("Invalid key.");
        }
        if (ZonedDateTime.now().isAfter(resetPassword.getExpirationTime())) {  // check if token expired
            throw new ValidationException("Token has expired.");
        }
        if (resetPassword.isUsed()) {  // check if user already reset password
            throw new ValidationException("Token has already been used.");
        }

        resetPassword.setUsed(true);
        resetPasswordRepository.save(resetPassword);
        authService.resetPassword(resetPassword.getUserEmail(), request.newPassword());
        log.info("Password has been successfully reset, email: {}", resetPassword.getUserEmail());
    }

}
