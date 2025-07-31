# Production Deployment Guide

## Prerequisites

- Docker and Docker Compose installed
- Kubernetes cluster (for K8s deployment)
- Domain name and SSL certificates
- SMTP credentials for email service
- PostgreSQL database (or use provided container)
- Redis instance (or use provided container)

## 1. Environment Setup

### 1.1 Generate JWT Secret
```bash
# Generate a secure JWT secret (256 bits = 32 bytes)
openssl rand -base64 32
```

### 1.2 Create Environment File
```bash
cp env.example .env
# Edit .env with your production values
```

### 1.3 Required Environment Variables
- `POSTGRES_USER` / `POSTGRES_PASSWORD`: Database credentials
- `REDIS_PASSWORD`: Redis authentication
- `JWT_SECRET`: Base64 encoded secret (256+ bits)
- `MAIL_USERNAME` / `MAIL_PASSWORD`: SMTP credentials
- `MAGIC_LINK_BASE_URL`: Your production domain
- `FRONTEND_URL`: Your frontend domain

## 2. Docker Compose Deployment

### 2.1 Start Services
```bash
# Start all services
docker-compose -f docker-compose.prod.yml up -d

# Check service status
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f blog-service
```

### 2.2 Health Checks
```bash
# Check application health
curl http://localhost/actuator/health

# Check individual services
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

## 3. Kubernetes Deployment

### 3.1 Create Secrets
```bash
# Database credentials
kubectl create secret generic db-credentials \
  --from-literal=username=blogapp_user \
  --from-literal=password=your_secure_password

# Redis credentials
kubectl create secret generic redis-credentials \
  --from-literal=password=your_redis_password

# JWT secret
kubectl create secret generic jwt-secret \
  --from-literal=secret=your_base64_jwt_secret

# Email credentials
kubectl create secret generic email-credentials \
  --from-literal=username=your_email@gmail.com \
  --from-literal=password=your_app_password
```

### 3.2 Deploy Services
```bash
# Apply all Kubernetes manifests
kubectl apply -f kubernetes/

# Check deployment status
kubectl get pods
kubectl get services
```

## 4. Security Considerations

### 4.1 Network Security
- Use HTTPS/TLS for all external communications
- Configure firewall rules to restrict access
- Use VPN for database access if needed

### 4.2 Application Security
- Regularly rotate JWT secrets
- Monitor for suspicious activities
- Implement rate limiting (already configured)
- Use strong passwords for all services

### 4.3 Data Security
- Enable database encryption at rest
- Use encrypted connections (SSL/TLS)
- Regular backups with encryption
- Implement data retention policies

## 5. Monitoring and Logging

### 5.1 Application Monitoring
- Spring Boot Actuator endpoints available at `/actuator`
- Health checks: `/actuator/health`
- Metrics: `/actuator/metrics`
- Application info: `/actuator/info`

### 5.2 Log Management
- Logs are written to `/app/logs/blogapp.log`
- Configure log rotation (100MB max, 30 days retention)
- Consider using ELK stack or similar for centralized logging

### 5.3 Performance Monitoring
- Monitor database connection pool usage
- Track Redis cache hit rates
- Monitor JVM metrics (heap, GC, threads)
- Set up alerts for critical metrics

## 6. Backup and Recovery

### 6.1 Database Backups
```bash
# PostgreSQL backup
docker exec blogapp_postgres_1 pg_dump -U blogapp_user blogapp > backup.sql

# Automated backup script
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec blogapp_postgres_1 pg_dump -U blogapp_user blogapp | gzip > backup_$DATE.sql.gz
```

### 6.2 Application Data
- Backup Redis data (if persistence enabled)
- Backup application logs
- Backup configuration files

## 7. Scaling Considerations

### 7.1 Horizontal Scaling
- Kubernetes deployment supports multiple replicas
- Use load balancer for traffic distribution
- Consider database read replicas for high read loads

### 7.2 Vertical Scaling
- Monitor resource usage and adjust limits
- Consider dedicated database instances for high traffic
- Use Redis cluster for high availability

## 8. Maintenance

### 8.1 Regular Updates
- Keep base images updated
- Monitor for security vulnerabilities
- Update dependencies regularly

### 8.2 Performance Optimization
- Monitor slow queries and optimize
- Adjust cache TTL based on usage patterns
- Consider CDN for static content

## 9. Troubleshooting

### 9.1 Common Issues
- Database connection issues: Check credentials and network
- Redis connection issues: Verify password and connectivity
- Email delivery issues: Check SMTP credentials and limits
- JWT token issues: Verify secret configuration

### 9.2 Debug Commands
```bash
# Check service logs
docker-compose -f docker-compose.prod.yml logs [service-name]

# Check Kubernetes logs
kubectl logs -f deployment/blogapp-backend-deployment

# Check database connectivity
docker exec blogapp_postgres_1 psql -U blogapp_user -d blogapp -c "SELECT 1;"

# Check Redis connectivity
docker exec blogapp_redis_1 redis-cli -a your_password ping
```

## 10. Support

For issues and questions:
- Check application logs first
- Review health check endpoints
- Monitor resource usage
- Consider enabling debug logging temporarily 