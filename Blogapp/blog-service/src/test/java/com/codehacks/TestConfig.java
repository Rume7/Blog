package com.codehacks;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

import com.codehacks.email.client.EmailServiceClient;
import static org.mockito.Mockito.mock;

/**
 * Test configuration for providing mock beans and test-specific configurations
 */
@TestConfiguration
public class TestConfig {

    /**
     * Provides a mock JavaMailSender for tests that doesn't actually send emails
     */
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
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

    /**
     * Provides a mock EmailServiceClient for tests
     */
    @Bean
    @Primary
    public EmailServiceClient emailServiceClient() {
        return mock(EmailServiceClient.class);
    }

    /**
     * Provides a TestRestTemplate bean for integration tests
     */
    @Bean
    @Primary
    public TestRestTemplate testRestTemplate() {
        return new TestRestTemplate();
    }


} 