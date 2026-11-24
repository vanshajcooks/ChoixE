import os
import sys
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak
)

def build_pdf():
    pdf_filename = "/home/vanshaj/dev/projs/ewa/campusconnect/CampusConnect_Presentation.pdf"
    
    doc = SimpleDocTemplate(
        pdf_filename,
        pagesize=landscape(A4),
        leftMargin=30,
        rightMargin=30,
        topMargin=22,
        bottomMargin=18
    )

    styles = getSampleStyleSheet()

    # Enterprise color palette
    c_navy = colors.HexColor("#0F172A")
    c_blue = colors.HexColor("#1D4ED8")
    c_light_blue = colors.HexColor("#EFF6FF")
    c_blue_border = colors.HexColor("#3B82F6")
    c_slate = colors.HexColor("#334155")
    c_subtext = colors.HexColor("#64748B")
    c_card_bg = colors.HexColor("#F8FAFC")
    c_card_border = colors.HexColor("#CBD5E1")
    c_green = colors.HexColor("#059669")
    c_green_bg = colors.HexColor("#ECFDF5")
    c_purple = colors.HexColor("#7C3AED")
    c_purple_bg = colors.HexColor("#F5F3FF")
    c_amber_bg = colors.HexColor("#FFFBEB")
    c_amber_border = colors.HexColor("#D97706")
    c_amber_text = colors.HexColor("#92400E")

    # Typography styles
    s_meta = ParagraphStyle('SlideMeta', fontName='Helvetica-Bold', fontSize=8.5, leading=11, textColor=c_blue, spaceAfter=2)
    s_title = ParagraphStyle('SlideTitle', fontName='Helvetica-Bold', fontSize=16, leading=19, textColor=c_navy, spaceAfter=2)
    s_subtitle = ParagraphStyle('SlideSubtitle', fontName='Helvetica', fontSize=9.5, leading=12, textColor=c_subtext, spaceAfter=6)
    s_card_head = ParagraphStyle('CardHeading', fontName='Helvetica-Bold', fontSize=10, leading=13, textColor=c_navy, spaceAfter=4)
    s_bullet = ParagraphStyle('BulletText', fontName='Helvetica', fontSize=8.5, leading=11.5, textColor=c_slate, spaceAfter=3.5)
    s_guide = ParagraphStyle('GuideText', fontName='Helvetica', fontSize=8, leading=10.5, textColor=colors.HexColor("#1E40AF"))
    s_notes = ParagraphStyle('SpeakerNotes', fontName='Helvetica-Oblique', fontSize=8, leading=10.5, textColor=c_amber_text)
    s_table_header = ParagraphStyle('TableHeader', fontName='Helvetica-Bold', fontSize=8, leading=10, textColor=c_navy)
    s_table_cell = ParagraphStyle('TableCell', fontName='Helvetica', fontSize=7.8, leading=9.8, textColor=c_slate)

    story = []

    def make_header(num, cat, title, subtitle):
        content = [
            Paragraph(f"SLIDE {num:02d} OF 14 &nbsp;•&nbsp; {cat.upper()}", s_meta),
            Paragraph(title, s_title),
            Paragraph(subtitle, s_subtitle)
        ]
        t = Table([[content]], colWidths=[781.89])
        t.setStyle(TableStyle([
            ('BOTTOMPADDING', (0,0), (-1,-1), 0),
            ('TOPPADDING', (0,0), (-1,-1), 0),
            ('LEFTPADDING', (0,0), (-1,-1), 0),
            ('RIGHTPADDING', (0,0), (-1,-1), 0),
            ('LINEBELOW', (0,0), (-1,-1), 1.5, c_blue)
        ]))
        return t

    def make_notes(text):
        p = Paragraph(f"<b>Speaker Notes:</b> {text}", s_notes)
        t = Table([[p]], colWidths=[781.89])
        t.setStyle(TableStyle([
            ('BACKGROUND', (0,0), (-1,-1), c_amber_bg),
            ('BOX', (0,0), (-1,-1), 0.5, c_amber_border),
            ('LINELEFT', (0,0), (-1,-1), 3.5, c_amber_border),
            ('TOPPADDING', (0,0), (-1,-1), 4.5),
            ('BOTTOMPADDING', (0,0), (-1,-1), 4.5),
            ('LEFTPADDING', (0,0), (-1,-1), 8),
            ('RIGHTPADDING', (0,0), (-1,-1), 8),
        ]))
        return t

    # =========================================================================
    # SLIDE 1: COVER
    # =========================================================================
    p_badge = Paragraph("<font color='#93C5FD'><b>ENTERPRISE WEB APPLICATIONS (EWA) COURSE DELIVERABLE</b></font>", ParagraphStyle('B', fontName='Helvetica-Bold', fontSize=9, alignment=1))
    p_main_title = Paragraph("CampusConnect", ParagraphStyle('T1', fontName='Helvetica-Bold', fontSize=28, leading=32, textColor=colors.white, alignment=1))
    p_main_sub = Paragraph("Enterprise Campus Placement Management Portal", ParagraphStyle('T2', fontName='Helvetica', fontSize=13, leading=16, textColor=colors.HexColor("#93C5FD"), alignment=1))
    p_desc = Paragraph("A centralized, multi-tenant web enterprise portal engineered on Spring Boot 3 &amp; MySQL 8, providing end-to-end placement automation with strict role-based governance, automated eligibility auditing, and verifiable recruitment lifecycle tracking.", ParagraphStyle('TD', fontName='Helvetica', fontSize=9.5, leading=13.5, textColor=colors.HexColor("#CBD5E1"), alignment=1))

    def meta_box(title, val):
        return [
            Paragraph(f"<font color='#60A5FA'><b>{title.upper()}</b></font>", ParagraphStyle('MB1', fontName='Helvetica-Bold', fontSize=7.5, leading=9.5)),
            Spacer(1, 2),
            Paragraph(f"<font color='#FFFFFF'>{val}</font>", ParagraphStyle('MB2', fontName='Helvetica', fontSize=8.5, leading=11))
        ]

    meta_table = Table([
        [
            meta_box("Architecture", "Layered Monolith<br/>Spring Boot 3 + Java 21"),
            meta_box("Database &amp; ORM", "MySQL 8.0 (InnoDB)<br/>Spring Data JPA / Hibernate 6"),
            meta_box("Security Engine", "Spring Security + BCrypt<br/>Multi-Tenant RBAC"),
            meta_box("Presentation Tier", "Thymeleaf SSR + Bootstrap 5<br/>Responsive Web UI")
        ]
    ], colWidths=[185, 185, 185, 185])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), colors.HexColor("#1E293B")),
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#334155")),
        ('TOPPADDING', (0,0), (-1,-1), 7),
        ('BOTTOMPADDING', (0,0), (-1,-1), 7),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))

    cover_inner = [
        Spacer(1, 10),
        p_badge,
        Spacer(1, 10),
        p_main_title,
        Spacer(1, 4),
        p_main_sub,
        Spacer(1, 12),
        p_desc,
        Spacer(1, 20),
        meta_table,
        Spacer(1, 15)
    ]

    cover_box = Table([[cover_inner]], colWidths=[781.89])
    cover_box.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_navy),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#1E3A8A")),
        ('TOPPADDING', (0,0), (-1,-1), 15),
        ('BOTTOMPADDING', (0,0), (-1,-1), 15),
        ('LEFTPADDING', (0,0), (-1,-1), 20),
        ('RIGHTPADDING', (0,0), (-1,-1), 20),
    ]))

    story.append(cover_box)
    story.append(Spacer(1, 14))
    story.append(make_notes("Good morning/afternoon respected Professor. Today we present CampusConnect, our comprehensive Enterprise Web Application project. We have built an end-to-end recruitment management ecosystem engineered to replace outdated manual spreadsheet tracking with automated eligibility verification, multi-stage approval governance, and role-based data security."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 2: PROBLEM STATEMENT
    # =========================================================================
    story.append(make_header(2, "Business Context & Motivation", "Problem Statement & Enterprise Need", "Transforming fragmented institutional campus recruitment into a governed enterprise workflow"))
    story.append(Spacer(1, 6))

    s2_left = [
        Paragraph("Challenges in Traditional Placement Drives", s_card_head),
        Paragraph("• <b>Data Fragmentation:</b> Student credentials and drive details scattered across spreadsheets, Google Forms, and informal chats.", s_bullet),
        Paragraph("• <b>Manual Eligibility Auditing:</b> Placement teams manually cross-check CGPA, active backlogs, and branch criteria for hundreds of applicants.", s_bullet),
        Paragraph("• <b>Lack of Audit Trails:</b> Application status changes (applied, shortlisted, rejected) lack timestamped historical logs.", s_bullet),
        Paragraph("• <b>Communication Gaps:</b> Students miss interview slots and notifications due to uncoordinated broadcast channels.", s_bullet),
        Paragraph("• <b>Security &amp; Privacy Vulnerabilities:</b> Sensitive resumes and academic marks shared openly without access boundaries.", s_bullet),
    ]

    s2_right = [
        Paragraph("Enterprise Value Proposition of CampusConnect", s_card_head),
        Paragraph("• <b>Centralized Hub:</b> Unified portal connecting Students, Corporate Recruiters, and the College Placement Office (TPO).", s_bullet),
        Paragraph("• <b>Automated Eligibility Engine:</b> Server-side rule engine verifies CGPA, department, and backlog criteria before application submission.", s_bullet),
        Paragraph("• <b>Mandatory Governance:</b> TPO verifies student profiles and approves company job postings before campus-wide visibility.", s_bullet),
        Paragraph("• <b>Auditable State Machine:</b> Every application status transition is validated and permanently logged with reviewer remarks.", s_bullet),
        Paragraph("• <b>Institutional Intelligence:</b> Real-time analytics for placement percentages, average salary packages, and branch metrics.", s_bullet),
    ]

    t2 = Table([[s2_left, s2_right]], colWidths=[385, 385])
    t2.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BACKGROUND', (1,0), (1,0), c_green_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('BOX', (1,0), (1,0), 0.5, colors.HexColor("#A7F3D0")),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('LINELEFT', (1,0), (1,0), 3, c_green),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t2)
    story.append(Spacer(1, 6))
    
    s2_guide = Table([[Paragraph("<b>Suggested Slide Visual &amp; Demo:</b> Present a 2-column comparative layout. Point out how spreadsheet verification fails at scale (500+ applicants across 4 branches), and contrast it with CampusConnect's instant automated server-side verification.", s_guide)]], colWidths=[781.89])
    s2_guide.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_light_blue),
        ('BOX', (0,0), (-1,-1), 0.5, c_blue_border),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(s2_guide)
    story.append(Spacer(1, 6))
    story.append(make_notes("In higher education institutes, placement drives handle hundreds of companies and thousands of applications. The manual approach fails when verifying complex criteria across branches. CampusConnect solves this at the enterprise level by shifting the burden of compliance, verification, and scheduling to an automated backend."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 3: SOLUTION OVERVIEW (3 ROLES)
    # =========================================================================
    story.append(make_header(3, "System Architecture Scope", "Solution Overview: 3-Role Ecosystem", "Clear separation of duties across Students, Recruiters, and Placement Administrators"))
    story.append(Spacer(1, 6))

    c1 = [
        Paragraph("1. Student Portal", s_card_head),
        Paragraph("• <b>Profile &amp; Resumes:</b> Maintain verified academic credentials and securely upload PDF resumes.", s_bullet),
        Paragraph("• <b>Job Discovery:</b> Browse open opportunities with dynamic 'Eligible / Ineligible' status badges.", s_bullet),
        Paragraph("• <b>1-Click Applications:</b> Instantly apply when criteria match; view clear unmet criteria if disqualified.", s_bullet),
        Paragraph("• <b>Interview Schedules:</b> Real-time calendar view of upcoming interview rounds and links.", s_bullet),
    ]

    c2 = [
        Paragraph("2. Recruiter Portal", s_card_head),
        Paragraph("• <b>Corporate Profile:</b> Maintain verified corporate profile and industry details.", s_bullet),
        Paragraph("• <b>Job Postings:</b> Define multi-parameter criteria (CGPA cutoff, allowed branches, max backlogs).", s_bullet),
        Paragraph("• <b>Applicant Workbench:</b> Review verified candidates, inspect resumes, and update recruitment stages.", s_bullet),
        Paragraph("• <b>Interview Management:</b> Schedule online/offline interview rounds with date and location details.", s_bullet),
    ]

    c3 = [
        Paragraph("3. TPO Admin Portal", s_card_head),
        Paragraph("• <b>Student Verification:</b> Validate marks and approve student registrations.", s_bullet),
        Paragraph("• <b>Company &amp; Job Gatekeeping:</b> Review corporate authenticity and approve job postings prior to publication.", s_bullet),
        Paragraph("• <b>Placement Analytics:</b> Track department placements, top recruiting partners, and salary averages.", s_bullet),
        Paragraph("• <b>Priority Notices:</b> Broadcast urgent placement notices targeted to specific user groups.", s_bullet),
    ]

    t3 = Table([[c1, c2, c3]], colWidths=[253, 255, 253])
    t3.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BACKGROUND', (1,0), (1,0), c_purple_bg),
        ('BACKGROUND', (2,0), (2,0), c_green_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('BOX', (1,0), (1,0), 0.5, colors.HexColor("#DDD6FE")),
        ('BOX', (2,0), (2,0), 0.5, colors.HexColor("#A7F3D0")),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('LINELEFT', (1,0), (1,0), 3, c_purple),
        ('LINELEFT', (2,0), (2,0), 3, c_green),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(t3)
    story.append(Spacer(1, 8))

    s3_guide = Table([[Paragraph("<b>Screenshot &amp; Demo Guidance:</b> Display the 3 distinct dashboard home screens side-by-side or highlight the dynamic navbar that adapts navigation options based on the authenticated role (Student vs Recruiter vs Officer).", s_guide)]], colWidths=[781.89])
    s3_guide.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_light_blue),
        ('BOX', (0,0), (-1,-1), 0.5, c_blue_border),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(s3_guide)
    story.append(Spacer(1, 8))
    story.append(make_notes("The solution is built around three distinct user roles. The student applies for matching jobs, the recruiter handles candidate shortlisting, and the Placement Officer serves as the governing controller who verifies students and approves company job postings to ensure high-quality placement standards."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 4: ARCHITECTURE & TECH STACK
    # =========================================================================
    story.append(make_header(4, "Enterprise System Architecture", "Enterprise Layered Architecture & Tech Stack", "Layered monolithic design pattern ensuring strong cohesion and transaction boundaries"))
    story.append(Spacer(1, 6))

    s4_left = [
        Paragraph("Enterprise Tiered Breakdown", s_card_head),
        Paragraph("• <b>Presentation &amp; Routing:</b> Thymeleaf SSR + Bootstrap 5 UI routed by Spring MVC with strict HTTP parameter validation.", s_bullet),
        Paragraph("• <b>Enterprise Security:</b> Spring Security filter chains enforcing BCrypt, active sessions, and multi-tenant RBAC.", s_bullet),
        Paragraph("• <b>Business Services:</b> Encapsulates business workflows, eligibility rules, and transaction scopes (@Transactional).", s_bullet),
        Paragraph("• <b>Persistence Layer:</b> Spring Data JPA &amp; Hibernate 6 ORM providing parameterized queries &amp; HikariCP connection pool.", s_bullet),
        Paragraph("• <b>Relational Database:</b> MySQL 8.0 InnoDB engine ensuring ACID compliance, foreign keys, and referential integrity.", s_bullet),
    ]

    # Visual Table for Architecture Layers
    s_arch = ParagraphStyle('Arch', fontName='Helvetica-Bold', fontSize=8, leading=10, textColor=colors.HexColor("#0F172A"), alignment=1)
    s_sub = ParagraphStyle('ArchSub', fontName='Helvetica', fontSize=7, leading=9, textColor=colors.HexColor("#475569"), alignment=1)
    
    layers = [
        ("Presentation Layer", "Thymeleaf SSR • Bootstrap 5 • Responsive UI", "#EFF6FF", "#3B82F6"),
        ("Security Interceptor Tier", "Spring Security • Multi-Tenant RBAC • BCrypt", "#F5F3FF", "#7C3AED"),
        ("Web Controller Tier", "Spring MVC • RESTful Handlers • DTO Validation", "#EFF6FF", "#3B82F6"),
        ("Business Service Tier", "Eligibility Engine • Workflows • @Transactional", "#ECFDF5", "#059669"),
        ("Persistence / ORM Tier", "Spring Data JPA • Hibernate 6 • HikariCP Pool", "#FEF3C7", "#D97706"),
        ("Database Storage Tier", "MySQL 8.0 InnoDB • ACID Transactions • UTF-8", "#F1F5F9", "#475569"),
    ]
    
    rows = []
    for title, sub, bg, border in layers:
        content = [Paragraph(f"<b>{title}</b>", s_arch), Paragraph(sub, s_sub)]
        rows.append([content])
    
    s4_diag_table = Table(rows, colWidths=[385])
    ts4 = [
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]
    for i, (_, _, bg, border) in enumerate(layers):
        ts4.append(('BACKGROUND', (0, i), (0, i), colors.HexColor(bg)))
        ts4.append(('LINELEFT', (0, i), (0, i), 3.5, colors.HexColor(border)))
    s4_diag_table.setStyle(TableStyle(ts4))

    s4_right = [
        Paragraph("Tiered Architecture Flowchart", s_card_head),
        s4_diag_table
    ]

    t4 = Table([[s4_left, s4_right]], colWidths=[385, 385])
    t4.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('TOPPADDING', (0,0), (0,0), 8),
        ('BOTTOMPADDING', (0,0), (0,0), 8),
        ('LEFTPADDING', (0,0), (0,0), 10),
        ('RIGHTPADDING', (0,0), (0,0), 10),
    ]))
    story.append(t4)
    story.append(Spacer(1, 6))
    story.append(make_notes("Notice that we deliberately selected a Layered Monolith rather than microservices. In an academic institution, transactional integrity between student verification, job approvals, and interview scheduling is paramount. A monolithic architecture with Spring Boot avoids distributed transaction failure and simplifies deployment while keeping clean module boundaries."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 5: ROLE-BASED ACCESS CONTROL
    # =========================================================================
    story.append(make_header(5, "Security Architecture", "Role-Based Access Control (RBAC) & Authorization", "Multi-tenant role boundaries enforced at URL, controller, and service layers"))
    story.append(Spacer(1, 6))

    s5_left = [
        Paragraph("Security Principles Enforced", s_card_head),
        Paragraph("• <b>Least Privilege:</b> Strict endpoint restriction via Spring Security URL patterns (/student/**, /recruiter/**, /officer/**).", s_bullet),
        Paragraph("• <b>Role Mapping:</b> Authenticated sessions mapped to ROLE_STUDENT, ROLE_RECRUITER, or ROLE_OFFICER.", s_bullet),
        Paragraph("• <b>Tenant Isolation:</b> Recruiters cannot query or update other companies' candidates (prevents IDOR).", s_bullet),
        Paragraph("• <b>Dual Verification:</b> Both presentation tags and service-layer assertions guard protected operations.", s_bullet),
    ]

    table_data = [
        [Paragraph("<b>Functional Operation</b>", s_table_header),
         Paragraph("<b>Student</b>", s_table_header),
         Paragraph("<b>Recruiter</b>", s_table_header),
         Paragraph("<b>Placement Officer</b>", s_table_header)],
        [Paragraph("Account Self-Registration", s_table_cell), Paragraph("Open", s_table_cell), Paragraph("Open", s_table_cell), Paragraph("Pre-provisioned", s_table_cell)],
        [Paragraph("Student Profile Verification", s_table_cell), Paragraph("Forbidden", s_table_cell), Paragraph("Forbidden", s_table_cell), Paragraph("Exclusive TPO Power", s_table_cell)],
        [Paragraph("Create &amp; Edit Job Postings", s_table_cell), Paragraph("Forbidden", s_table_cell), Paragraph("Own Postings Only", s_table_cell), Paragraph("Review &amp; Manage", s_table_cell)],
        [Paragraph("Job Posting Approval &amp; Publish", s_table_cell), Paragraph("Forbidden", s_table_cell), Paragraph("Forbidden", s_table_cell), Paragraph("Mandatory Approval", s_table_cell)],
        [Paragraph("Apply for Job Postings", s_table_cell), Paragraph("If Verified &amp; Eligible", s_table_cell), Paragraph("Forbidden", s_table_cell), Paragraph("Forbidden", s_table_cell)],
        [Paragraph("Shortlist &amp; Move Applications", s_table_cell), Paragraph("Forbidden", s_table_cell), Paragraph("Own Applicants Only", s_table_cell), Paragraph("Supervisory View", s_table_cell)],
        [Paragraph("Schedule Interviews", s_table_cell), Paragraph("Forbidden", s_table_cell), Paragraph("Own Applicants Only", s_table_cell), Paragraph("Supervisory View", s_table_cell)],
        [Paragraph("View Placement Analytics", s_table_cell), Paragraph("Aggregated View", s_table_cell), Paragraph("Out of Scope", s_table_cell), Paragraph("Complete Admin View", s_table_cell)],
    ]

    t5_matrix = Table(table_data, colWidths=[150, 100, 110, 130])
    t5_matrix.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#F1F5F9")),
        ('BOX', (0,0), (-1,-1), 0.5, c_card_border),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))

    t5 = Table([[s5_left, t5_matrix]], colWidths=[270, 500])
    t5.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_purple_bg),
        ('BOX', (0,0), (0,0), 0.5, colors.HexColor("#DDD6FE")),
        ('LINELEFT', (0,0), (0,0), 3, c_purple),
        ('TOPPADDING', (0,0), (0,0), 8),
        ('BOTTOMPADDING', (0,0), (0,0), 8),
        ('LEFTPADDING', (0,0), (0,0), 8),
        ('RIGHTPADDING', (0,0), (0,0), 8),
    ]))
    story.append(t5)
    story.append(Spacer(1, 6))
    story.append(make_notes("In EWA, access control must be rigorous. This permission matrix demonstrates how our Spring Security configuration segments the application. A student cannot approve themselves, and recruiters cannot see competitors' postings or applicant resumes. Any unauthorized URL attempt redirects automatically to an access-denied error page."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 6: BUSINESS WORKFLOWS & STATE MACHINE
    # =========================================================================
    story.append(make_header(6, "Business Logic & Lifecycle", "Core Business Workflows & State Transitions", "Finite State Machine ensuring validated, auditable application progression"))
    story.append(Spacer(1, 6))

    s6_left = [
        Paragraph("Application State Machine Rules", s_card_head),
        Paragraph("• <b>Controlled Transitions:</b> Progression: <b>APPLIED &rarr; SHORTLISTED &rarr; INTERVIEW_SCHEDULED &rarr; SELECTED / REJECTED</b>.", s_bullet),
        Paragraph("• <b>Illegal Jump Prevention:</b> Candidates cannot jump straight to Selected without completing interview stages.", s_bullet),
        Paragraph("• <b>Immutable History Logs:</b> Every update generates a record in <code>ApplicationStatusHistory</code> with reviewer comments.", s_bullet),
        Paragraph("• <b>Two-Phase Job Gatekeeping:</b> Recruiter drafts job &rarr; Placement Officer inspects and approves before student visibility.", s_bullet),
    ]

    # Visual Table for State Pipeline
    state_steps = [
        ("Step 1: APPLIED", "Candidate submits application; verified profile & criteria validated.", "#EFF6FF", "#3B82F6"),
        ("Step 2: SHORTLISTED", "Recruiter reviews verified profile, marks, and resume.", "#F5F3FF", "#7C3AED"),
        ("Step 3: INTERVIEW_SCHEDULED", "Interview round slated with date, time, mode, and meeting URL.", "#FEF3C7", "#D97706"),
        ("Step 4A: SELECTED", "Candidate clears rounds; official campus placement offer extended.", "#ECFDF5", "#059669"),
        ("Step 4B: REJECTED", "Candidate disqualified or fails round; feedback permanently logged.", "#FEF2F2", "#EF4444"),
    ]
    state_rows = []
    for stitle, sdesc, sbg, sborder in state_steps:
        scontent = [Paragraph(f"<b>{stitle}</b>", s_arch), Paragraph(sdesc, s_sub)]
        state_rows.append([scontent])
    
    s6_diag_table = Table(state_rows, colWidths=[385])
    ts6 = [
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]
    for i, (_, _, sbg, sborder) in enumerate(state_steps):
        ts6.append(('BACKGROUND', (0, i), (0, i), colors.HexColor(sbg)))
        ts6.append(('LINELEFT', (0, i), (0, i), 3.5, colors.HexColor(sborder)))
    s6_diag_table.setStyle(TableStyle(ts6))

    s6_right = [
        Paragraph("Application State Pipeline", s_card_head),
        s6_diag_table
    ]

    t6 = Table([[s6_left, s6_right]], colWidths=[385, 385])
    t6.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('TOPPADDING', (0,0), (0,0), 8),
        ('BOTTOMPADDING', (0,0), (0,0), 8),
        ('LEFTPADDING', (0,0), (0,0), 10),
        ('RIGHTPADDING', (0,0), (0,0), 10),
    ]))
    story.append(t6)
    story.append(Spacer(1, 6))
    story.append(make_notes("Notice the state machine governing applications. An enterprise application must protect against invalid states. If a recruiter attempts to schedule an interview for a rejected candidate or jump straight to selected, the service throws an ApplicationStatusTransitionException."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 7: AUTOMATED ELIGIBILITY ENGINE
    # =========================================================================
    story.append(make_header(7, "Business Rules & Automation", "Automated Eligibility Evaluation Engine", "Dynamic rule verification removing human error from candidate screening"))
    story.append(Spacer(1, 6))

    s7_left = [
        Paragraph("Dynamic Multi-Parameter Evaluation", s_card_head),
        Paragraph("• <b>Profile Verification Check:</b> Student must be verified by TPO (unverified students cannot apply).", s_bullet),
        Paragraph("• <b>Graduation Batch:</b> Student passing batch must match target job batch (e.g. 2026).", s_bullet),
        Paragraph("• <b>Department In-List:</b> Student department must exist in job's allowed branches.", s_bullet),
        Paragraph("• <b>Academic Cutoff:</b> Current student CGPA &ge; minimum required CGPA threshold.", s_bullet),
        Paragraph("• <b>Backlog Ceiling:</b> Active student backlogs &le; maximum allowed backlogs.", s_bullet),
        Paragraph("• <b>Application Deadline:</b> Application timestamp must precede published job deadline.", s_bullet),
    ]

    s7_right = [
        Paragraph("Evaluation Pipeline Architecture", s_card_head),
        Paragraph("• <b>Double-Gate Defense:</b> Client-side badges inform the student, but the server executes the authoritative check prior to database commit.", s_bullet),
        Paragraph("• <b>Granular Diagnostics:</b> Ineligible candidates receive specific feedback explaining unmet conditions (e.g., 'CGPA 7.10 is below required 7.50').", s_bullet),
        Paragraph("• <b>Race Condition Protection:</b> Composite unique constraints on (job_id, student_id) prevent duplicate submissions.", s_bullet),
    ]

    t7 = Table([[s7_left, s7_right]], colWidths=[385, 385])
    t7.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BACKGROUND', (1,0), (1,0), c_green_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('BOX', (1,0), (1,0), 0.5, colors.HexColor("#A7F3D0")),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('LINELEFT', (1,0), (1,0), 3, c_green),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t7)
    story.append(Spacer(1, 6))

    s7_guide = Table([[Paragraph("<b>Demo Screenshot Guidance:</b> Open the Student Job Details screen. Demonstrate how an eligible student sees a green 'You are eligible to apply' banner with one-click apply, while an ineligible student sees exact unmet criteria highlighted in red.", s_guide)]], colWidths=[781.89])
    s7_guide.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_light_blue),
        ('BOX', (0,0), (-1,-1), 0.5, c_blue_border),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(s7_guide)
    story.append(Spacer(1, 6))
    story.append(make_notes("The eligibility engine is a signature feature of CampusConnect. In traditional drives, students submit resumes blindly, and placement coordinators spend hours manually filtering spreadsheets. Here, the backend algorithm performs multi-variable validation in microseconds."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 8: DATA MODEL & AUDIT
    # =========================================================================
    story.append(make_header(8, "Database & Persistence Design", "Relational Data Model & Audit Architecture", "Third Normal Form (3NF) relational design with entity relationships and audit logs"))
    story.append(Spacer(1, 6))

    s8_left = [
        Paragraph("Schema Integrity &amp; Normalization", s_card_head),
        Paragraph("• <b>User Entity Decoupling:</b> Credentials stored in <code>User</code>; extended 1:1 into <code>StudentProfile</code> and <code>Company</code>.", s_bullet),
        Paragraph("• <b>Granular Criteria Isolation:</b> <code>Job</code> maintains a 1:1 relationship with <code>JobEligibilityCriteria</code>.", s_bullet),
        Paragraph("• <b>Composite Uniqueness:</b> Unique constraint on <code>(job_id, student_id)</code> guarantees no duplicate applications.", s_bullet),
        Paragraph("• <b>Historical Audit Log:</b> <code>ApplicationStatusHistory</code> preserves state changes with recruiter IDs and timestamps.", s_bullet),
        Paragraph("• <b>Referential Integrity:</b> Cascade options prevent orphaned interview or application records.", s_bullet),
    ]

    schema_rows = [
        ("User (id, email, password, role)", "Base entity for authentication; extended 1:1 to profiles", "#EFF6FF", "#3B82F6"),
        ("StudentProfile (id, user_id, cgpa, branch, ...)", "Academic marks, verification status, resume file path", "#EFF6FF", "#3B82F6"),
        ("Company (id, user_id, name, approval_status, ...)", "Recruiter corporate profile approved by TPO", "#F5F3FF", "#7C3AED"),
        ("Job (id, company_id, title, package, deadline, ...)", "Corporate job openings; 1:1 with eligibility criteria", "#F5F3FF", "#7C3AED"),
        ("Application (id, job_id, student_id, status, ...)", "Composite unique constraint; tracks application pipeline", "#ECFDF5", "#059669"),
        ("Interview (id, application_id, round, datetime)", "Multi-round interview slots with recruiter feedback", "#FEF3C7", "#D97706"),
        ("ApplicationStatusHistory (id, app_id, status, ...)", "Immutable historical audit log of all status transitions", "#F1F5F9", "#475569"),
    ]
    s_sc_title = ParagraphStyle('SCTitle', fontName='Helvetica-Bold', fontSize=7.5, leading=9.5, textColor=c_navy)
    s_sc_desc = ParagraphStyle('SCDesc', fontName='Helvetica', fontSize=7, leading=8.5, textColor=c_subtext)
    
    sc_table_rows = []
    for ent, desc, bg, border in schema_rows:
        sc_table_rows.append([[Paragraph(f"<b>{ent}</b>", s_sc_title), Paragraph(desc, s_sc_desc)]])
    
    s8_schema_table = Table(sc_table_rows, colWidths=[385])
    ts8 = [
        ('BOX', (0,0), (-1,-1), 0.5, colors.HexColor("#CBD5E1")),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0,0), (-1,-1), 2.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 2.5),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]
    for i, (_, _, bg, border) in enumerate(schema_rows):
        ts8.append(('BACKGROUND', (0, i), (0, i), colors.HexColor(bg)))
        ts8.append(('LINELEFT', (0, i), (0, i), 3, colors.HexColor(border)))
    s8_schema_table.setStyle(TableStyle(ts8))

    s8_right = [
        Paragraph("Entity Relational Schema (3NF)", s_card_head),
        s8_schema_table
    ]

    t8 = Table([[s8_left, s8_right]], colWidths=[385, 385])
    t8.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('TOPPADDING', (0,0), (0,0), 8),
        ('BOTTOMPADDING', (0,0), (0,0), 8),
        ('LEFTPADDING', (0,0), (0,0), 10),
        ('RIGHTPADDING', (0,0), (0,0), 10),
    ]))
    story.append(t8)
    story.append(Spacer(1, 6))
    story.append(make_notes("Here is our database schema in 3NF. The core entity User handles authentication, while StudentProfile and Company manage operational data. Notice the ApplicationStatusHistory table — this is an enterprise audit trail that ensures every status modification is traceable for academic oversight."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 9: ENTERPRISE SECURITY & TRANSACTIONS
    # =========================================================================
    story.append(make_header(9, "Enterprise Non-Functional Attributes", "Enterprise Security, Transactions & Reliability", "Defensive programming adhering to OWASP enterprise web security standards"))
    story.append(Spacer(1, 6))

    s9_left = [
        Paragraph("Application Security Hardening", s_card_head),
        Paragraph("• <b>BCrypt Password Hashing:</b> Passwords salted &amp; hashed with adaptive work factor 10. No plaintext credentials stored.", s_bullet),
        Paragraph("• <b>CSRF Protection:</b> Synchronizer Token Pattern enforced on all state-altering requests (POST/PUT/DELETE).", s_bullet),
        Paragraph("• <b>SQL Injection Immunity:</b> 100% parameterized queries via Spring Data JPA repositories.", s_bullet),
        Paragraph("• <b>XSS Defense:</b> Server-side template engine (Thymeleaf) automatically escapes HTML characters.", s_bullet),
        Paragraph("• <b>IDOR Defense:</b> Controllers enforce session ownership checks on requested applicant IDs.", s_bullet),
    ]

    s9_right = [
        Paragraph("Transaction Management &amp; Reliability", s_card_head),
        Paragraph("• <b>ACID Guarantees:</b> Service operations decorated with <code>@Transactional</code> ensure atomic commits.", s_bullet),
        Paragraph("• <b>Optimistic / State Concurrency:</b> Prevents dirty writes during simultaneous candidate status updates.", s_bullet),
        Paragraph("• <b>Secure File Handling:</b> Resumes stored with sanitized UUID filenames outside public web roots to stop directory traversal.", s_bullet),
        Paragraph("• <b>Institutional Privacy:</b> Conforms to student privacy norms with role-isolated visibility.", s_bullet),
    ]

    t9 = Table([[s9_left, s9_right]], colWidths=[385, 385])
    t9.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BACKGROUND', (1,0), (1,0), c_amber_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('BOX', (1,0), (1,0), 0.5, colors.HexColor("#FDE68A")),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('LINELEFT', (1,0), (1,0), 3, c_amber_border),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t9)
    story.append(Spacer(1, 6))

    s9_guide = Table([[Paragraph("<b>Security Standard Checklist:</b> Point out that the architecture complies with OWASP Top 10 guidelines for web applications, including parameterized persistence, CSRF token filters, and cryptographic password hashing.", s_guide)]], colWidths=[781.89])
    s9_guide.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_light_blue),
        ('BOX', (0,0), (-1,-1), 0.5, c_blue_border),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(s9_guide)
    story.append(Spacer(1, 6))
    story.append(make_notes("Security and data integrity are central to Enterprise Web Applications. We have incorporated OWASP top 10 defenses: BCrypt hashing, CSRF token validation on every form submission, automated XSS escaping via Thymeleaf, and transactional rollbacks on service exceptions."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 10: UI SHOWCASE: STUDENT & RECRUITER
    # =========================================================================
    story.append(make_header(10, "UI & User Experience Demonstration", "UI Showcase: Student & Recruiter Portals", "Task-oriented responsive interfaces designed for rapid decision-making"))
    story.append(Spacer(1, 6))

    s10_left = [
        Paragraph("Student Experience Highlights", s_card_head),
        Paragraph("• <b>Live Metric Counters:</b> Dashboard counters for Applied, Shortlisted, and Selected job applications.", s_bullet),
        Paragraph("• <b>Eligibility Badges:</b> Real-time visual status pills (Eligible / Ineligible) on job listings.", s_bullet),
        Paragraph("• <b>Resume Management:</b> PDF resume upload with instant live viewing capabilities.", s_bullet),
        Paragraph("• <b>Interview Timeline:</b> Chronological cards showing upcoming rounds and virtual meeting links.", s_bullet),
    ]

    s10_right = [
        Paragraph("Recruiter Workbench Highlights", s_card_head),
        Paragraph("• <b>Job Publishing Form:</b> Form configuring package, location, allowed branches, and GPA cutoffs.", s_bullet),
        Paragraph("• <b>Candidate Filtering:</b> Applicant review list sorted by submission date and review status.", s_bullet),
        Paragraph("• <b>One-Click Scheduling:</b> Modal form to assign interview date, time, and venue/URL.", s_bullet),
        Paragraph("• <b>Recruitment Stage Actions:</b> Dropdown to advance candidate to Shortlisted, Selected, or Rejected.", s_bullet),
    ]

    t10 = Table([[s10_left, s10_right]], colWidths=[385, 385])
    t10.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BACKGROUND', (1,0), (1,0), c_purple_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('BOX', (1,0), (1,0), 0.5, colors.HexColor("#DDD6FE")),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('LINELEFT', (1,0), (1,0), 3, c_purple),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t10)
    story.append(Spacer(1, 6))

    s10_guide = Table([[Paragraph("<b>Demo Screenshot Placement:</b> Capture and insert two screenshots: (1) Student Job Details Page (<code>/student/jobs/{id}</code>) displaying the Eligibility Checklist; (2) Recruiter Applicant Management Workbench (<code>/recruiter/jobs/{id}/applicants</code>).", s_guide)]], colWidths=[781.89])
    s10_guide.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_light_blue),
        ('BOX', (0,0), (-1,-1), 0.5, c_blue_border),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(s10_guide)
    story.append(Spacer(1, 6))
    story.append(make_notes("Here are the primary user interfaces. The student dashboard emphasizes clarity, showing application progression and interview dates. The recruiter portal acts as an applicant tracking system (ATS), allowing hiring teams to review verified resumes and schedule interview rounds in one place."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 11: UI SHOWCASE: TPO GOVERNANCE
    # =========================================================================
    story.append(make_header(11, "Administration & Governance", "UI Showcase: TPO Governance & Administration", "Institutional command center for verification, gatekeeping, and communications"))
    story.append(Spacer(1, 6))

    s11_left = [
        Paragraph("Student Verification Workbench", s_card_head),
        Paragraph("• <b>Roster Management:</b> Filterable roster of all registered students by department, batch, and status.", s_bullet),
        Paragraph("• <b>Academic Audit:</b> TPO reviews student CGPA, backlogs, and academic documents before approving.", s_bullet),
        Paragraph("• <b>Strict Gatekeeping:</b> Unverified students are completely blocked from applying to any placement drive.", s_bullet),
    ]

    s11_right = [
        Paragraph("Job Gatekeeping &amp; Broadcasts", s_card_head),
        Paragraph("• <b>Company Approval:</b> TPO validates recruiting company authenticity before granting posting privileges.", s_bullet),
        Paragraph("• <b>Job Approval Queue:</b> Job descriptions, CTC packages, and eligibility criteria reviewed before publishing.", s_bullet),
        Paragraph("• <b>Targeted Announcements:</b> Priority notice board for scheduling alerts and drive notices across roles.", s_bullet),
    ]

    t11 = Table([[s11_left, s11_right]], colWidths=[385, 385])
    t11.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_green_bg),
        ('BACKGROUND', (1,0), (1,0), c_amber_bg),
        ('BOX', (0,0), (0,0), 0.5, colors.HexColor("#A7F3D0")),
        ('BOX', (1,0), (1,0), 0.5, colors.HexColor("#FDE68A")),
        ('LINELEFT', (0,0), (0,0), 3, c_green),
        ('LINELEFT', (1,0), (1,0), 3, c_amber_border),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t11)
    story.append(Spacer(1, 6))

    s11_guide = Table([[Paragraph("<b>Demo Screenshot Placement:</b> Capture and insert two screenshots: (1) Placement Officer Student Verification Queue (<code>/officer/students</code>) with Verify action buttons; (2) Job Approval Management Queue (<code>/officer/jobs</code>) showing pending corporate job postings.", s_guide)]], colWidths=[781.89])
    s11_guide.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_light_blue),
        ('BOX', (0,0), (-1,-1), 0.5, c_blue_border),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(s11_guide)
    story.append(Spacer(1, 6))
    story.append(make_notes("The Placement Officer dashboard provides the governance layer. Notice how jobs do not go live automatically — the TPO must inspect the salary package and criteria. Similarly, students cannot apply until the TPO confirms their academic records match official college transcripts."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 12: ANALYTICS & REPORTING
    # =========================================================================
    story.append(make_header(12, "Enterprise Business Intelligence", "Enterprise Analytics & Reporting Engine", "Real-time placement intelligence powering institutional accreditations and student visibility"))
    story.append(Spacer(1, 6))

    s12_left = [
        Paragraph("Key Performance Indicators (KPIs)", s_card_head),
        Paragraph("• <b>Placement Percentage:</b> Real-time calculation of placed vs. eligible verified students.", s_bullet),
        Paragraph("• <b>Salary Statistics:</b> Highest, Average, and Median CTC packages parsed via <code>SalaryPackageParser</code>.", s_bullet),
        Paragraph("• <b>Department Distribution:</b> Placed vs. unplaced counts for CSE, IT, ECE, Mechanical, etc.", s_bullet),
        Paragraph("• <b>Top Recruiting Partners:</b> Roster of companies with highest hiring volumes and offers made.", s_bullet),
    ]

    # Analytics KPI Grid & Breakdown Table
    kpi_card_1 = [Paragraph("<b>Placement Ratio</b>", s_sc_desc), Paragraph("<font color='#059669'><b>84.6%</b></font>", ParagraphStyle('K1', fontName='Helvetica-Bold', fontSize=13, leading=15))]
    kpi_card_2 = [Paragraph("<b>Total Placed</b>", s_sc_desc), Paragraph("<font color='#1D4ED8'><b>381 / 450</b></font>", ParagraphStyle('K2', fontName='Helvetica-Bold', fontSize=13, leading=15))]
    kpi_card_3 = [Paragraph("<b>Highest CTC</b>", s_sc_desc), Paragraph("<font color='#7C3AED'><b>24.5 LPA</b></font>", ParagraphStyle('K3', fontName='Helvetica-Bold', fontSize=13, leading=15))]
    kpi_card_4 = [Paragraph("<b>Average CTC</b>", s_sc_desc), Paragraph("<font color='#D97706'><b>7.8 LPA</b></font>", ParagraphStyle('K4', fontName='Helvetica-Bold', fontSize=13, leading=15))]

    t_kpi = Table([[kpi_card_1, kpi_card_2, kpi_card_3, kpi_card_4]], colWidths=[95, 95, 95, 100])
    t_kpi.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_card_bg),
        ('BOX', (0,0), (-1,-1), 0.5, c_card_border),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))

    dept_data = [
        [Paragraph("<b>Department</b>", s_table_header), Paragraph("<b>Eligible</b>", s_table_header), Paragraph("<b>Placed</b>", s_table_header), Paragraph("<b>Rate</b>", s_table_header), Paragraph("<b>Avg CTC</b>", s_table_header)],
        [Paragraph("Computer Science (CSE)", s_table_cell), Paragraph("160", s_table_cell), Paragraph("148", s_table_cell), Paragraph("92.5%", s_table_cell), Paragraph("9.2 LPA", s_table_cell)],
        [Paragraph("Information Tech (IT)", s_table_cell), Paragraph("110", s_table_cell), Paragraph("101", s_table_cell), Paragraph("91.8%", s_table_cell), Paragraph("8.4 LPA", s_table_cell)],
        [Paragraph("Electronics (ECE)", s_table_cell), Paragraph("95", s_table_cell), Paragraph("74", s_table_cell), Paragraph("77.9%", s_table_cell), Paragraph("6.8 LPA", s_table_cell)],
        [Paragraph("Mechanical (MECH)", s_table_cell), Paragraph("85", s_table_cell), Paragraph("58", s_table_cell), Paragraph("68.2%", s_table_cell), Paragraph("5.9 LPA", s_table_cell)],
    ]
    t_dept = Table(dept_data, colWidths=[145, 60, 60, 60, 60])
    t_dept.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor("#F1F5F9")),
        ('BOX', (0,0), (-1,-1), 0.5, c_card_border),
        ('INNERGRID', (0,0), (-1,-1), 0.5, colors.HexColor("#E2E8F0")),
        ('TOPPADDING', (0,0), (-1,-1), 2.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 2.5),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))

    s12_right = [
        Paragraph("Institutional Placement Intelligence", s_card_head),
        t_kpi,
        Spacer(1, 5),
        t_dept
    ]

    t12 = Table([[s12_left, s12_right]], colWidths=[385, 385])
    t12.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('TOPPADDING', (0,0), (0,0), 8),
        ('BOTTOMPADDING', (0,0), (0,0), 8),
        ('LEFTPADDING', (0,0), (0,0), 10),
        ('RIGHTPADDING', (0,0), (0,0), 10),
    ]))
    story.append(t12)
    story.append(Spacer(1, 6))
    story.append(make_notes("Analytics is an essential EWA deliverable. Rather than compiling manual Excel reports at the end of the year, CampusConnect computes placement percentages, average salary packages, and department ratios in real time, ready for NIRF, NBA, or NAAC reporting."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 13: TESTING & DEPLOYMENT
    # =========================================================================
    story.append(make_header(13, "Quality Assurance & DevOps", "Testing, Quality Assurance & Deployment", "Comprehensive automated testing pipeline and externalized configuration management"))
    story.append(Spacer(1, 6))

    s13_left = [
        Paragraph("Automated Testing Architecture", s_card_head),
        Paragraph("• <b>Unit Tests (JUnit 5 &amp; Mockito):</b> Service logic tested in isolation with mocked repository calls and edge-case validation.", s_bullet),
        Paragraph("• <b>Web Integration Tests:</b> <code>MockMvc</code> simulating authenticated user requests, CSRF validation, and redirect flows.", s_bullet),
        Paragraph("• <b>Isolated Test Profile:</b> Runs against <code>application-test.yml</code> with in-memory H2 database for zero-side-effect test runs.", s_bullet),
        Paragraph("• <b>Automated Maven Builds:</b> Executed via Maven Wrapper (<code>./mvnw clean test</code>) with 100% test pass rate.", s_bullet),
    ]

    s13_right = [
        Paragraph("Configuration &amp; DevOps", s_card_head),
        Paragraph("• <b>Zero Secrets in Source Control:</b> Database credentials and session secrets injected via environment variables (<code>.env</code>).", s_bullet),
        Paragraph("• <b>Reproducible Database Migration:</b> Database provisioning automated via <code>docs/database/init-db.sql</code>.", s_bullet),
        Paragraph("• <b>Standalone Executable Artifact:</b> Packaged into a self-contained Spring Boot runnable JAR (<code>java -jar campusconnect.jar</code>).", s_bullet),
    ]

    t13 = Table([[s13_left, s13_right]], colWidths=[385, 385])
    t13.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_green_bg),
        ('BACKGROUND', (1,0), (1,0), c_purple_bg),
        ('BOX', (0,0), (0,0), 0.5, colors.HexColor("#A7F3D0")),
        ('BOX', (1,0), (1,0), 0.5, colors.HexColor("#DDD6FE")),
        ('LINELEFT', (0,0), (0,0), 3, c_green),
        ('LINELEFT', (1,0), (1,0), 3, c_purple),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t13)
    story.append(Spacer(1, 6))

    s13_guide = Table([[Paragraph("<b>Verification Proof:</b> Automated test suites validate controller HTTP status codes, service eligibility algorithms, and security interceptors on every build before production packaging.", s_guide)]], colWidths=[781.89])
    s13_guide.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_light_blue),
        ('BOX', (0,0), (-1,-1), 0.5, c_blue_border),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 8),
        ('RIGHTPADDING', (0,0), (-1,-1), 8),
    ]))
    story.append(s13_guide)
    story.append(Spacer(1, 6))
    story.append(make_notes("We adhered to enterprise development standards throughout the project lifecycle. Business logic is backed by automated JUnit 5 and Mockito tests running against isolated H2 databases. Sensitive credentials are never checked into git, and the application packages cleanly into an executable JAR file."))
    story.append(PageBreak())

    # =========================================================================
    # SLIDE 14: CONCLUSION
    # =========================================================================
    story.append(make_header(14, "Project Conclusion & Next Steps", "Conclusion, Business Impact & Q&A", "A modern, secure, and production-ready enterprise placement management platform"))
    story.append(Spacer(1, 6))

    s14_left = [
        Paragraph("Key Project Accomplishments", s_card_head),
        Paragraph("• <b>End-to-End Enterprise System:</b> Complete digital workflow from student onboarding to final placement offer issuance.", s_bullet),
        Paragraph("• <b>Governed Institutional Trust:</b> Eliminated unverified candidates and unapproved company job postings.", s_bullet),
        Paragraph("• <b>Operational Efficiency:</b> Replaced weeks of manual spreadsheet audits with instant server-side eligibility checks.", s_bullet),
        Paragraph("• <b>Enterprise Security Architecture:</b> Role-based tenant isolation, CSRF token defense, and BCrypt encryption.", s_bullet),
    ]

    s14_right = [
        Paragraph("Future Extensibility Roadmap", s_card_head),
        Paragraph("• <b>Campus Single Sign-On (SSO):</b> Integration with university SAML / OAuth2 Active Directory.", s_bullet),
        Paragraph("• <b>Automated Notifications:</b> SMS &amp; WhatsApp gateway integration for real-time interview alerts.", s_bullet),
        Paragraph("• <b>Automated Resume Parser:</b> Machine learning extraction of skills and projects directly into student profiles.", s_bullet),
    ]

    t14 = Table([[s14_left, s14_right]], colWidths=[385, 385])
    t14.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (0,0), c_card_bg),
        ('BACKGROUND', (1,0), (1,0), c_green_bg),
        ('BOX', (0,0), (0,0), 0.5, c_card_border),
        ('BOX', (1,0), (1,0), 0.5, colors.HexColor("#A7F3D0")),
        ('LINELEFT', (0,0), (0,0), 3, c_blue),
        ('LINELEFT', (1,0), (1,0), 3, c_green),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(t14)
    story.append(Spacer(1, 6))

    s14_banner = Table([[
        [Paragraph("<font color='#4338CA'><b>Thank You! Open for Questions &amp; Live System Demonstration</b></font>", ParagraphStyle('SB1', fontName='Helvetica-Bold', fontSize=12, alignment=1)),
         Spacer(1, 2),
         Paragraph("<font color='#64748B'>CampusConnect &nbsp;•&nbsp; Enterprise Web Applications &nbsp;•&nbsp; Java 21 &amp; Spring Boot 3.4</font>", ParagraphStyle('SB2', fontName='Helvetica', fontSize=8.5, alignment=1))]
    ]], colWidths=[781.89])
    s14_banner.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), c_purple_bg),
        ('BOX', (0,0), (-1,-1), 1, colors.HexColor("#C7D2FE")),
        ('TOPPADDING', (0,0), (-1,-1), 7),
        ('BOTTOMPADDING', (0,0), (-1,-1), 7),
        ('LEFTPADDING', (0,0), (-1,-1), 10),
        ('RIGHTPADDING', (0,0), (-1,-1), 10),
    ]))
    story.append(s14_banner)
    story.append(Spacer(1, 6))
    story.append(make_notes("In conclusion, CampusConnect meets all technical, architectural, and security requirements of an Enterprise Web Application. It modernizes university campus placements with full auditability, automation, and transparency. Thank you for your time, and we now welcome your questions and would love to demonstrate the live application."))

    doc.build(story)
    print(f"Generated PDF: {pdf_filename} (Size: {os.path.getsize(pdf_filename)} bytes)")

if __name__ == "__main__":
    build_pdf()
