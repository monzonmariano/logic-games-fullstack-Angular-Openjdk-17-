package com.logicgames.api.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Value("${spring.mail.password:}")
    private String emailPassword; // Aquí usaremos tu API Key de Resend (ej: re_123456...)

    @Value("${APP_FRONTEND_URL}")
    private String frontendBaseUrl;

    private final String FROM_EMAIL = "onboarding@resend.dev";
    private final String RESEND_API_URL = "https://api.resend.com/emails";

    public void sendVerificationEmail(String toEmail, String code, String linkToken) {
        String subject = "¡Bienvenido a LogicGames! Confirma tu cuenta";
        String verificationLink = frontendBaseUrl + "/verify-link?token=" + linkToken;

        String contentBody = "¡Gracias por registrarte! <br>"
                + "Tu código de verificación de 6 dígitos es: "
                + "<h1>" + code + "</h1>"
                + "<p>O, si lo prefieres, activa tu cuenta haciendo clic en el enlace de abajo:</p>"
                + "<a href='" + verificationLink + "' target='_blank'>Activar mi Cuenta</a>"
                + "<p>Si no te has registrado, por favor ignora este email.</p>";

        sendEmailViaApi(toEmail, subject, contentBody);
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        String subject = "Tu código de reseteo de contraseña de LogicGames";
        String contentBody = "Hemos recibido una solicitud para resetear tu contraseña. Tu código de 6 dígitos es: "
                + "<h1>" + code + "</h1>"
                + "<p>Introduce este código en la app para establecer una nueva contraseña.</p>"
                + "<p>Si no has solicitado esto, puedes ignorar este email.</p>";

        sendEmailViaApi(toEmail, subject, contentBody);
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
                + "<p>Si no has solicitado esto, puedes ignora este email.</p>";

        sendEmailViaApi(toEmail, subject, contentBody);
    }

    private void sendEmailViaApi(String toEmail, String subject, String htmlContent) {
        // Modo simulación local si no hay API key real
        if (emailPassword == null || emailPassword.isBlank() || emailPassword.equals("FAKE_PASSWORD")) {
            System.out.println("--- MODO SIMULACIÓN DE EMAIL (API) ---");
            System.out.println("A: " + toEmail);
            System.out.println("Asunto: " + subject);
            System.out.println("-------------------------------------");
            return;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(emailPassword); // Tu API Key de Resend va aquí

            Map<String, Object> body = new HashMap<>();
            body.put("from", FROM_EMAIL);
            body.put("to", new String[]{toEmail});
            body.put("subject", subject);
            body.put("html", htmlContent);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_API_URL, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Email enviado exitosamente vía API a: " + toEmail);
            } else {
                System.err.println("Error de Resend API: " + response.getBody());
            }

        } catch (Exception ex) {
            System.err.println("Error al conectar con Resend API: " + ex.getMessage());
            throw new RuntimeException("Error al enviar email por API", ex);
        }
    }
}