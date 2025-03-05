package com.tcc.edlaine.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender emailSender;
    private final String SUBJECT_EMAIL_SENDER = "Compartilhamento de Documento";
    private final String MESSAGE_TEXT_EMAIL_SENDER = "Você recebeu acesso ao documento: ";

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendEmail(String toEmail,
                          String fileName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(SUBJECT_EMAIL_SENDER);
        message.setText(MESSAGE_TEXT_EMAIL_SENDER + fileName);
        emailSender.send(message);
    }
}