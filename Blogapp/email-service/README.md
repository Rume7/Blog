# Email Service Module

A reusable Spring Boot module for handling email operations, specifically designed for magic link authentication.

## 🚀 Features

- **Magic Link Authentication**: Secure passwordless login via email
- **Token Management**: Database-backed token storage with expiration
- **Email Templates**: Professional email content generation
- **REST API**: HTTP endpoints for email operations
- **Scheduled Cleanup**: Automatic cleanup of expired tokens
- **Comprehensive Logging**: Detailed logging for monitoring
- **Standalone Service**: Can run independently or as a module

## 🔧 Usage

### As a Module (Recommended)

Add the email-service as a dependency to your project:

```xml
<dependency>
    <groupId>com.codehacks</groupId>
    <artifactId>email-service</artifactId>
    <version>1.0.8</version>
</dependency>
```

Import the configuration in your main application:

```java
@SpringBootApplication
@Import(EmailServiceConfig.class)
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

### As a Standalone Service

Run the email service independently:

```bash
cd email-service
mvn spring-boot:run
```

## 🌐 REST API Endpoints

### Send Magic Link Email
```http
POST /api/v1/email/magic-link
Content-Type: application/json

{
  "email": "user@example.com",
  "username": "username"
}
```

### Validate Token
```http
GET /api/v1/email/validate-token?token=abc123...
```

### Get Email from Token
```http
GET /api/v1/email/email-from-token?token=abc123...
```

### Health Check
```http
GET /api/v1/email/health
```

## 🔒 Security Features

- **Secure Token Generation**: UUID-based tokens
- **Token Expiration**: Configurable expiration time
- **Single Use**: Tokens marked as used after validation
- **Token Masking**: Secure logging with masked tokens
- **Database Storage**: Persistent token tracking

## 🧪 Testing

Run the tests:

```bash
mvn test
```

The module includes comprehensive unit tests covering:
- Email sending functionality
- Token validation
- Error scenarios
- Cleanup operations

## 📊 Monitoring

### Logs to Monitor

- `EmailService` - Email operations
- `EmailCleanupScheduler` - Cleanup operations
- `EmailController` - API requests

### Health Check

The service provides a health check endpoint at `/api/v1/email/health`

## 🚀 Deployment

### Docker

```dockerfile
FROM openjdk:17-jre-slim
COPY target/email-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: email-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: email-service
  template:
    metadata:
      labels:
        app: email-service
    spec:
      containers:
      - name: email-service
        image: email-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: MAIL_USERNAME
          valueFrom:
            secretKeyRef:
              name: email-secrets
              key: username
        - name: MAIL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: email-secrets
              key: password
```

## 🔄 Integration Examples

### Running Locally

```bash
mvn spring-boot:run
```

### Testing

```bash
mvn test
```

## 📈 Future Enhancements

- [ ] HTML email templates
- [ ] Email queue (RabbitMQ/Kafka)
- [ ] Rate limiting
- [ ] Multiple email providers
- [ ] Email delivery tracking
- [ ] Template customization API
- [ ] Webhook notifications
- [ ] Metrics and monitoring

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License. 