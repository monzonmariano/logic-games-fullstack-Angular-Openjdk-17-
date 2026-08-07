package com.logicgames.api.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Leemos la contraseña del correo desde las propiedades
    @Value("${spring.mail.password:}")
    private String emailPassword;

    @Value("${APP_FRONTEND_URL}")
    private String frontendBaseUrl;

    // IMPORTANTE: Resend te pedirá enviar desde 'onboarding@resend.dev'
    // hasta que verifiques tu propio dominio (ej. hola@logicgames.com).
    private final String FROM_EMAIL = "onboarding@resend.dev";

    public void sendVerificationEmail(String toEmail, String code, String linkToken) {
        String subject = "¡Bienvenido a LogicGames! Confirma tu cuenta";
        String verificationLink = frontendBaseUrl + "/verify-link?token=" + linkToken;

        String contentBody = "¡Gracias por registrarte! <br>"
                + "Tu código de verificación de 6 dígitos es: "
                + "<h1>" + code + "</h1>"
                + "<p>O, si lo prefieres, activa tu cuenta haciendo clic en el enlace de abajo:</p>"
                + "<a href='" + verificationLink + "' target='_blank'>Activar mi Cuenta</a>"
                + "<p>Si no te has registrado, por favor ignora este email.</p>";

        sendEmail(toEmail, subject, contentBody, code, verificationLink);
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        String subject = "Tu código de reseteo de contraseña de LogicGames";
        String contentBody = "Hemos recibido una solicitud para resetear tu contraseña. Tu código de 6 dígitos es: "
                + "<h1>" + code + "</h1>"
                + "<p>Introduce este código en la app para establecer una nueva contraseña.</p>"
                + "<p>Si no has solicitado esto, puedes ignorar este email.</p>";

        sendEmail(toEmail, subject, contentBody, code);
    }

    public void sendPasswordResetEmail(String toEmail, String code, String linkToken) {
        String subject = "Tu solicitud de reseteo de contraseña de LogicGames";
        String resetLink = frontendBaseUrl + "/reset-password?token=" + linkToken;

        String contentBody = "Has solicitado resetear tu contraseña.<br>"
                + "Tu código de 6 dígitos es: "
                + "<h1>" + code + "</h1>"
                + "<p>Introduce este código en la app para continuar.</p>"
                + "<p>O, si lo prefieres, haz clic en el enlace de abajo:</p>"
                + "<a href='" + resetLink + "' target='_blank'>Resetear mi Contraseña</a>"
                + "<p>Si no has solicitado esto, puedes ignorar este email.</p>";

        sendEmail(toEmail, subject, contentBody, code, resetLink);
    }

    private void sendEmail(String toEmail, String subject, String contentBody, String... debugInfo) {
        // Mantenemos tu genial modo de simulación
        if (emailPassword == null || emailPassword.isBlank() || emailPassword.equals("FAKE_PASSWORD")) {
            System.out.println("--- MODO SIMULACIÓN DE EMAIL ---");
            System.out.println("A: " + toEmail);
            System.out.println("Asunto: " + subject);
            for (String info : debugInfo) {
                System.out.println("¡DATO DE DEBUG!: " + info);
            }
            System.out.println("---------------------------------");
            return;
        }

        try {
            // Lógica estándar de Spring Boot para enviar HTML
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM_EMAIL);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(contentBody, true); // El 'true' indica que es HTML

            mailSender.send(message);
            System.out.println("Email enviado exitosamente a: " + toEmail);

        } catch (MessagingException ex) {
            System.err.println("Error al enviar email: " + ex.getMessage());
            throw new RuntimeException("Error al enviar email", ex);
        }
    }
}