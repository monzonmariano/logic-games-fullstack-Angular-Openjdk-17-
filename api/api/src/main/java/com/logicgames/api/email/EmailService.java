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
    private String apiKey;

    // (Asegúrate de cambiar esto a tu email verificado en SendGrid)
    private String fromEmail = "monzonmariano1@gmail.com";
    private String fromName = "Equipo de LogicGames";

    private SendGrid sendGridClient;

    @PostConstruct
    public void init() {
        // 1. ¡Crea el cliente! (Así de simple)
        this.sendGridClient = new SendGrid(apiKey);
    }

    public void sendResetLink(String toEmail, String token) {

        // 2. Define el remitente y el destinatario
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);

        // 3. Define el contenido (¡HTML!)
        String resetUrl = "http://localhost:4200/reset-password?token=" + token;
        String htmlContent = "<html><body>" +
                "<h1>¡Hola!</h1>" +
                "<p>Hemos recibido una solicitud para resetear tu contraseña.</p>" +
                "<a href=\"" + resetUrl + "\">Haz clic aquí para resetear tu contraseña</a>" +
                "<p>Si no solicitaste esto, ignora este email.</p>" +
                "</body></html>";

        Content content = new Content("text/html", htmlContent);

        // 4. Crea el "sobre" del email
        Mail mail = new Mail(from, "Resetea tu contraseña de LogicGames", to, content);

        // 5. ¡Envía el email!
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = this.sendGridClient.api(request);

            System.out.println("Email de reseteo enviado a: " + toEmail);
            System.out.println("Status de SendGrid: " + response.getStatusCode());

        } catch (IOException e) {
            System.err.println("Error al enviar email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
