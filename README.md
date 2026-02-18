# Multi-Tenant Appointment & Scheduling System

A professional, high-performance backend system built with Spring Boot for managing appointments across multiple independent organizations (tenants).

## 🚀 Key Features

- **Multi-Tenancy**: Data isolation at the database level using `tenant_id` and unique `slug` URL routing.
- **Dynamic Scheduling**: Flexible staff working hours with availability validation.
- **Staff Management**: Role-based access control (Owner, Manager, Staff) for organizational hierarchy.
- **Service Catalog**: Customizable service lists (employments) with duration and pricing.
- **Appointment Lifecycle**: Complete flow from booking/pending to confirmation, completion, or cancellation.
- **Secure Authentication**: JWT-based security with UUID-based user identification.

## 🏗️ System Architecture

The following diagram illustrates the core entity relationships and the multi-tenant structure:

```mermaid
erDiagram
    TENANT ||--o{ USER : "hosts"
    TENANT ||--o{ STAFF : "employs"
    TENANT ||--o{ EMPLOYEMENT : "offers"
    TENANT ||--o{ APPOINTMENT : "manages"
    
    USER ||--o{ STAFF : "is_linked_to"
    
    STAFF ||--o{ STAFF_SCHEDULE : "has"
    STAFF ||--o{ STAFF_SERVICES : "performs"
    
    EMPLOYEMENT ||--o{ STAFF_SERVICES : "assigned_to"
    EMPLOYEMENT ||--o{ APPOINTMENT : "booked_for"
    
    STAFF ||--o{ APPOINTMENT : "assigned_to"

    TENANT {
        Long id PK
        String slug UK
        String organization_name
    }
    USER {
        UUID id PK
        String email UK
        String status
    }
    STAFF {
        UUID id PK
        Enum role
        String position
    }
    APPOINTMENT {
        UUID id PK
        Date date
        Time start_time
        Enum status
    }
```

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3+ (Java 25)
- **Database**: PostgreSQL (UUID Primary Keys)
- **Migration**: Flyway
- **Security**: Spring Security & Structured JWT
- **Build Tool**: Gradle
- **Documentation**: Swagger/OpenAPI (Optional)

## 🚦 Getting Started

### Prerequisites
- JDK 21+
- PostgreSQL 15+

### Installation
1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```
2. Configure your database in `src/main/resources/application.yml`.
3. Run the application:
   ```bash
   ./gradlew bootRun
   ```
