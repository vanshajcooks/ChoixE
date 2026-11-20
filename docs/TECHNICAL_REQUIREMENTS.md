# CampusConnect

## Technical Requirements Document (TRD)

**Document status:** Baseline / Requirements Frozen\
**Version:** 1.0\
**Project type:** Enterprise Web Application\
**Primary language:** Java 21\
**Prepared for:** CampusConnect development team and AI coding
assistants (Antigravity / OpenCode)

------------------------------------------------------------------------

## Document Control

  Field           Value
  --------------- ----------------------------------------------------
  Project name    CampusConnect
  Document        Technical Requirements Document
  Version         1.0
  Status          Approved baseline
  Primary users   Students, Recruiters/Companies, Placement Officers
  Architecture    Layered monolithic web application
  Backend         Java 21, Spring Boot 3.x, Spring MVC
  Frontend        Thymeleaf, HTML, CSS, Bootstrap 5
  Database        MySQL 8
  Persistence     Spring Data JPA, Hibernate
  Security        Spring Security
  Build           Maven

## 1. Purpose and Scope

CampusConnect is a web-based Campus Placement Management Portal intended
to centralize placement activities between students,
companies/recruiters, and the college placement department.

The system will support student registration and verification, company
registration and approval, job posting and company-defined eligibility
criteria, applications, interview scheduling, recruitment result
updates, placement analytics, announcements, and notifications.

This document defines the agreed product scope, functional requirements,
technical architecture, data requirements, security expectations, and
acceptance criteria. It is the baseline reference for implementation.
Developers and AI coding assistants must follow this document and must
not add unrelated features or expand the product scope without explicit
approval from the project team.

### 1.1 Objectives

1.  Centralize campus recruitment activities in one application.
2.  Allow students to create and maintain academic profiles and apply
    for eligible opportunities.
3.  Allow companies to register, publish jobs, define eligibility
    criteria, review applicants, schedule interviews, and update
    recruitment outcomes.
4.  Allow Placement Officers to verify students, approve companies,
    monitor recruitment, publish announcements, and view placement
    analytics.
5.  Provide students with visibility into placement statistics,
    interview schedules, announcements, and application progress.

### 1.2 In Scope

-   Role-based authentication and authorization.
-   Student self-registration and Placement Officer verification.
-   Company self-registration and Placement Officer approval.
-   Student academic profile and resume upload.
-   Company profiles and job postings.
-   Per-job eligibility criteria defined by the company.
-   Backend eligibility evaluation.
-   Student applications and application status tracking.
-   Recruiter applicant review and recruitment result updates.
-   Interview scheduling and student visibility.
-   College placement analytics visible to students.
-   Announcements and in-application notifications.
-   Placement Officer monitoring and management dashboards.

### 1.3 Out of Scope

The following are not part of the agreed scope:

-   AI/ML-based resume screening or candidate recommendations.
-   Payment processing or financial transactions.
-   A separate microservices architecture.
-   Native Android/iOS applications.
-   External job-board integrations.
-   Automated third-party background verification.
-   Complex email/SMS gateway integrations unless separately approved.
-   A separate super-administrator role beyond the Placement Officer
    role.

## 2. Users, Roles and Permissions

The application has three primary roles.

### 2.1 Student

A student can: - Register an account and sign in. - Create and update
their own academic profile. - Upload and replace their resume. - View
approved and published job opportunities. - View eligibility results and
unmet criteria for a job. - Apply to a job when eligible and when
applications are open. - View their own applications and status
history. - View interview schedules associated with their
applications. - View college placement analytics. - View announcements
and their notifications.

A student must not: - Verify their own account. - Modify another
student's profile or resume. - Create or modify company job postings. -
Change application review or selection outcomes. - View private
applicant information belonging to other students.

### 2.2 Recruiter / Company

A company representative can: - Register a company account. - Maintain
the company's own profile. - Create and manage job postings for that
company. - Define eligibility criteria separately for each job. - Submit
job postings for Placement Officer approval before publication. - View
applicants for the company's own jobs. - Review applications and update
recruitment statuses. - Schedule interviews for applicants to the
company's jobs. - Update interview outcomes and final recruitment
results.

A recruiter must not: - Approve their own company registration. - View
or modify another company's jobs or applicants. - Modify student
academic profiles or verification decisions. - Access Placement
Officer-only administration functions.

### 2.3 Placement Officer

A Placement Officer can: - View and verify student
registrations/profiles. - Approve or reject company registrations. -
Review and approve/reject job postings before publication. - Monitor
jobs, applications, interviews, and recruitment outcomes across
companies. - View placement analytics. - Create, publish, edit, and
deactivate announcements. - Monitor recruitment activity and student
placement progress.

The Placement Officer is the administrative role in the agreed project
scope. A separate super-admin role is not required.

### 2.4 Permission Matrix

  ------------------------------------------------------------------------
  Capability                 Student          Recruiter  Placement Officer
  --------------- ------------------ ------------------ ------------------
  Self-register                  Yes                Yes     No requirement

  Manage own                     Yes    Company profile     Administrative
  profile                                          only          oversight

  Verify student                  No                 No                Yes

  Approve company                 No                 No                Yes

  Create job                      No                Yes  May review/manage
  posting                                               

  Approve job                     No                 No                Yes
  posting                                               

  Define job                      No      Yes, own jobs             Review
  eligibility                                           

  View eligible                  Yes           Own jobs                All
  jobs                                                  

  Apply for job                  Yes                 No                 No

  View applicants   Own applications Own company's jobs                All
                                only                    

  Update                          No Yes, own company's            Monitor
  recruitment                                      jobs 
  status                                                

  Schedule                        No Yes, own company's            Monitor
  interviews                                 applicants 

  View placement                 Yes    Not required in                Yes
  analytics                                    baseline 

  Publish                         No                 No                Yes
  announcements                                         

  View                           Yes                Yes                Yes
  announcements                                         
  ------------------------------------------------------------------------

## 3. Functional Requirements

Each requirement has a stable identifier. The implementation and testing
process should refer to these IDs.

### 3.1 Authentication and Account Management

-   **FR-AUTH-01:** The system shall provide registration and login for
    Students and Recruiters.
-   **FR-AUTH-02:** The system shall authenticate users securely using
    Spring Security.
-   **FR-AUTH-03:** The system shall associate each account with an
    authorized role.
-   **FR-AUTH-04:** The system shall restrict protected pages and
    actions according to role.
-   **FR-AUTH-05:** The system shall prevent disabled, rejected, or
    unverified accounts from accessing restricted workflows.
-   **FR-AUTH-06:** The system shall provide logout.
-   **FR-AUTH-07:** Passwords shall be stored as secure password hashes
    and never as plaintext.

### 3.2 Student Registration and Verification

-   **FR-STU-01:** A student shall be able to self-register.
-   **FR-STU-02:** A newly registered student shall have a pending
    verification state.
-   **FR-STU-03:** A Placement Officer shall be able to review student
    details and verify or reject the profile.
-   **FR-STU-04:** The student shall be able to view their verification
    state.
-   **FR-STU-05:** Only verified students shall be allowed to apply for
    jobs.
-   **FR-STU-06:** Students shall be able to maintain academic
    information including name, email, roll/registration number,
    department/branch, graduation year, CGPA, and backlog information.
-   **FR-STU-07:** Students shall be able to update permitted profile
    fields. Changes to fields used for eligibility may require
    re-verification or be flagged for Placement Officer review.
-   **FR-STU-08:** Students shall only access and modify their own
    profile.

### 3.3 Resume Management

-   **FR-RES-01:** A student shall be able to upload a resume.
-   **FR-RES-02:** A student shall be able to replace their own resume.
-   **FR-RES-03:** The system shall validate permitted file types and
    maximum file size.
-   **FR-RES-04:** Resume files shall be stored outside publicly
    executable web content, with access controlled by the application.
-   **FR-RES-05:** Recruiters shall only be able to access resumes
    belonging to applicants for their own company's jobs, subject to the
    application workflow.
-   **FR-RES-06:** Placement Officers shall be able to access resumes
    where required for placement administration.

### 3.4 Company Registration and Approval

-   **FR-COMP-01:** A company representative shall be able to register a
    company account.
-   **FR-COMP-02:** A newly registered company shall have a pending
    approval state.
-   **FR-COMP-03:** A Placement Officer shall be able to approve or
    reject a company registration.
-   **FR-COMP-04:** Only approved companies shall be allowed to publish
    job opportunities.
-   **FR-COMP-05:** A company shall be able to maintain its own profile.
-   **FR-COMP-06:** Recruiters shall only manage their own company's
    information and recruitment activity.

### 3.5 Job Posting and Approval

-   **FR-JOB-01:** An approved company shall be able to create job
    postings.
-   **FR-JOB-02:** A job posting shall include, at minimum, job title,
    description, role/type, location or work mode where applicable,
    application opening and closing dates, and eligibility criteria.
-   **FR-JOB-03:** A company shall be able to edit its own job postings
    while permitted by the posting state.
-   **FR-JOB-04:** A job posting shall be submitted for Placement
    Officer review before becoming visible as an approved opportunity.
-   **FR-JOB-05:** A Placement Officer shall be able to approve or
    reject a submitted job posting.
-   **FR-JOB-06:** Students shall only see approved and published jobs.
-   **FR-JOB-07:** The system shall enforce application opening and
    closing dates.
-   **FR-JOB-08:** The system shall preserve the company ownership of
    each job posting.

### 3.6 Job-Specific Eligibility Engine

-   **FR-ELIG-01:** Each job shall have its own eligibility criteria
    configured by the company.
-   **FR-ELIG-02:** Supported baseline criteria shall include minimum
    CGPA, eligible branches, maximum active backlogs, and graduation
    year.
-   **FR-ELIG-03:** The system shall evaluate eligibility on the server
    side using the student's verified academic profile and the job's
    criteria.
-   **FR-ELIG-04:** The system shall display an eligibility result to
    the student.
-   **FR-ELIG-05:** Where possible, the system shall display which
    criteria are not satisfied.
-   **FR-ELIG-06:** Only eligible students shall be allowed to submit an
    application.
-   **FR-ELIG-07:** The system shall revalidate eligibility at
    application submission time; a frontend-only check is insufficient.
-   **FR-ELIG-08:** The system shall handle missing or unverified
    academic data without incorrectly marking a student eligible.
-   **FR-ELIG-09:** Eligibility criteria shall be associated with the
    relevant job and shall not be treated as one global company-wide
    rule.
-   **FR-ELIG-10:** Changes to criteria after applications have begun
    shall be controlled and auditable. Existing application eligibility
    decisions should not be silently rewritten.

### 3.7 Student Applications

-   **FR-APP-01:** A verified and eligible student shall be able to
    apply to an open job.
-   **FR-APP-02:** The system shall prevent duplicate applications by
    the same student to the same job.
-   **FR-APP-03:** The system shall record application submission date
    and time.
-   **FR-APP-04:** A student shall be able to view their own
    applications.
-   **FR-APP-05:** A student shall be able to view the current status of
    each application.
-   **FR-APP-06:** The system shall maintain application status history.
-   **FR-APP-07:** Recruiters shall be able to view applications only
    for jobs owned by their company.
-   **FR-APP-08:** The system shall enforce job closing dates and any
    configured application state restrictions.

### 3.8 Recruitment Review and Results

-   **FR-REC-01:** Recruiters shall be able to review applicants for
    their own company's jobs.
-   **FR-REC-02:** Recruiters shall be able to update recruitment
    status.
-   **FR-REC-03:** The baseline status lifecycle shall support: APPLIED,
    UNDER_REVIEW, SHORTLISTED, INTERVIEW_SCHEDULED, SELECTED, REJECTED,
    NOT_SELECTED, and WITHDRAWN.
-   **FR-REC-04:** The system shall record status changes with actor and
    timestamp.
-   **FR-REC-05:** Recruiters shall be able to update final results for
    their own recruitment process.
-   **FR-REC-06:** Placement Officers shall be able to monitor
    recruitment results across companies but recruiter updates remain
    the source of those results.
-   **FR-REC-07:** Students shall be able to view the status of their
    own applications and released outcomes.
-   **FR-REC-08:** Invalid status transitions shall be rejected by the
    backend.

### 3.9 Interview Scheduling

-   **FR-INT-01:** Recruiters shall be able to schedule interviews for
    applicants to their own company's jobs.
-   **FR-INT-02:** Interview records shall include date, time,
    mode/location, round or interview type, and relevant instructions.
-   **FR-INT-03:** Students shall be able to view interview schedules
    associated with their applications.
-   **FR-INT-04:** The system shall notify affected students when an
    interview is scheduled or changed.
-   **FR-INT-05:** Recruiters shall be able to update interview details
    where authorized.
-   **FR-INT-06:** The system shall retain interview history or the
    latest schedule state so changes can be communicated accurately.

### 3.10 Placement Analytics

-   **FR-AN-01:** The system shall provide a college-level placement
    analytics dashboard.
-   **FR-AN-02:** Students shall be able to view the published college
    placement analytics.
-   **FR-AN-03:** Placement Officers shall be able to view analytics
    across recruitment activity.
-   **FR-AN-04:** Analytics may include total registered students,
    verified students, placed students, placement percentage,
    company-wise outcomes, and package statistics where data is
    available.
-   **FR-AN-05:** The system shall calculate statistics from recorded
    application and recruitment data rather than hard-coded values.
-   **FR-AN-06:** The system shall avoid exposing private student
    records through aggregate analytics.
-   **FR-AN-07:** The calculation definitions for placed count and
    placement percentage shall be documented and consistently applied.

### 3.11 Announcements and Notifications

-   **FR-NOT-01:** Placement Officers shall be able to create, edit,
    publish, and deactivate announcements.
-   **FR-NOT-02:** Students and Recruiters shall be able to view
    announcements relevant to them.
-   **FR-NOT-03:** The system shall generate in-application
    notifications for relevant events, including application status
    changes and interview scheduling or updates.
-   **FR-NOT-04:** Users shall be able to view their notifications.
-   **FR-NOT-05:** Notifications shall identify the event, creation
    time, and relevant application/job where appropriate.
-   **FR-NOT-06:** The system shall support read/unread notification
    state.
-   **FR-NOT-07:** Email/SMS delivery is not required in the baseline
    scope.

## 4. Business Rules

1.  Students self-register; their accounts require Placement Officer
    verification before applying.
2.  Companies self-register; their accounts require Placement Officer
    approval before creating publishable opportunities.
3.  Each job posting owns its eligibility criteria. Criteria must not be
    assumed to be global to a company.
4.  Eligibility is evaluated on the backend against verified student
    data.
5.  A student cannot apply more than once to the same job.
6.  A student cannot apply when the job is not approved/published, is
    not open, or the student is ineligible.
7.  Recruiters can access and update recruitment data only for their own
    company's jobs.
8.  Recruiters update recruitment outcomes; Placement Officers monitor
    outcomes across the college.
9.  Students can view their own application progress and interview
    schedules.
10. Students can view college-level placement analytics, but not private
    records of other students.
11. Status transitions must be validated and recorded with actor and
    timestamp.
12. Placement analytics must be derived from actual recorded data.
13. The Placement Officer is the administrative role; a separate
    super-admin is not required.

## 5. Non-Functional Requirements

### 5.1 Security

-   Passwords must be hashed using a password encoder supported by
    Spring Security.
-   Authorization must be enforced on the server for every protected
    operation.
-   Prevent insecure direct object references: users must not access
    another user's records by changing an identifier.
-   Validate and sanitize user input.
-   Use CSRF protection for browser-based form submissions.
-   Restrict resume file type and size; do not trust the supplied
    filename or MIME type alone.
-   Store uploaded files outside executable/public application
    directories.
-   Keep secrets and database credentials outside source control.
-   Use parameterized queries / ORM-safe access.
-   Do not log passwords, session tokens, or sensitive resume contents.

### 5.2 Reliability and Data Integrity

-   Use database constraints for uniqueness and referential integrity.
-   Use transactions for multi-step operations that must succeed or fail
    together.
-   Prevent duplicate applications through both application logic and a
    database uniqueness constraint.
-   Store timestamps consistently and display them in the configured
    application timezone.
-   Provide understandable error pages/messages without exposing stack
    traces to end users.

### 5.3 Performance

-   Common dashboard and listing pages should use pagination where
    result sets may grow.
-   Avoid unnecessary repeated database queries and N+1 loading
    patterns.
-   Add indexes for frequently filtered fields such as job status,
    company, application status, student, and relevant foreign keys.
-   The application is intended for a college-scale demonstration
    deployment; formal high-scale performance targets are not defined in
    this baseline.

### 5.4 Usability and Accessibility

-   Provide role-appropriate dashboards and navigation.
-   Forms must show validation errors clearly.
-   The UI must be responsive for common desktop and mobile browser
    sizes.
-   Use consistent labels, status indicators, and date/time formats.
-   Do not communicate status using color alone.

### 5.5 Maintainability and Testability

-   Follow layered architecture and keep business logic out of
    controllers.
-   Use constructor-based dependency injection.
-   Keep controllers, services, repositories, entities, DTOs, and
    configuration organized.
-   Add unit tests for eligibility and status transition logic.
-   Add integration tests for key persistence and security workflows.
-   Use meaningful names and avoid unnecessary abstraction.

## 6. Technical Architecture

### 6.1 Architecture Style

Use a layered monolithic Spring Boot application. The application is
deployed as one unit and organized into clear modules/packages.
Microservices are explicitly not required.

### 6.2 Request Flow

1.  Browser sends an HTTP request.
2.  Spring Security authenticates the session and checks authorization.
3.  Spring MVC routes the request to the relevant controller.
4.  Controller validates request input and delegates to a service.
5.  Service executes business rules and coordinates repositories.
6.  Spring Data JPA/Hibernate persists or retrieves data from MySQL.
7.  Controller returns a Thymeleaf view or redirects after a successful
    form action.

### 6.3 Layer Responsibilities

  -----------------------------------------------------------------------
  Layer                               Responsibility
  ----------------------------------- -----------------------------------
  Presentation                        Thymeleaf templates, HTML, CSS,
                                      Bootstrap, small amounts of
                                      JavaScript

  Controller                          HTTP routing, binding, validation
                                      handoff, view selection

  Service                             Business rules, workflow
                                      orchestration,
                                      authorization-sensitive operations

  Repository                          Database persistence and queries

  Entity                              Persistent domain model

  DTO/Form Model                      Input/output data transfer and form
                                      validation

  Security                            Authentication, role checks, route
                                      and operation authorization

  Configuration                       Application configuration and
                                      infrastructure beans
  -----------------------------------------------------------------------

### 6.4 Proposed Domain Modules

-   authentication
-   student
-   company
-   job
-   eligibility
-   application
-   recruitment
-   interview
-   analytics
-   announcement
-   notification

Modules may be represented as packages within one Spring Boot
application.

## 7. Technology Requirements

  Area                           Selected technology
  ------------------------------ ------------------------------------
  Language                       Java 21
  Backend framework              Spring Boot 3.x
  Web framework                  Spring MVC
  Server-side templates          Thymeleaf
  Frontend styling               Bootstrap 5, HTML, CSS
  Database                       MySQL 8
  ORM                            Spring Data JPA and Hibernate
  Authentication/authorization   Spring Security
  Build tool                     Maven
  Testing                        JUnit 5, Mockito, Spring Boot Test
  API/manual testing             Postman where applicable
  Version control                Git and GitHub

Use a compatible, supported patch release from the Spring Boot 3.x line
and compatible dependency versions. Do not upgrade to a different major
Spring Boot line without checking compatibility and team approval.

## 8. Data Requirements and Initial Entity Model

The following is the initial logical entity inventory. Exact columns,
nullability, cardinalities, indexes, and join-table design must be
finalized in the database design/ERD before implementation.

  --------------------------------------------------------------------------
  Entity                     Purpose                 Key relationships
  -------------------------- ----------------------- -----------------------
  User                       Credentials, identity,  One user has one role;
                             role, account state     may own a
                                                     StudentProfile or
                                                     Company

  Role                       STUDENT, RECRUITER,     Referenced by User
                             PLACEMENT_OFFICER       

  StudentProfile             Academic and student    One-to-one with User
                             details                 

  Company                    Company details and     Associated with
                             approval state          recruiter User; has
                                                     Jobs

  Job                        Job posting and         Belongs to Company; has
                             publication state       criteria and
                                                     Applications

  JobEligibilityCriteria     Criteria for an         Belongs to Job
                             individual job          

  JobEligibleBranch          Branch values           Belongs to Job or
                             associated with a job   criteria record

  Application                A student's application References
                             to a job                StudentProfile and Job

  ApplicationStatusHistory   Status changes and      Belongs to Application;
                             audit history           actor references User

  RecruitmentDrive           Placement/recruitment   May be associated with
                             event grouping          Jobs

  Interview                  Interview schedule and  Belongs to Application
                             outcome details         

  Announcement               Placement notices       Created by Placement
                                                     Officer

  Notification               In-app user             Belongs to recipient
                             notifications           User
  --------------------------------------------------------------------------

### 8.1 Data Integrity Requirements

-   User email and other chosen unique identifiers must have appropriate
    unique constraints.
-   Student registration/roll number must be unique where applicable.
-   A student-job pair must be unique in Application.
-   Every job must reference its owning company.
-   Every application must reference one student and one job.
-   Every status history record must reference its application and the
    actor who made the change.
-   Every interview must reference the relevant application.
-   Deletion behavior must preserve required historical records. Prefer
    controlled deactivation or restricted deletion for records
    referenced by applications and history.

## 9. Key Workflows

### 9.1 Student Onboarding

1.  Student submits registration form.
2.  System creates account with pending verification state.
3.  Student completes academic profile.
4.  Placement Officer reviews the profile.
5.  Placement Officer verifies or rejects it.
6.  Verified student can use placement application workflows.

### 9.2 Company Onboarding and Job Publication

1.  Company representative registers.
2.  System creates company account in pending approval state.
3.  Placement Officer approves or rejects the company.
4.  Approved recruiter creates a job posting.
5.  Recruiter defines criteria for that specific job.
6.  Recruiter submits job for review.
7.  Placement Officer approves or rejects the job.
8.  Approved and open job becomes visible to students.

### 9.3 Job Application

1.  Verified student opens an approved job.
2.  Backend evaluates student profile against that job's criteria.
3.  System displays eligibility and unmet criteria.
4.  Eligible student submits application.
5.  Backend rechecks eligibility, job state, dates, and duplicate
    application.
6.  System records application and initial status history.
7.  Recruiter reviews the application and updates its status.
8.  Student sees the latest status and relevant notifications.

### 9.4 Interview and Result

1.  Recruiter selects an applicant for an interview stage.
2.  Recruiter schedules or updates interview details.
3.  System notifies the student.
4.  Student views the interview schedule.
5.  Recruiter updates subsequent application status and final outcome.
6.  Placement Officer monitors recruitment progress and outcomes.

### 9.5 Placement Analytics

1.  System queries recorded student, job, application, and outcome data.
2.  Analytics service calculates approved aggregate metrics.
3.  Placement Officer views college-level analytics.
4.  Students view the student-facing published analytics.
5.  Private student-level data is not exposed through aggregate
    dashboards.

## 10. Application Status Model

The baseline application statuses are:

-   `APPLIED`
-   `UNDER_REVIEW`
-   `SHORTLISTED`
-   `INTERVIEW_SCHEDULED`
-   `SELECTED`
-   `REJECTED`
-   `NOT_SELECTED`
-   `WITHDRAWN`

The service layer must validate permitted transitions. The exact
transition matrix should be implemented as an explicit rule set and
covered by tests. Status history must retain previous status, new
status, actor, timestamp, and an optional reason/comment where
supported.

## 11. UI / Page Requirements

The UI should provide separate navigation and dashboards for each role.

### Student pages

-   Registration and login
-   Student dashboard
-   Academic profile
-   Resume management
-   Job listing and job details
-   Eligibility result
-   My applications and application details
-   Interview schedule
-   Placement analytics
-   Announcements and notifications

### Recruiter pages

-   Company registration and login
-   Recruiter dashboard
-   Company profile
-   Job list and create/edit job
-   Eligibility criteria editor
-   Job approval state
-   Applicant list and applicant details
-   Application review/status update
-   Interview scheduling
-   Recruitment results

### Placement Officer pages

-   Placement Officer login/dashboard
-   Pending student verification
-   Student profile review
-   Pending company approvals
-   Company management
-   Pending job approvals
-   Job and recruitment monitoring
-   Placement analytics
-   Announcement management
-   Notifications/administrative updates

## 12. Validation and Error Handling

-   Validate required fields, lengths, formats, and numeric ranges on
    the server.
-   Validate CGPA against the configured grading scale used by the
    project.
-   Reject unsupported resume formats and files exceeding the configured
    size.
-   Reject applications for closed, unapproved, or ineligible jobs.
-   Reject duplicate applications.
-   Reject recruiter access to another company's job/application.
-   Reject student access to another student's private data.
-   Reject unauthorized verification, approval, and result updates.
-   Display user-friendly messages; log technical details server-side
    without exposing sensitive information.
-   Use centralized exception handling where appropriate.

## 13. Testing Requirements

At minimum, test the following:

### Unit tests

-   Eligibility evaluation for eligible and ineligible students.
-   Missing academic data behavior.
-   Application status transition rules.
-   Placement percentage and aggregate metric calculations.
-   Validation of job opening/closing state.

### Integration tests

-   Student registration and pending verification.
-   Placement Officer student verification.
-   Company registration and approval.
-   Job submission and approval.
-   Job-specific eligibility evaluation against persisted data.
-   Application creation and duplicate prevention.
-   Recruiter ownership restrictions.
-   Interview scheduling and student visibility.
-   Announcement and notification creation/read state.

### Manual acceptance tests

-   Complete student journey from registration to application.
-   Complete recruiter journey from company registration to final
    result.
-   Complete Placement Officer journey for verification, approvals,
    monitoring, and announcements.
-   Confirm students can view placement analytics and interview
    schedules.

## 14. Acceptance Criteria

The MVP is acceptable when all of the following are demonstrated:

1.  Students can self-register and remain pending until verified by a
    Placement Officer.
2.  Placement Officers can verify or reject student profiles.
3.  Companies can self-register and remain pending until approved.
4.  Placement Officers can approve or reject companies.
5.  Approved companies can create job postings with job-specific
    eligibility criteria.
6.  Placement Officers can approve or reject job postings.
7.  Students can view approved jobs and receive correct
    backend-calculated eligibility results.
8.  Only verified and eligible students can apply to open jobs.
9.  Duplicate applications are prevented.
10. Recruiters can review applicants only for their own company's jobs.
11. Recruiters can update recruitment statuses and results.
12. Placement Officers can monitor recruitment outcomes across
    companies.
13. Recruiters can schedule interviews and students can view their own
    schedules.
14. Students can view college-level placement analytics.
15. Placement Officers can publish announcements.
16. Relevant in-app notifications are created and visible to users.
17. Role and ownership restrictions are enforced on the server.
18. Core workflows have automated tests and can be demonstrated
    reliably.

## 15. Repository and Code Organization Guidance

Use a single Spring Boot Maven project. Prefer a feature-oriented
package structure with layers inside each feature, or a clearly
organized layered structure. The team should choose one convention and
apply it consistently.

A suggested starting structure:

``` text
campusconnect/
├── .github/
│   └── (optional workflows and templates)
├── docs/
│   ├── CampusConnect_Technical_Requirements_Document.md
│   ├── diagrams/
│   └── database/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── .../campusconnect/
│   │   │       ├── config/
│   │   │       ├── security/
│   │   │       ├── auth/
│   │   │       ├── student/
│   │   │       ├── company/
│   │   │       ├── job/
│   │   │       ├── eligibility/
│   │   │       ├── application/
│   │   │       ├── recruitment/
│   │   │       ├── interview/
│   │   │       ├── analytics/
│   │   │       ├── announcement/
│   │   │       └── notification/
│   │   └── resources/
│   │       ├── templates/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── images/
│   │       └── application.yml
│   └── test/
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

The package structure is guidance, not a mandate to create empty
packages for every module on day one. Add modules as implementation
begins.

## 16. Configuration and Environment Requirements

-   Use environment variables or an untracked local configuration file
    for database credentials and secrets.
-   Commit a safe `application-example.yml` or documented environment
    variable list, not real credentials.
-   Configure a local MySQL database for development.
-   Use separate profiles where useful, such as `local` and `test`.
-   Do not commit uploaded resumes, database dumps containing personal
    data, IDE-specific files, build output, or secrets.
-   Document the Java and Maven requirements in README.
-   Include database creation and application startup instructions.

## 17. Instructions for AI Coding Assistants

Antigravity, OpenCode, or another coding assistant must:

1.  Read this TRD and the repository README before making changes.
2.  Treat the requirements and scope in this document as the approved
    baseline.
3.  Not add features outside the agreed scope.
4.  Use Java 21, Spring Boot 3.x, Maven, Thymeleaf, Bootstrap 5, MySQL,
    Spring Data JPA, Hibernate, and Spring Security unless the team
    explicitly changes the stack.
5.  Follow layered architecture and keep business logic in services.
6.  Enforce role and resource ownership checks on the backend.
7.  Implement job-specific eligibility criteria and revalidate
    eligibility at application submission.
8.  Use DTOs/form objects for web input rather than binding sensitive
    entity fields directly.
9.  Add tests for important business rules and security boundaries.
10. Avoid hard-coded credentials, secrets, sample production data, and
    hard-coded analytics.
11. Explain proposed changes and identify affected requirements before
    substantial architectural changes.
12. Do not generate the entire application in one step. Implement
    incrementally, verify builds and tests, and keep the repository
    runnable.

## 18. Open Technical Decisions to Resolve During Detailed Design

These are implementation details that must be settled before the
relevant module is coded. They do not change the approved product scope:

-   Exact student identity fields and the institution's CGPA/backlog
    conventions.
-   Whether student profile edits to eligibility-related fields trigger
    re-verification.
-   Exact company and job approval states.
-   Exact application status transition matrix.
-   Whether a job can be edited after approval and whether edits require
    re-approval.
-   Resume file size/type limits and local storage strategy.
-   Exact placement percentage definition and treatment of multiple
    offers.
-   Whether analytics are live-calculated or periodically summarized.
-   Interview time zone and handling of rescheduled interviews.
-   Notification event rules and retention.
-   Exact UI theme and page-level layout.

These decisions should be documented in the design or implementation
notes. Do not invent institutional policies; use clearly identified demo
assumptions when the college has not supplied a rule.

------------------------------------------------------------------------

**End of Document --- CampusConnect TRD v1.0**
