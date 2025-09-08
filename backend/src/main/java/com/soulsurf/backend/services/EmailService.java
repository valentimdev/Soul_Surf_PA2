package com.soulsurf.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "Redefinição de Senha";

        String url = "http://localhost:3000/reset-password?token=" + token;

        String text = "Olá, para redefinir sua senha, clique no link abaixo: \n" + url;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nao-responda@soulsurf.com"); // E-mail do remetente
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}