package com.bhavi.ecommerce.userservice.service.email;

import org.springframework.stereotype.Service;

@Service
public interface EmailService {
    void sendEmail(String to, String subject, String text);
    void sendHtmlEmail(String to, String subject, String htmlContent); // Useful for richer emails
}
