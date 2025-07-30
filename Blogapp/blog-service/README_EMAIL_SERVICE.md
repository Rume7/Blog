# Email Service Implementation

## Overview

The Email Service provides a complete magic link authentication system for the BlogApp. It handles the generation, sending, and validation of magic link tokens for secure passwordless authentication.

## Features

- ✅ **Magic Link Generation**: Secure UUID-based tokens
- ✅ **Email Sending**: SMTP-based email delivery
- ✅ **Token Validation**: Expiration and usage tracking
- ✅ **Database Storage**: Persistent token storage
- ✅ **Automatic Cleanup**: Scheduled cleanup of expired tokens
- ✅ **Comprehensive Logging**: Detailed logging for debugging
- ✅ **Error Handling**: Proper exception handling
- ✅ **Testing**: Full test coverage

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   AuthService   │───▶│   EmailService   │───▶│  JavaMailSender │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         │                       ▼                       │
         │              ┌──────────────────┐             │
         │              │ MagicLinkToken   │             │
         │              │   Repository     │             │
         │              └──────────────────┘             │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  AuthController │    │  MagicLinkToken  │    │   Email Server  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## Components

### 1. EmailService
Main service class that orchestrates the magic link process:
- `sendMagicLinkEmail()` - Generates and sends magic link emails
- `validateMagicLinkToken()` - Validates tokens and marks them as used
- `getEmailFromToken()` - Retrieves email associated with a token
- `cleanupExpiredTokens()` - Removes expired tokens

### 2. MagicLinkToken Entity
JPA entity for storing magic link tokens:
- Unique token string
- Email association
- Creation and expiration timestamps
- Usage tracking

### 3. EmailTemplateService
Generates email content with proper formatting:
- Magic link emails
- Welcome emails
- Password reset emails

### 4. EmailCleanupScheduler
Scheduled task that runs every hour to clean up expired tokens.

## Configuration

### Email Settings (application.yml)
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  magic-link:
    base-url: ${MAGIC_LINK_BASE_URL:http://localhost:3000}
    expiration-minutes: ${MAGIC_LINK_EXPIRATION_MINUTES:15}
```

### Environment Variables
- `MAIL_USERNAME`: Email account username
- `MAIL_PASSWORD`: Email account password/app password
- `MAGIC_LINK_BASE_URL`: Frontend URL for magic links
- `MAGIC_LINK_EXPIRATION_MINUTES`: Token expiration time

## API Endpoints

### 1. Initiate Magic Link Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### 2. Verify Magic Link
```http
GET /api/v1/auth/verify-magic-link?token=abc123...
```

## Security Features

- **Secure Token Generation**: UUID-based tokens
- **Token Expiration**: Configurable expiration time (default: 15 minutes)
- **Single Use**: Tokens are marked as used after validation
- **Token Masking**: Tokens are masked in logs for security
- **Rate Limiting**: Ready for rate limiting implementation

## Testing

The email service includes comprehensive tests:
- Unit tests for all service methods
- Mock-based testing for external dependencies
- Error scenario testing
- Token validation testing

Run tests with:
```bash
mvn test -Dtest=EmailServiceTest
```

## Monitoring

The service includes detailed logging:
- INFO level for successful operations
- WARN level for validation failures
- ERROR level for exceptions
- DEBUG level for detailed operations

## Future Enhancements

- [ ] HTML email templates
- [ ] Email queue for high-volume scenarios
- [ ] Rate limiting per email address
- [ ] Email delivery tracking
- [ ] Multiple email provider support
- [ ] Email templates customization via admin panel

## Troubleshooting

### Common Issues

1. **Email not sending**: Check SMTP configuration and credentials
2. **Token validation failing**: Check database connection and token expiration
3. **Cleanup not working**: Verify scheduling is enabled with `@EnableScheduling`

### Logs to Monitor

- `EmailService` logs for email operations
- `EmailCleanupScheduler` logs for cleanup operations
- Database connection logs for token operations 