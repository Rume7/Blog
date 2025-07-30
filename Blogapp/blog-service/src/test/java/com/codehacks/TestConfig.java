package com.codehacks;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.mockito.Mockito.mock;

/**
 * Test configuration that provides test-specific beans
 */
@TestConfiguration
public class TestConfig {

    /**
     * Provides a mock EmailService for tests
     */
    @Bean
    @Primary
    public TestEmailService emailService() {
        return mock(TestEmailService.class);
    }

    /**
     * Provides a test JavaMailSender that doesn't actually send emails
     */
    @Bean
    @Primary
    public JavaMailSender testMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("localhost");
        mailSender.setPort(1025);
        mailSender.setUsername("test");
        mailSender.setPassword("test");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");
        
        return mailSender;
    }
} 