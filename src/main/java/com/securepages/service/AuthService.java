package com.securepages.service;

import com.securepages.model.AccVerificationToken;
import com.securepages.model.ResetPasswordToken;
import com.securepages.model.Role;
import com.securepages.model.User;
import com.securepages.model.payload.RegisterRequest;
import com.securepages.repository.AccVerificationTokenRepository;
import com.securepages.repository.ResetPasswordTokenRepository;
import com.securepages.repository.RoleRepository;
import com.securepages.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccVerificationTokenRepository accVerificationTokenRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, AccVerificationTokenRepository accVerificationTokenRepository, ResetPasswordTokenRepository resetPasswordTokenRepository, PasswordEncoder passwordEncoder, JavaMailSender javaMailSender) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.accVerificationTokenRepository = accVerificationTokenRepository;
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.javaMailSender = javaMailSender;
    }

    @Transactional(readOnly = true)
    public boolean isUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public boolean register(RegisterRequest registerRequest) {
        try {
            Role guestRole = roleRepository.getById(1L);
            Set<Role> guestRoles = new HashSet<>();
            guestRoles.add(guestRole);

            String passwordEncoded = passwordEncoder.encode(registerRequest.getPassword());
            LocalDate dob = LocalDate.parse(registerRequest.getDateOfBirth(), DateTimeFormatter.ISO_LOCAL_DATE);

            User user = new User(
                    registerRequest.getUsername(),
                    registerRequest.getName(),
                    registerRequest.getEmail(),
                    passwordEncoded,
                    dob,
                    registerRequest.getCountry(),
                    "",
                    guestRoles,
                    true,
                    true,
                    true,
                    false
            );

            User newGuest = userRepository.save(user);

            String token = UUID.randomUUID().toString();
            AccVerificationToken accVerificationToken = new AccVerificationToken(token,newGuest, LocalDateTime.now().plusDays(1));

            accVerificationTokenRepository.save(accVerificationToken);

            sendAccConfirmationMail(user, token);

            return true;

        } catch (Exception e) {
            //logger.error("Error occurred while registering user: ", e);
            return false;
        }
    }

    private void sendAccConfirmationMail(User user, String token) {
        try{
            String recipientAddress = user.getEmail();
            String subject = "Registration Confirmation";
            String confirmationUrl = "http://192.168.0.105/securepages/account_confirmation?rec="+token;

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlMsg = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Account Confirmation</title>\n" +
                    "    <style>\n" +
                    "        body {\n" +
                    "            font-family: Arial, sans-serif;\n" +
                    "            background-color: #f4f4f4;\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "        }\n" +
                    "        .container {\n" +
                    "            background-color: #ffffff;\n" +
                    "            margin: 50px auto;\n" +
                    "            padding: 20px;\n" +
                    "            border-radius: 8px;\n" +
                    "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                    "            max-width: 600px;\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "        .btn {\n" +
                    "            display: inline-block;\n" +
                    "            background-color: #007BFF;\n" +
                    "            color: #ffffff;\n" +
                    "            padding: 10px 20px;\n" +
                    "            border-radius: 5px;\n" +
                    "            text-decoration: none;\n" +
                    "            margin-top: 20px;\n" +
                    "        }\n" +
                    "        .btn:hover {\n" +
                    "            background-color: #0056b3;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<div class=\"container\">\n" +
                    "    <h1>Confirm Your Account</h1>\n" +
                    "    <p>Please click the button below to activate your account.</p>\n" +
                    "    <a href=\""+ confirmationUrl +"\" class=\"btn\">Confirm Account</a>\n" +
                    "</div>\n" +
                    "</body>\n" +
                    "</html>";

            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(htmlMsg, true);

            javaMailSender.send(mimeMessage);

        }catch (MessagingException e){
            //logger.error("Error occurred while registering user: ", e);
        }
    }

    @Transactional
    public boolean accConfirmation(String token) {
        Optional<AccVerificationToken> optionalAccVerification = accVerificationTokenRepository.findByToken(token);
        if (optionalAccVerification.isPresent()) {
            AccVerificationToken accVerificationToken = optionalAccVerification.get();
            if (accVerificationToken.getUser().isEnabled()) {
                return false;
            }
            if (accVerificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {

                return false;
            }
            enableUser(accVerificationToken);
            return true;
        } else {
            return false;
        }
    }

    private void enableUser(AccVerificationToken accVerificationToken) {
        User user = accVerificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        //logger.info("User account enabled: {}", user.getUsername());
    }

    @Transactional
    public boolean forgotPassword(String email){
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        if(optionalUser.isPresent()){
            User user = optionalUser.get();
            if(user.isEnabled()){
                String token = UUID.randomUUID().toString();
                ResetPasswordToken resetPasswordToken =
                        new ResetPasswordToken(
                                token,
                                user,
                                LocalDateTime.now().plusDays(1));

                resetPasswordTokenRepository.save(resetPasswordToken);

                sendResetPasswordAcc(user, token);

                return true;
            }
        }

        return false;
    }

    private void sendResetPasswordAcc(User user, String token) {

        try{
            String recipientAddress = user.getEmail();
            String subject = "Password Reset Request";
            String resetPasswordUrl = "http://192.168.0.105/securepages/reset_password?rec="+token;

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlMsg = "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<title>Reset Password</title>" +
                    "<style>" +
                    "body {" +
                    "font-family: Arial, sans-serif;" +
                    "background-color: #f3f4f6;" +
                    "color: #333;" +
                    "padding: 20px;" +
                    "margin: 0;" +
                    "}" +
                    ".container {" +
                    "max-width: 600px;" +
                    "margin: 0 auto;" +
                    "background-color: #fff;" +
                    "padding: 20px;" +
                    "border-radius: 8px;" +
                    "box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);" +
                    "}" +
                    ".header {" +
                    "text-align: center;" +
                    "padding-bottom: 20px;" +
                    "}" +
                    ".content {" +
                    "text-align: center;" +
                    "padding-bottom: 20px;" +
                    "}" +
                    ".button {" +
                    "display: inline-block;" +
                    "padding: 10px 20px;" +
                    "font-size: 16px;" +
                    "color: #fff;" +
                    "background-color: #1d4ed8;" +
                    "text-decoration: none;" +
                    "border-radius: 5px;" +
                    "transition: background-color 0.3s ease;" +
                    "}" +
                    ".button:hover {" +
                    "background-color: #2563eb;" +
                    "}" +
                    ".footer {" +
                    "text-align: center;" +
                    "color: #6b7280;" +
                    "font-size: 12px;" +
                    "}" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class=\"container\">" +
                    "<div class=\"header\">" +
                    "<h1>Password Reset Request</h1>" +
                    "</div>" +
                    "<div class=\"content\">" +
                    "<p>We received a request to reset your password. Click the button below to reset it.</p>" +
                    "<a href=\""+resetPasswordUrl+"\" class=\"button\">Reset Password</a>" +
                    "</div>" +
                    "<div class=\"footer\">" +
                    "<p>If you did not request a password reset, please ignore this email.</p>" +
                    "<p>&copy; 2024 Your Company</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setTo(recipientAddress);
            helper.setSubject(subject);
            helper.setText(htmlMsg, true);

            javaMailSender.send(mimeMessage);

        }catch (MessagingException e){
            //logger.error("Error occurred while registering user: ", e);
        }

    }

    @Transactional
    public boolean resetPassword(String token,String password){

        if(!resetPasswordTokenRepository.existsByTokenAndDateUpdatedIsNotNull(token)){
            Optional<ResetPasswordToken> optionalResetPasswordToken =
                    resetPasswordTokenRepository.findByToken(token);

            if (optionalResetPasswordToken.isPresent()){
                ResetPasswordToken resetPasswordToken = optionalResetPasswordToken.get();
                User user = resetPasswordToken.getUser();

                String passwordEncoded = passwordEncoder.encode(password);
                user.setPassword(passwordEncoded);
                userRepository.save(user);

                resetPasswordToken.setDateUpdated(LocalDateTime.now());
                resetPasswordTokenRepository.save(resetPasswordToken);

                return true;

            }else{
                return false;
            }
        }

        return false;
    }
}
