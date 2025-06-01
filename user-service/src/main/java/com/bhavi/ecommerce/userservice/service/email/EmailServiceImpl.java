package com.bhavi.ecommerce.userservice.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail); // Set the sender
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
            System.out.println("Simple Email sent to: " + to + " with subject: " + subject);
        } catch (MailException e) {
            System.err.println("Error sending simple email to " + to + ": " + e.getMessage());
            // In a real application, you'd log this more robustly and potentially throw a custom exception
            // throw new EmailSendingException("Failed to send email", e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // true for multipart message, "UTF-8" for encoding

            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true indicates HTML content

            javaMailSender.send(message);
            System.out.println("HTML Email sent to: " + to + " with subject: " + subject);
        } catch (MessagingException | MailException e) {
            System.err.println("Error sending HTML email to " + to + ": " + e.getMessage());
            // In a real application, you'd log this more robustly and potentially throw a custom exception
            // throw new EmailSendingException("Failed to send HTML email", e);
        }
    }

}
