# 02 — MASTER PRODUCT REQUIREMENTS DOCUMENT
# Unified AI Education Ecosystem

**Status:** Product baseline / Proposed  
**MVP pilot:** Mathematics Grades 10–12  
**Architecture:** Centralized platform, modular monolith, multi-tenant  
**Governance:** Federated  
**AI governance:** Teacher-controlled KB + Answer Firewall

---

## 1. Purpose

Master PRD defines the target capabilities and business rules for the complete ecosystem. It includes MVP requirements and later-phase requirements, clearly marked to prevent future vision from being mistaken for first-release scope.

Priority:
- **P0:** Must-have for MVP or critical platform foundation.
- **P1:** Next release / early ecosystem.
- **P2:** Later ecosystem capability.
- **P3:** Advanced capability / subject to discovery.

## 2. Actors & roles

- Platform Super Admin
- Platform Operations / Security Admin
- Education Authority Admin
- Regional / District Authority Officer
- School Owner / School Admin
- Principal / School Leadership
- Academic Coordinator
- Teacher
- Student
- Parent / Guardian
- Group Moderator / Safeguarding Officer
- External Integration Client (service identity)

A user may have multiple scoped roles. Role assignment alone does not grant global access; each action must also pass scope, relationship, state and policy checks.

## 3. Functional Requirements

### Domain A — Identity, Organization & Federation

**ORG-001 [P0]** Platform supports a hierarchy of platform → authority (optional) → school group (optional) → school → academic year → class.

**ORG-002 [P0]** Tenant boundary is explicit and every protected resource has tenant ownership.

**ORG-003 [P0]** User may hold multiple memberships with different roles and scopes.

**ORG-004 [P0]** Authority delegation is explicit: delegated region, schools, data categories, actions and validity period.

**ORG-005 [P1]** School can configure logo, name, theme, portal identity, terminology and allowed local settings.

**ORG-006 [P1]** School policy inherits platform and authority policies; lower-level policies cannot override mandatory higher-level safeguards.

**ORG-007 [P2]** School-to-school sharing requires explicit sharing grant, recipient scope, purpose, expiry and audit record.

**ORG-008 [P2]** Platform supports federation registry and organization discovery without exposing private student data.

**Acceptance**
- A school admin cannot access another school's private records by changing identifiers.
- Authority users see only assigned scopes and approved aggregate/identified datasets.
- School identity configuration does not alter platform security standards.

### Domain B — Teacher & Student Learning Platform

**LMS-001 [P0]** Teacher creates and manages classes, subjects, grade levels and student enrollments.

**LMS-002 [P0]** Teacher creates assignments from typed text and uploaded images/PDF/documents.

**LMS-003 [P0]** Existing GeometryMathSupport problem-solving experience is available in teacher-authorized contexts and adapted for controlled student use.

**LMS-004 [P0]** Teacher can attach learning objectives, topic tags, due date, instructions and allowed resources.

**LMS-005 [P0]** Student sees only assignments assigned to their active class membership.

**LMS-006 [P0]** Student can submit typed work and multiple images of handwritten work.

**LMS-007 [P0]** Original submission artifacts are preserved; OCR output is a derived, correctable representation.

**LMS-008 [P0]** Student can see submission state and approved feedback.

**LMS-009 [P1]** Teacher can create lesson plans and reusable learning sequences.

**LMS-010 [P1]** Calendar combines assignments, class events, due dates and school announcements.

**LMS-011 [P2]** Student portfolio stores selected work and learning reflections.

### Domain C — Assessment, Rubric & AI Grading

**ASM-001 [P0]** Teacher creates rubric criteria, descriptions, levels, maximum points, partial-credit guidance and feedback descriptors.

**ASM-002 [P0]** Rubric version is immutable once used by a published assignment.

**ASM-003 [P0]** AI grading evaluates each criterion separately and returns proposed points, rationale, evidence references, confidence and uncertainty flags.

**ASM-004 [P0]** System validates score ranges, total calculation and schema deterministically.

**ASM-005 [P0]** AI proposal cannot be published as final grade without authorized teacher approval.

**ASM-006 [P0]** Teacher can accept, edit, reject and comment on each criterion proposal.

**ASM-007 [P0]** Approved grade is versioned and auditable.

**ASM-008 [P0]** Teacher controls release timing of score, answer key and detailed solution.

**ASM-009 [P1]** Support regrade request and teacher-managed review/appeal workflow.

**ASM-010 [P1]** Class-level analysis groups performance by criterion and topic.

**ASM-011 [P2]** Moderation workflow supports second marker or academic coordinator review.

**Acceptance**
- The system never exposes an unapproved score as final.
- Each proposal references exact assignment, rubric, submission and model/prompt versions.
- A teacher override records before/after values and actor.

### Domain D — Teacher-controlled Knowledge Governance

**KB-001 [P0]** Teacher can create/import knowledge items from text, image, PDF, document, lesson plan or approved learning material.

**KB-002 [P0]** Each item has subject, grade, topic, curriculum scope, owner, status, version and visibility.

**KB-003 [P0]** Only approved and effective KB versions are retrievable by student-facing AI.

**KB-004 [P0]** Teacher can bind specific KB versions to class or assignment scope.

**KB-005 [P0]** Retrieval records source chunk/version used in each AI response.

**KB-006 [P1]** School may curate a school-level library with explicit approval and sharing policy.

**KB-007 [P2]** Authority may publish official curriculum or policy resources to designated scopes.

**KB-008 [P2]** Content sharing between schools is opt-in and licensed/permissioned.

**Acceptance**
- Draft, pending, archived, expired or unauthorized KB content is excluded.
- A student cannot query teacher-only notes or another class's KB.
- Every grounded response can be traced to the approved source version.

### Domain E — Answer Firewall & Student AI

**AF-001 [P0]** Every student-facing AI request is checked server-side for identity, tenant, class, assignment and state.

**AF-002 [P0]** Firewall checks answer-release schedule and allowed response type.

**AF-003 [P0]** Student AI uses only teacher-approved, in-scope KB sources for instructional claims.

**AF-004 [P0]** During active assessment, AI provides teacher-configured hints, clarification questions or concept explanations, not prohibited final answers.

**AF-005 [P0]** AI must not reveal answer keys, hidden rubric notes, teacher-only resources or another student's work.

**AF-006 [P0]** Generated response passes grounding and answer-leakage validation before delivery.

**AF-007 [P0]** When no approved source exists, AI transparently declines to provide authoritative instruction and directs the student to teacher/approved resource.

**AF-008 [P0]** Teacher can inspect student question history within assigned class/subject scope, subject to published school policy.

**AF-009 [P1]** Teacher can configure hint depth and allowed help modes by assignment.

**AF-010 [P1]** After approved return/release, AI may explain teacher-approved feedback and solution.

**Acceptance**
- Zero critical answer disclosures in release-blocking adversarial test set.
- No unapproved source is passed to the model in student context.
- Every allow/block/escalate decision is logged with policy version.

### Domain F — Student Intelligence & Progress

**SI-001 [P0]** System records question, answer, assignment context, time, source and policy decision.

**SI-002 [P0]** Teacher can view full question history for students in classes they teach, with search/filter by assignment, topic and time.

**SI-003 [P1]** System tags observed errors and questions to curriculum concepts with confidence and evidence.

**SI-004 [P1]** Student profile shows learning timeline, completed work, approved results, strengths and areas to practice.

**SI-005 [P1]** Teacher sees evidence-based class and individual insights, not unsupported personality labels.

**SI-006 [P1]** Teacher can record interventions, follow-up dates and observed outcomes.

**SI-007 [P2]** Student receives personalized study recommendations based on approved data and teacher context.

**SI-008 [P2]** Learning profile supports subject-specific mastery maps and longitudinal comparisons.

**Acceptance**
- Every insight links to evidence or is explicitly marked as an estimate.
- The system does not describe a student as inherently weak, lazy or incapable.
- Students and authorized guardians can see relevant profile information under policy.

### Domain G — Teacher AI Copilot

**TC-001 [P1]** Teacher can generate lesson/teaching-plan drafts from prompt, image, document, PDF, existing lesson plan or selected KB.

**TC-002 [P1]** Teacher can generate question sets, answer keys, rubrics and differentiated practice from teacher-provided materials.

**TC-003 [P1]** Copilot can summarize class submissions and common misconceptions.

**TC-004 [P1]** Copilot can draft parent reports, announcements, reminders and class messages for teacher review.

**TC-005 [P1]** Copilot can prioritize grading tasks, pending work and follow-up reminders.

**TC-006 [P1]** Copilot must show source materials used and allow teacher editing before publication.

**TC-007 [P2]** Copilot can propose differentiated lesson variants based on observed class needs.

**Acceptance**
- Copilot never sends messages, publishes lessons or changes grades without explicit teacher action.
- Drafts are labeled as AI-generated and editable.
- Source provenance is visible.

### Domain H — Parent & Guardian Engagement

**PG-001 [P1]** Parent account relationship to student is verified through an approved school process.

**PG-002 [P1]** Parent can view permitted child's assignments, attendance (if available), approved grades, progress and upcoming schedule.

**PG-003 [P1]** Parent receives school/teacher announcements and important reminders.

**PG-004 [P1]** Parent can view teacher-approved strengths and practice areas with context.

**PG-005 [P1]** Parent can communicate with teacher through school-approved channels and quiet-hour policy.

**PG-006 [P2]** Parent can acknowledge notices, schedule meetings and access support resources.

**Acceptance**
- Guardian access is revoked when relationship or authorization ends.
- Parent cannot access another child's information without a verified relationship and explicit permission.
- Sensitive teacher-only notes and private peer interactions are not exposed.

### Domain I — Communication, Calendar & Notifications

**COM-001 [P0]** Teacher can publish class announcement and basic reminders.

**COM-002 [P0]** System notifies students about assignment publication, due dates and returned work.

**COM-003 [P1]** Calendar combines personal learning tasks, class schedule, school events and deadlines.

**COM-004 [P1]** Notifications support user preferences, quiet hours and delivery status.

**COM-005 [P2]** Direct/group messaging supports permission, moderation, reporting and retention rules.

**COM-006 [P2]** Group calls/video are limited to authorized rooms with safeguarding controls.

### Domain J — Study Groups & Collaboration Network

**COL-001 [P2]** Student/teacher can create or join moderated study groups under school policy.

**COL-002 [P2]** Group supports chat, shared resources, assignments and schedule.

**COL-003 [P2]** Group video/call requires moderator/teacher controls, participant visibility and safety reporting.

**COL-004 [P2]** Collaborative workspace supports whiteboard/document sharing.

**COL-005 [P2]** AI group assistant is scoped to approved group resources and learning objectives.

**COL-006 [P2]** Group progress reflects contribution and task completion without simplistic ranking.

**COL-007 [P2]** Cross-school groups require explicit school participation and sharing grants.

### Domain K — School Operating System

**SCH-001 [P2]** School admin manages school profile, organization units, staff and roles.

**SCH-002 [P2]** School manages students, classes, academic years and enrollments.

**SCH-003 [P2]** School manages timetable, calendar, attendance and events.

**SCH-004 [P2]** School manages curriculum mapping and academic plans.

**SCH-005 [P2]** School publishes notices and operational communications.

**SCH-006 [P2]** School dashboard provides scoped academic and operational reports.

**SCH-007 [P2]** School configures identity and local policies within federated constraints.

### Domain L — School Discovery & Knowledge Exchange

**NET-001 [P2]** School profile presents approved public information, activities, achievements and collaboration interests.

**NET-002 [P2]** Teachers can discover peers and opt-in communities by subject/topic.

**NET-003 [P2]** Schools can publish collaboration opportunities and inter-school events.

**NET-004 [P2]** Resource exchange uses owner, license, visibility, approval and expiry metadata.

**NET-005 [P2]** School-to-school study groups and projects require explicit participating-school approval.

### Domain M — Education Governance & Authority

**GOV-001 [P2]** Authority can publish official news, policy, guidance and implementation notices to scoped audiences.

**GOV-002 [P2]** Authority can define required reporting templates and deadlines.

**GOV-003 [P2]** Schools submit implementation status and evidence according to delegated policy.

**GOV-004 [P2]** Authority dashboard shows scoped aggregate progress and exceptions.

**GOV-005 [P2]** Platform supports policy versioning, effective dates, acknowledgment and audit.

**GOV-006 [P2]** Authority can discover school activities and best practices only within approved visibility.

**GOV-007 [P2]** Policy distribution is traceable: issuer, recipient scope, publication time, effective time, acknowledgment and status.

**Acceptance**
- Authority user cannot browse arbitrary student-level records unless explicitly authorized by legal mandate and policy.
- Official notices are distinguishable from school news and user-generated posts.
- Policy status can be traced across schools without conflating acknowledgment with actual implementation.

### Domain N — Analytics, Reporting & Interoperability

**ANA-001 [P0]** MVP provides assignment completion, grading queue and criterion-level class reporting.

**ANA-002 [P1]** Parent/student progress reporting uses approved grades and evidence.

**ANA-003 [P2]** School dashboard aggregates academic and operational indicators.

**ANA-004 [P2]** Authority dashboard aggregates only authorized scopes and privacy-protected metrics.

**INT-001 [P0]** Stable internal API contracts and event schemas are versioned.

**INT-002 [P2]** Platform supports secure integrations with school identity, SIS, LMS and education registries.

**INT-003 [P2]** Data exchange uses explicit purpose, scope, field mapping and audit.

## 4. Cross-cutting Non-Functional Requirements

### Security & privacy
- Tenant isolation enforced server-side.
- Least privilege, scoped role assignment and resource-level authorization.
- Encryption in transit and at rest.
- Sensitive actions are audited.
- Signed URLs are short-lived and issued after authorization.
- Student data sent to model providers is minimized.
- Retention, deletion, export and consent policies are defined before production.

### Reliability & performance
- Submission persistence must be durable.
- AI jobs are asynchronous, retry-safe and idempotent.
- Class dashboards use pagination and bounded queries.
- System exposes service health, queue depth, error rate and AI latency.
- Initial availability/SLO targets are established after pilot load testing.

### Accessibility & localization
- Vietnamese-first UX.
- Responsive desktop/tablet/mobile.
- Keyboard access and readable contrast.
- Mathematical notation accessible where feasible.
- Error messages actionable and age-appropriate.

### AI quality
- Version prompts, model, retrieval config and evaluation dataset.
- Track hallucination, grounding, refusal and override rates.
- Maintain regression suite by subject/topic.
- AI uncertainty must be surfaced; no hidden auto-finalization.

## 5. MVP User Stories & Acceptance Summary

### Teacher
- As a teacher, I can create a class and enroll students.
- As a teacher, I can create an assignment from text/image/document.
- As a teacher, I can define a rubric and answer-release rule.
- As a teacher, I can approve KB material and grant it to an assignment.
- As a teacher, I can review AI grading with evidence and approve the final result.
- As a teacher, I can inspect student questions in my classes.

### Student
- As a student, I can view my assignments and deadlines.
- As a student, I can submit text and photos of handwritten work.
- As a student, I can receive hints grounded in approved teacher materials.
- As a student, I can view teacher-approved feedback and learning history.

### Platform / tenant admin
- As an admin, I can provision a tenant and manage scoped memberships.
- As an admin, I can investigate security events within assigned scope without default unrestricted access to student content.

## 6. MVP Definition of Done

A feature is not Done until:
1. Functional requirements implemented.
2. Permission and tenant isolation tests pass.
3. Error and empty states are designed.
4. Audit events exist for sensitive transitions.
5. Unit/integration/E2E tests pass.
6. AI-enabled behavior has evaluation cases and safe fallback.
7. Analytics events are instrumented.
8. Documentation and support runbook updated.
9. Accessibility and responsive behavior reviewed.
10. Product owner and pilot teacher representative accept the workflow.
