# CampusConnect

CampusConnect is an enterprise web-based Campus Placement Management Portal designed to centralize and streamline placement activities between students, companies/recruiters, and the college placement office.

For complete product specifications, architecture, and functional requirements, refer to the [Technical Requirements Document (TRD)](docs/TECHNICAL_REQUIREMENTS.md).

---

## Technology Stack

- **Platform**: Java 25 (LTS)
- **Framework**: Spring Boot 3.4.2 (Spring MVC, Spring Data JPA, Spring Security)
- **Frontend / Templating**: Thymeleaf, HTML5, CSS3, Bootstrap 5
- **Database**: MySQL 8.0+
- **Persistence / ORM**: Hibernate 6.x
- **Build Tool**: Apache Maven (via Maven Wrapper `./mvnw`)
- **Testing**: JUnit 5, Mockito, Spring Boot Test, H2 (test-scope)

---

## Architecture & Code Structure

CampusConnect follows a clean, layered monolithic architecture deployed as a single Spring Boot application.

```text
campusconnect/
├── docs/
│   ├── TECHNICAL_REQUIREMENTS.md    # Approved Technical Requirements Document
│   └── database/
│       └── init-db.sql              # MySQL database initialization script
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/campusconnect/
│   │   │       └── CampusconnectApplication.java   # Main entry point
│   │   └── resources/
│   │       ├── application.yml                     # Main configuration
│   │       ├── application-example.yml             # Safe template configuration
│   │       ├── static/                             # Static assets (css, js, images)
│   │       └── templates/                          # Thymeleaf server-side templates
│   └── test/
│       ├── java/
│       │   └── com/campusconnect/
│       │       └── CampusconnectApplicationTests.java
│       └── resources/
│           └── application-test.yml                # Isolated test environment config
├── .env.example                                    # Environment variable template
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

Future business modules (per TRD Section 15) will be added under `com.campusconnect`:
`config`, `security`, `auth`, `student`, `company`, `job`, `eligibility`, `application`, `recruitment`, `interview`, `analytics`, `announcement`, `notification`.

---

## Getting Started

### 1. Prerequisites

Ensure you have the following installed on your development machine:
- **Java 25 JDK**: Verify with `java -version`
- **MySQL 8.0 Server**: Verify with `systemctl status mysql` or `mysql --version`
- **Git**

Maven is included via the project's Maven Wrapper (`./mvnw`).

### 2. Database Provisioning

Create the MySQL database and application user using the provided initialization script:

```bash
# Run using MySQL root (or administrator) account
sudo mysql -u root -p < docs/database/init-db.sql
```

Alternatively, run the following SQL statements in the MySQL interactive shell:

```sql
CREATE DATABASE IF NOT EXISTS campusconnect CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'campusconnect'@'localhost' IDENTIFIED BY 'campusconnect_dev_password';
GRANT ALL PRIVILEGES ON campusconnect.* TO 'campusconnect'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Environment Configuration

CampusConnect never commits database passwords or secrets to source control. Configure your local environment using environment variables:

```bash
# Copy template
cp .env.example .env

# Export variables for your local session (or configure in your IDE)
export DB_URL="jdbc:mysql://localhost:3306/campusconnect"
export DB_USERNAME="campusconnect"
export DB_PASSWORD="campusconnect_dev_password"
```

Default connection properties in `application.yml`:
- `DB_URL`: `jdbc:mysql://localhost:3306/campusconnect`
- `DB_USERNAME`: `campusconnect`
- `DB_PASSWORD`: (empty by default; override with your environment variable)

### 4. Build and Test

Execute automated tests using the Maven wrapper:

```bash
./mvnw clean test
```

Build the executable JAR file:

```bash
./mvnw clean package
```

### 5. Running the Application

Start the application with Maven:

```bash
./mvnw spring-boot:run
```

Or run the built JAR artifact:

```bash
java -jar target/campusconnect-0.0.1-SNAPSHOT.jar
```

Once started, access the application in your browser at:
`http://localhost:8080`

---

## Development Guidelines

1. **Layered Architecture**: Keep business logic in `@Service` classes; controllers handle HTTP routing, validation, and view delegation.
2. **Security**: Server-side role and ownership validation is mandatory on all protected endpoints.
3. **No Hardcoded Secrets**: Always inject database credentials and secrets via environment variables or untracked local profiles.
4. **Incremental Implementation**: Adhere strictly to the approved TRD baseline; do not add unapproved features or external integrations.
