# CampusConnect: Enterprise Campus Placement Management Portal
## PowerPoint Presentation Content Guide & Speaker Deck
### Course: UCS553 – Enterprise Web Application (EWA)
**Department of Computer Science & Engineering**  
**Thapar Institute of Engineering & Technology, Patiala**

---

## Presentation Metadata & Overview

- **Target Duration:** 15–20 Minutes (including 3–5 min Live Demo & Q&A)
- **Total Slides:** 14 Slides (Ideal for Academic Defense & Professor Evaluation)
- **Style:** Clean, professional, high-density bullet points; no dense paragraphs.
- **Tone:** Technical, architecture-focused, adhering strictly to the implemented codebase.

---

## Slide 1: Title & Project Identification

### Slide Header & Title
- **Main Title:** CampusConnect
- **Subtitle:** An Enterprise Web-Based Campus Placement Management Portal
- **Course:** UCS553: Enterprise Web Application Mini Project
- **Department:** Computer Science & Engineering Department
- **Institution:** Thapar Institute of Engineering & Technology, Patiala
- **Date:** November 2026

### Slide Content (Key Highlights)
- **Project Paradigm:** Multi-Role Enterprise Web Application (EWA)
- **Primary Backend:** Java 21 (LTS) & Spring Boot 3.4.2
- **Persistence & DB:** Spring Data JPA / Hibernate 6 & MySQL 8.0
- **Security Standard:** Spring Security 6 with Multi-Tenant Role-Based Access Control (RBAC)
- **Presentation Layer:** Server-Side Rendered (SSR) Thymeleaf + Bootstrap 5
- **Student Team:** [Roll Number – Student 1 | Roll Number – Student 2 | Roll Number – Student 3]
- **Project Supervisor:** Dr. [Faculty Name], Assistant Professor, CSED

### Suggested Visuals & Layout
- Two-column corporate title slide:
  - **Left Column:** Official Institute / University Crest & Project Title Banner.
  - **Right Column:** 4 Quick Architectural Badges:
    1. *Layered Monolith (Java 21)*
    2. *Relational Storage (MySQL 8)*
    3. *Enterprise Security (RBAC + BCrypt)*
    4. *Responsive UI (Bootstrap 5)*

### Speaker Notes
> "Respected Professor and committee members, good morning. Today we present CampusConnect, an end-to-end Enterprise Web Application developed for UCS553. CampusConnect digitizes, governs, and automates higher-education campus placements across students, visiting corporate recruiters, and the college placement office."

---

## Slide 2: Problem Statement & Industrial Motivation

### Slide Title
**Problem Statement & Institutional Context**

### Subtitle
*Overcoming data fragmentation and compliance risks in university placement operations*

### Slide Content (Bullet Points)
- **Legacy Process Bottlenecks:**
  - **Data Fragmentation:** Student records scattered across unlinked spreadsheets, Google Forms, and chat groups.
  - **Manual Eligibility Auditing:** Placement coordinators manually verify CGPA cutoffs, allowed branches, and active backlogs for hundreds of candidates per drive.
  - **Absence of Audit Trails:** Status updates (shortlisted, rejected, offered) lack immutable timestamped logs.
  - **Communication & Scheduling Chaos:** Interview slots and venue links fail to reach students reliably in real time.
  - **Data Privacy & Compliance Vulnerabilities:** Student marksheets, personal phone numbers, and resumes exposed without access boundaries.
- **Enterprise Necessity:**
  - Need for an automated, transactional, role-governed web portal enforcing strict compliance before applications are committed.

### Suggested Visuals & Layout
- **Comparative 2-Box Visual:**
  - **Left Box (Red Border - Legacy):** Manual spreadsheet chaos → Human error in CGPA cutoffs → Privacy leaks.
  - **Right Box (Green Border - CampusConnect):** Centralized Relational DB → Millisecond server-side eligibility check → Encrypted, role-isolated storage.

### Speaker Notes
> "In an institution managing hundreds of recruiters and thousands of students, spreadsheet-driven placement drives create severe risks. Human verification of multi-branch eligibility is slow and error-prone. CampusConnect solves this at the enterprise level with server-side business rule enforcement."

---

## Slide 3: Project Objectives & Functional Scope

### Slide Title
**Project Objectives & Enterprise Scope**

### Subtitle
*Defining explicit system boundaries and institutional deliverables*

### Slide Content (Bullet Points)
- **Primary Objectives:**
  - **Centralize Recruitment Lifecycle:** Single portal from student registration to final job offer release.
  - **Automate Compliance:** Dynamic, zero-error validation of company-specific academic criteria.
  - **Enforce Institutional Governance:** Mandatory TPO oversight over student profiles and company vacancies.
  - **Ensure Traceability:** Immutable audit logging for every stage transition and interview round.
  - **Deliver Real-Time Business Intelligence:** Automated aggregation of placement ratios, salary packages, and branch statistics.
- **System Scope Boundaries (Adhering to TRD):**
  - **In-Scope:** RBAC security, student mark verification, company approval, job eligibility engine, interview scheduling, notifications, and analytics.
  - **Explicitly Out-of-Scope:** External commercial job-board scraping, payment gateways, and third-party microservices (avoiding distributed transaction failure modes).

### Suggested Visuals & Layout
- **Scope Matrix Table / Icons:**
  - 5 Key Pillars displayed as structured cards: *Centralization*, *Eligibility Automation*, *TPO Governance*, *Audit Logging*, and *Institutional Analytics*.

### Speaker Notes
> "Our design strictly adheres to our Technical Requirements baseline. The goal was not to build a generic job portal, but a compliant institutional enterprise platform with strict two-way verification: unverified students cannot apply, and unapproved companies cannot post jobs."

---

## Slide 4: User Roles & System Workflows

### Slide Title
**Multi-Tenant Role Model & Operational Workflows**

### Subtitle
*Strict separation of concerns across the three enterprise actors*

### Slide Content (Bullet Points)
- **Student Actor:**
  - Completes academic profile (CGPA, active backlogs, passing batch, department).
  - Uploads verified PDF resume (stored securely via `FileStorageService`).
  - Discovers jobs with real-time "Eligible / Ineligible" status badges and diagnostic feedback.
  - Submits 1-click job applications and tracks multi-round interview schedules.
- **Recruiter Actor:**
  - Registers company profile and awaits administrative vetting.
  - Creates job openings with detailed criteria (min CGPA, allowed departments, max backlogs).
  - Reviews qualified candidates, inspects resumes, and updates applicant stages.
  - Slots online/offline interview rounds with date, time, and meeting URLs.
- **Placement Officer (TPO Administrator):**
  - Verifies student profile data against institutional marks transcripts.
  - Approves or rejects company registrations and job listings before campus broadcast.
  - Publishes priority announcements and monitors real-time college-wide placement KPIs.

### Suggested Visuals & Layout
- **Use-Case / Interaction Diagram:**
  - Flow showing `Student` → `Application` ← `Recruiter`, with `Placement Officer` acting as the central gatekeeper supervising both pipelines.

### Speaker Notes
> "CampusConnect implements three distinct user roles. The student and recruiter operate within their self-service portals, while the Placement Officer acts as the governing administrative authority ensuring data validity and recruitment transparency."

---

## Slide 5: Enterprise Layered Architecture

### Slide Title
**Enterprise Layered Monolithic Architecture**

### Subtitle
*High cohesion, low coupling, and unified transaction boundaries*

### Slide Content (Bullet Points)
- **Tiered Architectural Pattern:**
  - **Presentation Tier:** Server-Side Rendering (SSR) via Thymeleaf, HTML5, CSS3, and Bootstrap 5.
  - **Web / Controller Tier:** Spring MVC controllers handling HTTP routing, model binding, and Jakarta Validation.
  - **Security Filter Tier:** Spring Security filter chain executing session management, CSRF validation, and RBAC checks.
  - **Business Service Tier:** Transactional business logic (`@Transactional`), eligibility algorithms, and status transition guards.
  - **Persistence Tier:** Spring Data JPA repositories with Hibernate 6 ORM and HikariCP connection pooling.
  - **Database Tier:** MySQL 8.0 InnoDB engine providing full ACID guarantees and relational constraints.
- **Enterprise Architectural Justification:**
  - Chosen over microservices to eliminate distributed network latency and two-phase commit overhead, guaranteeing atomic database consistency for critical placement workflows.

### Suggested Visuals & Layout
- **Layered Architecture Diagram:**
```
+-----------------------------------------------------------------+
|  Presentation Tier: Thymeleaf Templates + Bootstrap 5 UI        |
+-----------------------------------------------------------------+
                                | HTTPS / Form POST / CSRF Tokens
+-----------------------------------------------------------------+
|  Security Tier: Spring Security (SecurityFilterChain, RBAC)     |
+-----------------------------------------------------------------+
                                | Authenticated Principal / Roles
+-----------------------------------------------------------------+
|  Web Controller Tier: Spring MVC (@Controller, @Valid DTOs)     |
+-----------------------------------------------------------------+
                                | Method Invocations
+-----------------------------------------------------------------+
|  Business Service Tier: @Service Layer + @Transactional Scopes  |
|  [Eligibility Engine | Application Service | Analytics Service] |
+-----------------------------------------------------------------+
                                | Spring Data JPA Repositories
+-----------------------------------------------------------------+
|  Persistence Tier: Hibernate 6 ORM + HikariCP Connection Pool   |
+-----------------------------------------------------------------+
                                | SQL Queries / ACID Transactions
+-----------------------------------------------------------------+
|  Database Storage: MySQL 8.0 Relational DB (InnoDB Engine)      |
+-----------------------------------------------------------------+
```

### Speaker Notes
> "From an enterprise perspective, architecture is about fitness for purpose. By choosing a clean, layered monolithic architecture on Spring Boot 3.4, we guarantee ACID transactions when moving an application from shortlisted to selected, while keeping code organized into clean domain packages."

---

## Slide 6: Technology Stack & Technical Specifications

### Slide Title
**Technology Stack & Enterprise Tooling**

### Subtitle
*Production-ready, type-safe enterprise Java ecosystem*

### Slide Content (Bullet Points)
- **Core Platform & Language:**
  - **Java 21 (LTS):** Modern language features, enhanced type safety, and robust memory management.
  - **Spring Boot 3.4.2:** Enterprise framework core providing auto-configuration and embedded Tomcat 10.
- **Enterprise Framework Components:**
  - **Spring MVC:** Model-View-Controller request routing and view resolution.
  - **Spring Security 6:** Delegating filter proxy, `BCryptPasswordEncoder`, and role-based route matchers.
  - **Spring Data JPA & Hibernate 6:** Object-Relational Mapping (ORM) and JPQL abstract queries.
  - **Jakarta Validation:** Declarative bean validation (`@NotNull`, `@DecimalMin`, `@NotBlank`).
- **Data & Storage:**
  - **MySQL 8.0 Community Server:** Primary relational database (InnoDB, UTF-8 unicode encoding).
  - **H2 In-Memory Database:** Test-scope database used for zero-side-effect automated test suites.
- **Build & DevOps:**
  - **Apache Maven (via `./mvnw` wrapper):** Deterministic build lifecycles and dependency management.
  - **Testing Frameworks:** JUnit 5 (Jupiter), Mockito, and Spring Boot Test (`MockMvc`).

### Suggested Visuals & Layout
- A 4-quadrant tech stack grid with official technology badges:
  - *Quadrant 1: Language & Core (Java 21, Spring Boot 3.4)*
  - *Quadrant 2: Persistence & DB (Hibernate 6, MySQL 8, H2)*
  - *Quadrant 3: Security & Web (Spring Security, Thymeleaf, Bootstrap 5)*
  - *Quadrant 4: Quality & Build (Maven Wrapper, JUnit 5, Mockito)*

### Speaker Notes
> "We selected Java 21 LTS and Spring Boot 3.4.2. Notice our dependency configuration: we use the Maven wrapper for cross-platform reproducible builds, externalized `.env` environment variables for database credentials, and H2 test profiles to run automated regression testing without dirtying the production database."

---

## Slide 7: Relational Database Design & Data Integrity

### Slide Title
**Relational Data Model & Integrity Constraints**

### Subtitle
*Third Normal Form (3NF) relational design with referential integrity*

### Slide Content (Bullet Points)
- **Entity Model Organization:**
  - `User`: Central authentication table (`id`, `email`, `password`, `role`, `status`).
  - `StudentProfile`: 1:1 extension of `User` (`cgpa`, `department`, `active_backlogs`, `graduation_year`, `resume_path`, `verification_status`).
  - `Company`: 1:1 extension of `User` (`company_name`, `website`, `approval_status`).
  - `Job`: 1:N from `Company` (`title`, `description`, `salary_package`, `deadline`, `status`).
  - `JobEligibilityCriteria`: 1:1 with `Job` (`min_cgpa`, `max_backlogs`, `graduation_year`, `eligible_branches`).
  - `Application`: N:1 with `StudentProfile` and `Job` (`status`, `applied_at`, `resume_path`).
  - `Interview`: N:1 with `Application` (`round_name`, `scheduled_at`, `mode`, `meeting_link`, `status`).
  - `ApplicationStatusHistory`: N:1 audit trail table tracking all status changes with reviewer remarks.
- **Relational Integrity Guarantees:**
  - **Composite Unique Constraint:** `UNIQUE (job_id, student_id)` prevents accidental or malicious duplicate applications.
  - **Foreign Key Constraints:** Cascade options configured to maintain consistency and prevent orphan records.

### Suggested Visuals & Layout
- **Entity-Relationship (ER) Diagram:**
```
+--------------------+        1:1       +--------------------------+
|      users         +------------------+     student_profiles     |
| (id, email, role)  |                  | (cgpa, dept, verif_stat) |
+---------+----------+                  +------------+-------------+
          | 1:1                                      |
          v                                          | 1:N
+--------------------+                  +------------v-------------+
|     companies      |                  |       applications       |
| (name, apprv_stat) |                  | (status, resume, date)   |
+---------+----------+                  +-----+--------------+-----+
          | 1:N                               | 1:N          | 1:N
          v                                   v              v
+--------------------+ 1:1 +----------------+ +------------+ +-----+----+
|       jobs         +-----+ job_criteria   | | interviews | | app_hist |
| (title, package)   |     | (cgpa, branch) | | (round)    | | (audit)  |
+--------------------+     +----------------+ +------------+ +----------+
```

### Speaker Notes
> "Our database schema is normalized to 3NF. We strictly decouple credentials in the `users` table from domain profiles. Notice the composite unique key on `(job_id, student_id)`—even if a student clicks 'Apply' simultaneously in multiple browser tabs, the database enforces single-application integrity."

---

## Slide 8: Core Engine: Automated Eligibility Evaluation

### Slide Title
**Core Business Engine: Automated Eligibility Evaluation**

### Subtitle
*Dynamic, multi-parameter rule verification removing manual human error*

### Slide Content (Bullet Points)
- **The Eligibility Problem in Placements:**
  - Companies specify complex multi-branch criteria, CGPA floors, and zero-backlog rules.
  - Manual verification leads to ineligible students sneaking into drives or administrative delays.
- **CampusConnect Automated Rule Pipeline (`EligibilityService`):**
  - **Gate 1 – Verification Check:** Student's `verification_status` must equal `VERIFIED` by TPO.
  - **Gate 2 – Batch Matching:** Student's `graduation_year` must match the job's target batch.
  - **Gate 3 – Department Eligibility:** Student's `department` must be contained within `eligible_branches`.
  - **Gate 4 – Academic Cutoff:** Student's `cgpa` >= `min_cgpa` threshold.
  - **Gate 5 – Backlog Ceiling:** Student's `active_backlogs` <= `max_backlogs` limit.
  - **Gate 6 – Application Window:** Submission timestamp <= job `deadline`.
- **User Experience & Defensive Programming:**
  - **Pre-application Check:** Displays color-coded diagnostic badges (e.g., *"CGPA 7.10 is below required 7.50"*).
  - **Server-Side Re-validation:** Even if UI checks are bypassed, `ApplicationService` re-runs `EligibilityService.evaluate()` prior to database commit.

### Suggested Visuals & Layout
- **Decision Flowchart:**
```
[Student clicks 'View Job']
             |
             v
   [Fetch Profile & Job Criteria]
             |
             +--> Check 1: Is Student Profile VERIFIED? ---> [NO] -> Ineligible Badge
             +--> Check 2: Does Graduation Batch Match? ---> [NO] -> Ineligible Badge
             +--> Check 3: Is Department in Allowed List? -> [NO] -> Ineligible Badge
             +--> Check 4: Is CGPA >= Minimum Cutoff? -----> [NO] -> Ineligible Badge
             +--> Check 5: Are Active Backlogs <= Max? ----> [NO] -> Ineligible Badge
             |
            [ALL CHECKS PASSED]
             |
             v
[Enable 'Apply Now' Button & 1-Click Application Commit]
```

### Speaker Notes
> "The automated eligibility engine in `EligibilityService.java` is one of our strongest business logic implementations. It runs a 6-gate diagnostic check. If a student is ineligible, it does not just block them; it returns a human-readable list explaining exactly which criteria failed."

---

## Slide 9: Finite State Machine & Governance Lifecycles

### Slide Title
**Application State Machine & Multi-Stage Governance**

### Subtitle
*Guaranteed lifecycle progression with complete audit traceability*

### Slide Content (Bullet Points)
- **Finite State Machine Architecture (`ApplicationStatus`):**
  - Linear, validated states: `APPLIED` → `SHORTLISTED` → `INTERVIEW_SCHEDULED` → `SELECTED` / `REJECTED`.
  - **Illegal Transition Defense:** A recruiter cannot jump from `APPLIED` directly to `SELECTED` without completing review and interview stages.
  - Invalid state attempts throw custom `ApplicationStatusTransitionException`.
- **Immutable Historical Audit Trail (`ApplicationStatusHistory`):**
  - Every transition creates an immutable historical row storing:
    1. Previous Status
    2. New Status
    3. Updating Recruiter / Officer ID
    4. Exact Timestamp
    5. Recruiter Feedback / Remarks
- **Two-Tier Corporate Gatekeeping:**
  - Company registers → TPO validates authenticity → Company Approved.
  - Recruiter drafts Job → TPO audits role, package & criteria → Job Published campus-wide.

### Suggested Visuals & Layout
- **State Transition Diagram:**
```
               +---------------------------+
               |     Student Submits       |
               |     (Status: APPLIED)     |
               +-------------+-------------+
                             | Recruiter Review
              +--------------+--------------+
              |                             |
              v                             v
+--------------------------+   +--------------------------+
|       SHORTLISTED        |   |         REJECTED         |
+-------------+------------+   |  (Terminal Audit State)  |
              |                +--------------------------+
              | Schedule Round              ^
              v                             |
+--------------------------+                |
|   INTERVIEW_SCHEDULED    +----------------+ (Failed Round)
+-------------+------------+
              | Final Round Cleared
              v
+--------------------------+
|         SELECTED         |
|   (Official Job Offer)   |
+--------------------------+
```

### Speaker Notes
> "In enterprise systems, business data must behave like a formal state machine. Applications cannot skip stages. Furthermore, every single change writes an audit record to `ApplicationStatusHistory`. This guarantees accountability if a student or recruiter ever queries a decision."

---

## Slide 10: Enterprise Security & Non-Functional Architecture

### Slide Title
**Enterprise Security, Transactions & Defensive Design**

### Subtitle
*Adhering to OWASP Top 10 web application security principles*

### Slide Content (Bullet Points)
- **Authentication & Password Protection:**
  - Passwords hashed with `BCryptPasswordEncoder` using adaptive cryptographic work factor (10 rounds). Plaintext passwords never stored.
  - Form login authenticated via Spring Security `DaoAuthenticationProvider` and custom `CustomUserDetailsService`.
- **Cross-Site Request Forgery (CSRF) Defense:**
  - Synchronizer Token Pattern enforced on all mutating HTTP requests (POST / PUT / DELETE) via Thymeleaf CSRF tokens.
- **Injection & XSS Protection:**
  - **SQL Injection:** Eliminated by Spring Data JPA parameterized queries and Criteria APIs.
  - **Cross-Site Scripting (XSS):** Thymeleaf automatically escapes all dynamic HTML expressions (`th:text`).
- **Authorization & Insecure Direct Object Reference (IDOR) Defense:**
  - Controller endpoints enforce tenant ownership: recruiters can only view and update candidates applied to their own company's jobs.
- **Secure File Storage (`FileStorageService`):**
  - PDF/DOC/DOCX extension whitelisting, 5MB file-size boundary, directory traversal prevention (`..` sanitization), and randomized UUID filenames.
- **Transactional Consistency:**
  - Service methods decorated with `@Transactional` ensure atomicity (e.g., job submission and notification triggers commit or rollback together).

### Suggested Visuals & Layout
- **Security Defense Grid (Icons & Checkmarks):**
  - *BCrypt 10-Round Hashing* | *CSRF Token Synchronizer*
  - *Parameterized JPA Queries* | *UUID File Storage*
  - *IDOR Ownership Validation* | *ACID Transaction Boundaries*

### Speaker Notes
> "Security is a core grading criterion for Enterprise Web Applications. We implemented OWASP Top 10 defenses: BCrypt password salting, CSRF tokens on all POST requests, parameterized queries preventing SQL injection, and strict session-ownership checks preventing recruiters from accessing other companies' candidate data."

---

## Slide 11: Java & Enterprise Concepts Applied

### Slide Title
**Java Concepts & Enterprise Design Patterns**

### Subtitle
*Demonstrating core academic Java principles within the enterprise solution*

### Slide Content (Bullet Points)
- **Advanced Java 21 & Language Features:**
  - **Modern Collections Framework:** Extensive use of `List`, `Set`, `Map`, and `stream()` pipelines for filtering and transformation.
  - **Regular Expressions (`java.util.regex`):** Custom `SalaryPackageParser` engine extracting compensation numbers from diverse textual inputs (e.g., "12-15 LPA", "50,000/month", "8.5 Lakhs").
  - **Modern Exception Handling:** Custom domain hierarchy (`ApplicationStatusTransitionException`, `ResourceNotFoundException`) mapped to HTTP error codes.
  - **Type-Safe Enumerations:** Strongly-typed domain states (`Role`, `ApplicationStatus`, `VerificationStatus`, `InterviewMode`, `NotificationType`).
- **Enterprise Design Patterns Applied:**
  - **Model-View-Controller (MVC):** Separation between Thymeleaf views, Spring controllers, and JPA domain entities.
  - **Data Access Object (DAO) / Repository Pattern:** Interfaces extending `JpaRepository` providing clean abstraction over SQL.
  - **Dependency Injection & Inversion of Control (IoC):** Constructor-based dependency injection throughout all `@Service` and `@Controller` classes.
  - **Cross-Cutting Concerns via `@ControllerAdvice`:** `NotificationHeaderAdvice` automatically injects unread notification counts into every page header without repeating controller code.

### Suggested Visuals & Layout
- **Pattern Mapping Table:**
  - *Pattern / Concept* vs. *Concrete CampusConnect Implementation Class*
  - e.g., IoC → `@Service` Constructors | RegEx → `SalaryPackageParser` | AOP/Advice → `NotificationHeaderAdvice` | Strategy → `EligibilityService`.

### Speaker Notes
> "This slide maps our theoretical Java and EWA curriculum directly to our implementation. We utilized constructor injection for IoC, regular expressions in our custom `SalaryPackageParser`, type-safe Enums for state modeling, and `@ControllerAdvice` to handle cross-cutting notification badges across the UI."

---

## Slide 12: Real-Time Analytics & Institutional Reporting

### Slide Title
**Enterprise Analytics & Reporting Module**

### Subtitle
*Real-time business intelligence for academic administration and accreditation*

### Slide Content (Bullet Points)
- **The Institutional Analytics Need:**
  - College accreditation bodies (NBA, NAAC, NIRF) require continuous statistics on student placement rates and salary packages.
- **Automated Aggregation Engine (`PlacementAnalyticsService`):**
  - **Real-Time KPI Computation:** Total students registered, verified, placed, and overall placement percentage.
  - **Statistical Salary Metrics:** Automatically parses and calculates Highest CTC, Average CTC, and Median CTC.
  - **Department-Wise Breakdown:** Dynamic computation of eligible candidates, placed counts, placement rate, and average package for CSE, IT, ECE, MECH, etc.
  - **Top Recruiting Partners:** Aggregates company hiring volumes and active placement offers.
- **Transparency for Students:**
  - Read-only analytics dashboard available to verified students to help them benchmark compensation trends across departments.

### Suggested Visuals & Layout
- **Mock Analytics Dashboard Visual (KPI Metric Cards + Table):**
```
+-----------------------------------------------------------------+
|  OVERALL PLACEMENT RATE: 84.6%   |  TOTAL PLACED: 381 / 450     |
|  HIGHEST PACKAGE: 24.50 LPA      |  AVERAGE CTC: 7.80 LPA       |
+-----------------------------------------------------------------+

Department-Wise Placement Distribution Table:
+----------------+---------------+------------+--------------+------------+
| Department     | Eligible      | Placed     | Placement %  | Avg CTC    |
+----------------+---------------+------------+--------------+------------+
| CSE            | 160           | 148        | 92.5%        | 9.2 LPA    |
| IT             | 110           | 101        | 91.8%        | 8.4 LPA    |
| ECE            | 95            | 74         | 77.9%        | 6.8 LPA    |
| Mechanical     | 85            | 58         | 68.2%        | 5.9 LPA    |
+----------------+---------------+------------+--------------+------------+
```

### Speaker Notes
> "In enterprise software, data is useless without reporting. Our analytics engine aggregates placement figures in real time. The custom `SalaryPackageParser` reads varied package strings and computes institutional metrics like average CTC and branch placement percentages, ready for institutional reporting."

---

## Slide 13: Results, Verification & Live Demo Guide

### Slide Title
**Quality Assurance, Verification & UI Showcase**

### Subtitle
*Demonstrating functional completeness, test suites, and operational screens*

### Slide Content (Bullet Points)
- **Quality Assurance & Automated Verification:**
  - **Unit Testing (JUnit 5 & Mockito):** Complete service-layer isolation with mocked repositories.
  - **Web Integration Tests (`MockMvc`):** Validating HTTP status codes, model attributes, form validations, and CSRF token defenses.
  - **Zero Side-Effect Test Environment:** Automated test profile (`application-test.yml`) running on an isolated in-memory H2 database.
  - **Automated Maven Pipeline:** 100% build and test pass rate via `./mvnw clean test`.
- **Key Application Screens to Showcase in Demo / Presentation:**
  1. **Student Job Details (`/student/jobs/{id}`):** Displays dynamic eligibility checklist with green checks or red failure criteria.
  2. **Recruiter Applicant Review (`/recruiter/jobs/{id}/applicants`):** Displays shortlisted candidates, resume links, and status change controls.
  3. **TPO Student Verification (`/officer/students`):** Roster table where the placement officer audits student marks and grants verified status.
  4. **Placement Analytics Dashboard (`/officer/analytics`):** Real-time KPI counter cards and department placement breakdown.

### Suggested Visuals & Layout
- **4-Screenshot Grid Frame:**
  - *Top-Left:* Student Eligibility Breakdown Screen
  - *Top-Right:* Recruiter Applicant Tracking & Interview Scheduler
  - *Bottom-Left:* Placement Officer Student Verification Queue
  - *Bottom-Right:* Institutional Analytics KPI Cards & Table

### Speaker Notes
> "Here are the live functional screens of the application. Notice the clean separation in UI: the student sees actionable eligibility tags; the recruiter gets an applicant tracking workbench; and the placement officer has full administrative controls. All modules are backed by automated JUnit tests."

---

## Slide 14: Conclusion & Future Scope

### Slide Title
**Conclusion, Business Impact & Future Scope**

### Subtitle
*Delivering an auditable, enterprise-grade placement management platform*

### Slide Content (Bullet Points)
- **Project Summary & Impact:**
  - **Unified Ecosystem:** Successfully integrated Students, Recruiters, and Placement Officers into a single governed platform.
  - **Human Error Eliminated:** Automated eligibility engine removes manual spreadsheet filtering.
  - **Enterprise Trust & Governance:** Mandatory TPO verification and immutable audit logs ensure complete institutional transparency.
  - **Production-Ready Quality:** Built on Java 21, Spring Boot 3, and MySQL with robust OWASP security and comprehensive tests.
- **Future Architectural Roadmap:**
  - **Campus Single Sign-On (SSO):** Integration with university LDAP / OAuth2 identity providers.
  - **Automated Messaging Gateways:** SMS and WhatsApp notification dispatchers for urgent interview slots.
  - **AI/ML Resume Parser:** Automated extraction of candidate skills and project keywords to match company requirements.
- **Session Wrap-Up:**
  - **Open for Questions & Live System Demonstration**

### Suggested Visuals & Layout
- Centered summary card with GitHub / Project repository link, contact details, and a bold banner:  
  **"Thank you! We now invite your questions and welcome the live demonstration."**

### Speaker Notes
> "In conclusion, CampusConnect satisfies all technical, architectural, and security requirements of an Enterprise Web Application. It replaces scattered spreadsheets with an automated, auditable, and secure platform. Thank you for your time. We are now eager to take your questions and demonstrate the live running application."

---

## Summary Checklist for Creating Your Presentation Slides

| Slide # | Slide Subject | Recommended Diagram / Screenshot |
|:---:|:---|:---|
| **01** | Title & Project Details | University Logo + Tech Stack Badges |
| **02** | Problem Statement & Context | Comparison: Manual Chaos vs. Enterprise System |
| **03** | Project Objectives & Scope | 5 Scope Pillar Cards (Centralize, Automate, Govern, Audit, Analyze) |
| **04** | User Roles & Workflows | Role Use-Case / Interaction Diagram |
| **05** | Enterprise Layered Architecture | 6-Tier Architecture Diagram (Thymeleaf → Spring MVC → Service → JPA → MySQL) |
| **06** | Technology Stack & Tools | 4-Quadrant Tech Stack Grid (Java 21, Spring Boot 3, MySQL 8, Maven) |
| **07** | Database Design & ER Diagram | Entity-Relationship (ER) Diagram showing 3NF entities |
| **08** | Automated Eligibility Engine | 6-Step Decision Flowchart for Student Criteria |
| **09** | Application State Machine | 5-State Transition Flow (`APPLIED` → `SELECTED`/`REJECTED`) |
| **10** | Enterprise Security & OWASP | Security Checklist Grid (BCrypt, CSRF, IDOR, SQLi, XSS) |
| **11** | Java & Enterprise Concepts | Mapping Table: Curriculum Concepts → Concrete Project Classes |
| **12** | Analytics & Reporting Module | KPI Stat Cards + Department-Wise Performance Table |
| **13** | Verification, QA & UI Showcase | 4 Application Screenshots (Student, Recruiter, TPO, Analytics) |
| **14** | Conclusion & Future Roadmap | Summary Highlights + Q&A / Live Demo Invitation |
