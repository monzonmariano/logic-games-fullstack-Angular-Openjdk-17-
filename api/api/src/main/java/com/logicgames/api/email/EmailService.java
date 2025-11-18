package com.logicgames.api.email;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${app.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${APP_FRONTEND_URL}")
    private String frontendBaseUrl;

    private final String FROM_EMAIL = "monzonmariano1@gmail.com";

    private SendGrid sendGridClient;

    public void sendVerificationEmail(String toEmail, String code, String linkToken) {
        String subject = "¡Bienvenido a LogicGames! Confirma tu cuenta";

        // ¡La URL a la que irá el enlace! (La crearemos en el frontend)
        String verificationLink = frontendBaseUrl + "/verify-link?token=" + linkToken;

        String contentBody = "¡Gracias por registrarte! <br>"
                + "Tu código de verificación de 6 dígitos es: "
                + "<h1>" + code + "</h1>"
                + "<p>O, si lo prefieres, activa tu cuenta haciendo clic en el enlace de abajo:</p>"
                + "<a href='" + verificationLink + "' target='_blank'>Activar mi Cuenta</a>"
                + "<p>Si no te has registrado, por favor ignora este email.</p>";

        // El modo simulación ahora imprimirá AMBAS cosas
        sendEmail(toEmail, subject, contentBody, code, verificationLink);
    }

    // --- ¡NUEVO MÉTODO 2! (Para recuperar contraseña) ---
    public void sendPasswordResetCode(String toEmail, String code) {
        String subject = "Tu código de reseteo de contraseña de LogicGames";
        String contentBody = "Hemos recibido una solicitud para resetear tu contraseña. Tu código de 6 dígitos es: "
                + "<h1>" + code + "</h1>"
                + "<p>Introduce este código en la app para establecer una nueva contraseña.</p>"
                + "<p>Si no has solicitado esto, puedes ignorar este email.</p>";

        sendEmail(toEmail, subject, contentBody, code);
    }

    // --- ¡NUEVO MÉTODO DE RESETEO! ---
    public void sendPasswordResetEmail(String toEmail, String code, String linkToken) {
        String subject = "Tu solicitud de reseteo de contraseña de LogicGames";

        // ¡La URL a la que irá el enlace! (Apunta a tu componente existente)
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
    /**
     * Método "helper" privado que construye y envía el email.
     */
    private void sendEmail(String toEmail, String subject, String contentBody, String... debugInfo) {
        // --- ¡MODO SIMULACIÓN (CORREGIDO)! ---
        // Ahora SÍ comprobará la clave del .env
        if (sendGridApiKey == null || sendGridApiKey.equals("SG.FAKE.test_key")) {
            System.out.println("--- MODO SIMULACIÓN DE EMAIL ---");
            System.out.println("A: " + toEmail);
            System.out.println("Asunto: " + subject);
            for (String info : debugInfo) {
                System.out.println("¡DATO DE DEBUG!: " + info);
            }
            System.out.println("---------------------------------");
            return;
        }

        // --- Lógica real de SendGrid ---
        Email from = new Email(FROM_EMAIL);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", contentBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);

            System.out.println("Email enviado exitosamente a: " + toEmail);

        } catch (IOException ex) {
            System.err.println("Error al enviar email: " + ex.getMessage());
            throw new RuntimeException("Error al enviar email", ex);
        }
    }
}
