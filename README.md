# KYC Service

A Spring Boot microservice responsible for Know Your Customer (KYC) document management, verification workflows, and administrative oversight within a secure banking platform. This service integrates with JWT-based authentication and provides both REST APIs and web-based admin dashboard for comprehensive KYC operations.

## Overview

The KYC Service is a critical component of the banking microservices ecosystem, providing comprehensive document lifecycle management for customer verification processes. It handles document uploads, verification workflows, status tracking, and integration with customer services through secure APIs and administrative interfaces.

### Key Features

- **Document Management**: Secure upload, download, and deletion of KYC documents
- **Verification Workflows**: Admin-driven document verification and rejection processes
- **Multi-format Support**: Support for various document types (Aadhaar, PAN, Photos)
- **JWT Authentication**: Role-based access control with Bearer token validation
- **Admin Dashboard**: Web-based interface for verification management
- **Customer Integration**: Seamless integration with Customer Service APIs
- **Status Tracking**: Real-time KYC status updates and statistics
- **Access Control**: Customer-specific document access restrictions

## Architecture

The service follows a layered architecture pattern with clear separation of concerns:

```
┌─────────────────┐
│  Admin Dashboard│ ← Web Interface Layer
├─────────────────┤
│   Controllers   │ ← REST API Layer
├─────────────────┤
│    Services     │ ← Business Logic Layer
├─────────────────┤
│   Repositories  │ ← Data Access Layer
├─────────────────┤
│    Database     │ ← Oracle 23c
└─────────────────┘
```

### Service Integration

- **Inbound**: Web Gateway (JWT-secured requests)
- **External**: Customer Service API integration
- **Internal**: Admin Dashboard web interface
- **Database**: Oracle 23c for document storage

## Technology Stack

| Component | Technology | Version | Description |
|-----------|------------|---------|-------------|
| Runtime | Java | 17+ | Core application runtime |
| Framework | Spring Boot | 3.x | Main application framework |
| Web | Spring Web MVC | 3.x | REST API implementation |
| Security | Spring Security | 3.x | Authentication and authorization |
| Data Access | Spring Data JPA | 3.x | Database operations |
| Database | Oracle Database | 23c | Primary data storage |
| Migration | Flyway | Latest | Database schema management |
| Mapping | MapStruct | Latest | Object mapping utilities |
| Utilities | Lombok | Latest | Code generation annotations |
| Authentication | JWT (jjwt) | Latest | Token-based authentication |
| Documentation | SpringDoc OpenAPI | Latest | API documentation generation |
| Template Engine | Thymeleaf | Latest | Server-side HTML rendering |
| UI Framework | Bootstrap | 5.x | Frontend styling framework |

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.6+
- Oracle Database 23c
- Customer Service running on port 8081
- Git

## Installation & Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd kyc-service
```

### 2. Database Setup

Ensure Oracle Database is running and accessible:

```sql
-- Create user and schema
CREATE USER KYC_USER IDENTIFIED BY your_password;
GRANT CONNECT, RESOURCE, DBA TO KYC_USER;

-- Create tables (handled by Flyway migrations)
```

### 3. Customer Service Dependencies

Ensure Customer Service is running on port 8081 for API integration.

### 4. Build Application

```bash
./mvnw clean compile
```

### 5. Run Application

```bash
./mvnw spring-boot:run
```

The application will start on port 8084.

## Configuration

### Application Configuration (application.yml)

```yaml
server:
  port: 8084

spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521/FREEPDB1
    username: KYC_USER
    password: your_password
    driver-class-name: oracle.jdbc.OracleDriver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

jwt:
  secret: your_base64_encoded_secret_key
  expiration-ms: 3600000

customer:
  service:
    url: http://localhost:8081

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Oracle database connection URL | `jdbc:oracle:thin:@localhost:1521/FREEPDB1` |
| `DB_USERNAME` | Database username | `KYC_USER` |
| `DB_PASSWORD` | Database password | Required |
| `JWT_SECRET` | JWT signing secret (Base64) | Required |
| `CUSTOMER_SERVICE_URL` | Customer Service base URL | `http://localhost:8081` |

## KYC Service APIs

### Authentication

All endpoints require JWT authentication via the `Authorization` header:

```
Authorization: Bearer <jwt_token>
```

### Customer Operations

#### Upload Document

**POST** `/api/kyc/upload`

Upload a KYC document. Customer ID is automatically resolved from JWT token.

**Request:**
```
Content-Type: multipart/form-data
- name: "Aadhaar Card"
- file: [document file]
```

**Response:**
```json
{
  "documentId": 1,
  "customerId": 123,
  "documentName": "Aadhaar Card",
  "documentType": "image/jpeg",
  "status": "PENDING",
  "uploadedAt": "2024-01-15T10:30:00Z"
}
```

#### Get My Documents

**GET** `/api/kyc/my-documents`

Retrieve all documents for the authenticated customer.

#### Download Document

**GET** `/api/kyc/document/{documentId}/download`

Download a specific document (access controlled).

#### Delete Document

**DELETE** `/api/kyc/document/{documentId}`

Delete a pending document (customers can only delete their own pending documents).

### Administrative API Operations

#### Get Pending Verifications

**GET** `/api/kyc/admin/pending-verifications`

Returns all documents awaiting verification. Requires ADMIN role.

#### Get Customer Documents

**GET** `/api/kyc/admin/documents/{customerId}`

Administrative access to all documents for a specific customer.

#### Verify Document

**PUT** `/api/kyc/admin/verify/{documentId}?message=verification_message`

Verify a KYC document with optional message.

#### Reject Document

**PUT** `/api/kyc/admin/reject/{documentId}?message=rejection_reason`

Reject a KYC document with mandatory rejection reason.

#### Get Statistics

**GET** `/api/kyc/admin/stats`

Returns KYC verification statistics.

**Response:**
```json
{
  "total": 150,
  "pending": 25,
  "verified": 100,
  "rejected": 25
}
```

### API Documentation Access

- Swagger UI: `http://localhost:8084/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8084/v3/api-docs`

## Admin Dashboard

### Web Interface

The KYC Service provides a comprehensive web-based admin dashboard for document verification management.

#### Access Points

- **Login**: `http://localhost:8084/admin/login`
- **Dashboard**: `http://localhost:8084/admin/dashboard`

#### Default Admin Credentials

```
Username: admin
Password: admin123
```

### Dashboard Features

#### Main Dashboard

- **KYC Statistics Overview**: Real-time statistics display
- **Customer Search**: Search by name, email, phone, or PAN
- **KYC Status Filtering**: Filter by PENDING, VERIFIED, REJECTED
- **Pagination**: Efficient handling of large customer lists

#### Customer Details View

- **Customer Information**: Complete customer profile display
- **Document Management**: View all uploaded documents
- **Document Actions**: Download, verify, or reject documents
- **Status Tracking**: Real-time verification status updates

#### Administrative Functions

##### Document Verification Workflow

1. **View Pending Documents**: Access documents awaiting verification
2. **Document Review**: Download and examine submitted documents
3. **Verification Decision**: Approve or reject with comments
4. **Status Update**: Automatic status synchronization with Customer Service

##### Customer KYC Approval

1. **Document Validation**: Ensure all required documents are verified
2. **KYC Approval**: Bulk approval when all documents are verified
3. **Service Integration**: Automatic status update to Customer Service
4. **Audit Trail**: Complete verification history maintenance

### Security Features

- **Session Management**: Secure HTTP-only cookie authentication
- **Role-Based Access**: Admin-only access to verification functions
- **CSRF Protection**: Built-in cross-site request forgery protection
- **Input Validation**: Comprehensive server-side validation

## Database Schema

### KYC Documents Table

```sql
CREATE TABLE KYC_DOCUMENTS (
    document_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id NUMBER(19) NOT NULL,
    document_name VARCHAR2(100) NOT NULL,
    document_type VARCHAR2(100) NOT NULL,
    document_content BLOB NOT NULL,
    status VARCHAR2(20) DEFAULT 'PENDING',
    message VARCHAR2(1000),
    verified_by VARCHAR2(100),
    verified_at TIMESTAMP,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Users Table

```sql
CREATE TABLE USERS (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR2(100) UNIQUE NOT NULL,
    role VARCHAR2(20) NOT NULL,
    password_hash VARCHAR2(255),
    enabled NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Data Validation Rules

- **Document Names**: AADHAAR, PAN, PHOTO (case-insensitive)
- **Verification Status**: PENDING, VERIFIED, REJECTED
- **File Types**: Images (JPEG, PNG), PDF documents
- **Access Control**: Customers can only access their own documents

## Security Implementation

### JWT Authentication

The service implements comprehensive JWT-based authentication:

- **Token Validation**: All protected endpoints validate JWT tokens
- **Role-Based Access**: ADMIN and CUSTOMER roles with different permissions
- **User Context**: Authenticated user information available in request context
- **Cross-Service Integration**: Token-based communication with Customer Service

### Security Components

- **JwtTokenFilter**: Handles token extraction and validation
- **JwtUtils**: Token generation and parsing utilities
- **SecurityConfig**: Comprehensive security configuration
- **CustomUserDetailsService**: User authentication service

### Protected Resources

All endpoints except admin login require valid JWT authentication.

## Customer Service Integration

### API Client Integration

The service integrates with Customer Service through a dedicated client:

```java
@Component
public class CustomerServiceClient {
    // Resolves userId to customerId
    public Long getCustomerIdByUserId(Long userId, String jwtToken);
    
    // Verifies document ownership
    public boolean verifyCustomerOwnership(Long customerId, Long userId, String jwtToken);
}
```

### Integration Points

1. **User Resolution**: Convert JWT userId to Customer Service customerId
2. **Ownership Verification**: Ensure users can only access their documents
3. **KYC Status Updates**: Synchronize verification status changes
4. **Customer Data Retrieval**: Fetch customer details for admin dashboard

## Testing with Postman

### Import Postman Collection

1. **Download Collection**: Use the provided `KYC Service APIs Copy.postman_collection.json`
2. **Import to Postman**: File → Import → Select the JSON file
3. **Set Environment Variables**:
   ```
   base_url: localhost:8084
   auth_token: <customer_jwt_token>
   admin_token: <admin_jwt_token>
   documentId: <document_id_for_testing>
   customerId: <customer_id_for_testing>
   userId: 21
   ```

### Test Scenarios

#### Customer Flow Testing

1. **Authentication**: Obtain JWT token from Auth Service
2. **Document Upload**:
   ```bash
   POST /api/kyc/upload
   - Set Authorization header with customer token
   - Add form data: name="Aadhaar Card", file=[select file]
   ```
3. **View Documents**: `GET /api/kyc/my-documents`
4. **Download Document**: `GET /api/kyc/document/{documentId}/download`

#### Admin Flow Testing

1. **Admin Authentication**: Obtain admin JWT token
2. **View Pending Verifications**: `GET /api/kyc/admin/pending-verifications`
3. **Verify Document**: `PUT /api/kyc/admin/verify/{documentId}?message=Approved`
4. **Reject Document**: `PUT /api/kyc/admin/reject/{documentId}?message=Unclear image`
5. **View Statistics**: `GET /api/kyc/admin/stats`

#### Dashboard Testing

1. **Admin Login**: Navigate to `http://localhost:8084/admin/login`
2. **Dashboard Access**: Use credentials (admin/admin123)
3. **Customer Management**: Search, filter, and manage customers
4. **Document Verification**: Review and verify documents through UI

## Project Structure

```
src/main/java/com/bank/kyc/
├── client/                   # External service clients
│   └── CustomerServiceClient.java
├── config/                   # Configuration classes
│   ├── CorsConfig.java
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── WebConfig.java
├── controller/               # REST controllers
│   ├── AdminKYCController.java
│   ├── AdminLoginViewController.java
│   ├── AdminViewController.java
│   ├── AuthController.java
│   └── KycController.java
├── dto/                     # Data transfer objects
│   ├── AdminLoginRequest.java
│   ├── AdminLoginResponse.java
│   ├── CustomerDTO.java
│   ├── JwtResponse.java
│   ├── KycDocumentResponse.java
│   ├── KycStatsDTO.java
│   ├── KycUploadRequest.java
│   └── VerifyRequest.java
├── entity/                  # JPA entities
│   ├── KycDocument.java
│   └── User.java
├── enums/                   # Enumeration classes
│   └── DocumentStatus.java
├── exception/               # Exception handling
│   ├── CustomAuthException.java
│   └── GlobalExceptionHandler.java
├── repository/              # Data repositories
│   ├── KycDocumentRepository.java
│   └── UserRepository.java
├── security/                # Security components
│   ├── CustomUserDetailsService.java
│   ├── JwtService.java
│   ├── JwtTokenFilter.java
│   └── JwtUtils.java
├── service/                 # Business logic
│   ├── AdminAuthService.java
│   ├── AdminDashboardService.java
│   ├── AuthService.java
│   ├── CustomerIntegrationService.java
│   ├── KycService.java
│   └── impl/
│       ├── AuthServiceImpl.java
│       └── KycServiceImpl.java
├── util/                    # Utility classes
│   ├── AuthenticatedUser.java
│   ├── JWTUtil.java
│   └── VerificationStatus.java
└── KycServiceApplication.java

src/main/resources/
├── db/migration/            # Flyway migration scripts
├── static/                  # Static web assets
│   ├── css/
│   └── js/
├── templates/               # Thymeleaf templates
│   └── admin/
│       ├── login.html
│       ├── dashboard.html
│       └── customer-details.html
└── application.yml
```

## Error Handling

The service implements comprehensive error handling:

### Exception Types

- **CustomAuthException**: Authentication and authorization failures
- **RuntimeException**: Business logic violations
- **ValidationException**: Input validation failures
- **SecurityException**: Access control violations

### Error Response Format

```json
{
  "error": {
    "code": "AUTH_ERROR",
    "message": "Invalid or expired token",
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

## Logging

### Logging Configuration

The service uses SLF4J with Logback for structured logging:

- **INFO**: Business operations and service interactions
- **DEBUG**: Detailed tracing for development
- **ERROR**: Exception handling and system errors
- **WARN**: Security violations and validation failures

### Log Categories

```yaml
logging:
  level:
    com.bank.kyc: INFO
    com.bank.kyc.security: DEBUG
    org.springframework.security: DEBUG
```

## Data Migration

Database schema is managed using Flyway migrations:

### Migration Files

- **V1__Create_kyc_documents_table.sql**: KYC documents table creation
- **V2__Create_users_table.sql**: Users table for authentication
- **V3__Insert_admin_user.sql**: Default admin user setup

### Migration Commands

```bash
# Check migration status
./mvnw flyway:info

# Migrate to latest version
./mvnw flyway:migrate

# Validate current schema
./mvnw flyway:validate
```

## Monitoring and Health Checks

### Actuator Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### Health Check URLs

- **Application Health**: `http://localhost:8084/actuator/health`
- **Application Info**: `http://localhost:8084/actuator/info`
- **Metrics**: `http://localhost:8084/actuator/metrics`

## Troubleshooting

### Common Issues

1. **Database Connection Failures**
   - Verify Oracle service is running
   - Check connection parameters in application.yml
   - Validate user permissions and schema access

2. **JWT Authentication Errors**
   - Verify JWT secret configuration matches other services
   - Check token expiration settings
   - Validate token format in requests

3. **Customer Service Integration Issues**
   - Ensure Customer Service is running on port 8081
   - Check network connectivity between services
   - Verify JWT token compatibility

4. **File Upload Failures**
   - Check file size limits in configuration
   - Verify supported file types
   - Ensure sufficient disk space for blob storage

### Debug Mode

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    com.bank.kyc: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

