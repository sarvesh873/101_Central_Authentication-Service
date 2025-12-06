# Central Authentication Service

[![Build Status](https://github.com/yourusername/101_Central_Authentication-Service/actions/workflows/build.yml/badge.svg)](https://github.com/yourusername/101_Central_Authentication-Service/actions)
[![Coverage](https://img.shields.io/codecov/c/github/yourusername/101_Central_Authentication-Service)](https://codecov.io/gh/yourusername/101_Central_Authentication-Service)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

A high-performance, secure, and scalable authentication microservice built with Spring Boot 3.x. This service provides JWT-based authentication and user management capabilities with support for virtual threads.

## 🚀 Features

- **JWT-based Authentication**
- **User Management** (Create, Read, Update, Delete)
- **Role-based Access Control (RBAC)**
- **Virtual Threads** for improved concurrency
- **OpenAPI 3.0** documentation
- **Containerized** with Docker
- **CI/CD** ready
- **90%+ Test Coverage**

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker 20.10+ (optional)
- PostgreSQL 14+ (or compatible database)

## 🛠️ Installation

### Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/sarvesh873/101_Central_Authentication-Service.git
   cd 101_Central_Authentication-Service
   ```

2. Configure the database in `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/auth_db
       username: your_username
       password: your_password
   ```

3. Build and run:
   ```bash
   mvn spring-boot:run
   ```

### Using Docker

```bash
docker-compose up -d
```

## 🔧 Configuration

| Environment Variable | Description | Default                                  |
|----------------------|-------------|------------------------------------------|
| `SERVER_PORT` | Application port | 8086                                     |
| `SPRING_DATASOURCE_URL` | Database URL | jdbc:postgresql://localhost:5432/auth_db |
| `JWT_SECRET` | Secret key for JWT | your-256-bit-secret                      |
| `JWT_EXPIRATION_MS` | JWT expiration time in milliseconds | 3600000 (1 hour)                         |

## 📚 API Documentation

Once the application is running, access the following:

- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **OpenAPI 3.0 Docs**: http://localhost:8084/v3/api-docs

## 🧪 Testing

Run the test suite with coverage:

```bash
mvn clean test jacoco:report
```

## 🚀 Deployment upcoming

### Kubernetes

```bash
kubectl apply -f k8s/
```

### Helm

```bash
helm install auth-service ./charts/auth-service
```

## 🛡️ Security

- Password hashing with BCrypt
- JWT with RSA 256-bit encryption
- Role-based access control
- Input validation
- CORS protection
- CSRF protection (for web clients)
- Rate limiting

## 📈 Monitoring

The service exposes Prometheus metrics at `/actuator/prometheus` and includes:

- Request/response metrics
- JVM metrics
- Database connection pool metrics
- Custom business metrics

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot Team
- OpenAPI Community
- All Contributors

---

