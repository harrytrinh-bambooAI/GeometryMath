# 03 — SYSTEM ARCHITECTURE
# Unified AI Education Ecosystem

## 1. Architecture decisions

| Decision | Baseline |
|---|---|
| Deployment | Centralized platform |
| Governance | Federated |
| Application style | Modular monolith |
| Data tenancy | Multi-tenant, tenant-scoped records |
| Frontend | Responsive web portals, role-aware navigation |
| Backend | API + application/domain modules |
| Async processing | Worker processes and queue |
| Primary database | Relational database (PostgreSQL recommended) |
| File storage | Object storage |
| AI | Server-side AI Gateway, provider-agnostic |
| Search | Tenant-filtered retrieval; start with relational/full-text, add vector search where justified |
| Observability | Logs, metrics, traces, audit |

## 2. Logical architecture

```text
                    UNIFIED EDUCATION PLATFORM
 ┌────────────────────────────────────────────────────────────────┐
 │ Experience Layer                                                │
 │ Platform Console | Authority Portal | School Portal             │
 │ Teacher Workspace | Student Workspace | Parent Portal           │
 │ Collaboration Rooms | Public/School News                         │
 └───────────────────────────────┬────────────────────────────────┘
                                 │
 ┌───────────────────────────────▼────────────────────────────────┐
 │ API Gateway / BFF / Identity Context                             │
 │ Authentication | Session | Rate Limit | Request Correlation     │
 └───────────────────────────────┬────────────────────────────────┘
                                 │
 ┌───────────────────────────────▼────────────────────────────────┐
 │ Modular Monolith                                                 │
 │                                                                  │
 │ Identity & Federation       Organization & School                │
 │ Authorization / Policy      Classroom & Curriculum               │
 │ Assignment & Lesson         Submission & File                     │
 │ Knowledge Governance        Assessment & Rubric                   │
 │ AI Orchestrator             Answer Firewall                       │
 │ Student Intelligence        Teacher Copilot                       │
 │ Parent Engagement           Communication & Calendar              │
 │ Collaboration               School Operations                     │
 │ Education Governance        Analytics & Reporting                 │
 │ Audit & Compliance          Integration Hub                       │
 └───────────────────────────────┬────────────────────────────────┘
                                 │
 ┌───────────────────────────────▼────────────────────────────────┐
 │ Infrastructure Adapters                                         │
 │ PostgreSQL | Object Storage | Queue | Search/Vector | Cache      │
 │ AI Providers | Email/Push | Video Provider | External SIS/API     │
 └────────────────────────────────────────────────────────────────┘
```

## 3. Multi-level organization model

```text
Platform
 ├── Education Authority (optional hierarchy, delegated scope)
 │    └── Region / District / Local Unit
 │         └── School Group (optional)
 │              └── School
 │                   ├── Academic Year
 │                   ├── Department / Subject Group
 │                   ├── Class
 │                   │    ├── Teacher Membership
 │                   │    └── Student Membership
 │                   └── School Communities / Events
 └── Platform-wide services and shared standards
```

The exact administrative hierarchy is configurable; it must not hard-code one country's organizational levels.

## 4. Modular monolith boundaries

### Platform foundation
- `identity`: user, authentication, session, recovery.
- `organization`: authority, school group, school, department, academic year.
- `membership`: user-role-scope assignments.
- `authorization`: RBAC + ABAC policy enforcement.
- `federation`: delegated authority, sharing grants, cross-tenant discovery.
- `policy`: policy hierarchy, inheritance, effective dates.

### Learning & teaching
- `classroom`: classes, enrollments, subject, grade.
- `curriculum`: subject/topic/standard mapping.
- `lesson`: lesson plan and content.
- `assignment`: assignment and versioning.
- `submission`: attempt, files, OCR artifacts.
- `rubric`: criteria and versioning.
- `assessment`: AI proposal, teacher review, approved grade.
- `knowledge`: KB ingestion, approval, chunking, grants.
- `answer_firewall`: student AI policy enforcement.
- `student_intelligence`: question history, mastery and progress.
- `teacher_copilot`: teacher-facing generation and workflows.

### Ecosystem
- `parent`: guardian relationship and access.
- `communication`: announcements, chat, notification.
- `calendar`: schedules, due dates, events.
- `collaboration`: groups, rooms, shared workspace.
- `school_operations`: staff, timetable, attendance, events.
- `education_governance`: news, policy, implementation tracking.
- `analytics`: aggregate metrics and reports.
- `integration`: API clients, imports, exports and external mappings.
- `audit`: append-only audit events.
- `safeguarding`: moderation, report, block, incident case.

### Dependency rules
- Modules communicate through application services or domain events.
- A module must not write another module's tables directly.
- Authorization is centralized and invoked on every protected operation.
- AI provider adapters do not own education domain rules.
- Cross-tenant sharing goes through federation grants; never bypasses authorization.
- Audit events are emitted for sensitive state transitions.

## 5. Multi-tenancy strategy

Recommended initial deployment: shared application and shared relational database, with tenant-scoped records and strict backend enforcement.

Required:
- `tenant_id` on all tenant-owned resources.
- Authenticated membership resolves tenant context server-side.
- Tenant filters are mandatory in repositories.
- PostgreSQL Row-Level Security may provide defense in depth.
- File object keys include tenant namespace; access via short-lived signed URLs.
- Queue messages carry tenant and actor context and re-check permissions at execution time.
- Cache keys include tenant and permission scope.
- Search/vector retrieval filters tenant and grants before content reaches the model.
- Automated cross-tenant IDOR tests.
- Cross-school collaboration uses explicit `SharingGrant`, not a global read permission.

For very large organizations or special regulatory needs, evaluate dedicated database/storage isolation as a later option without changing domain contracts.

## 6. Federated policy architecture

Policy hierarchy:
1. Platform mandatory baseline.
2. Authority policy within delegated mandate.
3. School policy within authority/platform constraints.
4. Class/teacher configuration within school constraints.

Policy record fields:
`policy_id`, `issuer_org_id`, `scope_type`, `scope_id`, `policy_type`, `version`, `status`, `effective_from`, `effective_to`, `priority`, `mandatory`, `rules_json`, `supersedes_id`, `published_by`, `approved_by`.

Policy resolution must be deterministic, explainable and auditable. A lower-level policy cannot disable a mandatory upper-level control. Conflicts are surfaced to authorized administrators rather than silently resolved by arbitrary precedence.

## 7. AI architecture

### AI Gateway
- Provider abstraction and secret management.
- Model/task routing (OCR, structured grading, hint, explanation, teacher drafting).
- Prompt template and model version registry.
- Structured output validation.
- Timeouts, retry, rate limit and cost metering.
- Data minimization and provider policy enforcement.
- Evaluation hooks and fallback models.

### AI Orchestrator
Coordinates bounded task workflows:
- OCR/transcription.
- Geometry/math solver.
- Keyword lecture generation.
- Contextual student chat.
- Rubric-based grading.
- Teacher lesson/assessment generation.
- Student insight summarization.

AI output is never trusted by default. It is parsed, validated and checked against business rules.

### Student AI request pipeline

```text
Student request
 → Authenticate user
 → Resolve tenant/class/assignment scope
 → Authorize membership and resource
 → Check assignment state and answer-release policy
 → Retrieve approved teacher KB only
 → Build bounded prompt with untrusted-input isolation
 → Generate response through AI Gateway
 → Validate schema and provenance
 → Check answer leakage / policy compliance
 → Allow / transform / refuse / escalate
 → Store interaction + sources + policy decision + audit
```

### Teacher AI Copilot pipeline
- Teacher selects task and source materials.
- System validates teacher scope and source access.
- AI generates a draft.
- System returns source provenance and warnings.
- Teacher edits and explicitly approves/publishes.
- Every published artifact stores author, reviewer and AI provenance.

## 8. Grading architecture

```text
Submission received
 → File validation + malware scan
 → Preserve original image/document
 → OCR / math transcription
 → Confidence and ambiguity checks
 → Load exact assignment + rubric + KB/reference versions
 → AI criterion-level evaluation
 → Schema validation
 → Deterministic score checks
 → Evidence/provenance validation
 → Confidence/anomaly triage
 → Persist AI proposal
 → Teacher review/edit
 → Teacher approval
 → Release approved feedback
```

AI proposal and final grade are separate domain objects. No background worker may write final grades directly.

## 9. GeometryMathSupport integration

### Preserve existing capabilities
- Text and image problem input; clipboard image paste.
- OCR/transcription into editable problem text.
- `level` selection (THCS/THPT) as existing capability; pilot config initially targets THPT 10–12.
- Structured step-by-step solution.
- Step `principle` modal.
- Clickable `{{keyword}}` and short lecture modal.
- 2D SVG diagrams.
- 3D Three.js scene with vertices, edges, faces, labels.
- 3D rotate/zoom and highlight tools.
- Add line/plane and calculate selected intersections.
- Oxyz axes.
- Contextual chat with short answer and suggested keywords.
- Question-answer journal.
- Problem history, search without diacritic sensitivity, pagination.
- Geometry helper functions: vector operations, plane construction, ray-plane intersection, closest points between lines and plane-plane intersection.

### Replace artifact-specific runtime
- Replace `claude.use('sample')` with backend AI Gateway.
- Replace `claude.use('db')` with tenant-scoped persistent storage.
- Store per-user/per-class history, not shared artifact history.
- Store original image, OCR version and structured result separately.
- Hide or gate complete solutions in student assessment mode according to Answer Firewall.
- Retain teacher-authorized solver mode for lesson preparation and review.

### Known technical debt to preserve in backlog
- Duplicate “Mặt Mặt …” label.
- No free point, midpoint or ratio point creation.
- No drag-handle plane resizing.
- Skew-line edge intersection can be approximated without warning.
- Fan triangulation fails for concave/unsorted polygon faces.
- No automatic hidden-edge rendering.
- No mobile pinch zoom or reset-view control.
- Chat history is not filtered by current problem.
- AI-generated coordinates are not geometrically validated.
- No authentication or per-student isolation in current artifact.
- Current screenshot data uses mock AI/mock DB; not evidence of live AI or production persistence.

## 10. Core data domains

See `04_DATA_MODEL_AND_PERMISSION_MATRIX.md` for entity details. Architecture domains:
- Identity & organization.
- Learning content & curriculum.
- Assignment & assessment.
- Knowledge & AI interaction.
- Student/teacher/parent intelligence.
- Communication & collaboration.
- School operations.
- Governance & reporting.
- Audit, consent and integration.

## 11. API and integration principles

- Versioned REST/JSON APIs initially; event contracts for asynchronous workflows.
- API derives tenant context from authenticated membership.
- No client-supplied tenant ID is trusted without server validation.
- Idempotency keys for submissions, payments if later introduced, and long-running actions.
- Outbox pattern for reliable event publication.
- Webhooks are signed, replay-protected and scoped.
- External integrations use service accounts with least privilege and explicit data contracts.

Illustrative API groups:
- `/v1/identity`, `/v1/organizations`, `/v1/memberships`
- `/v1/schools`, `/v1/classes`, `/v1/curriculum`
- `/v1/assignments`, `/v1/submissions`, `/v1/rubrics`, `/v1/grades`
- `/v1/knowledge`, `/v1/ai/ask`, `/v1/ai/grading`
- `/v1/students/:id/learning-profile`
- `/v1/parents/children`
- `/v1/groups`, `/v1/messages`, `/v1/events`
- `/v1/governance/policies`, `/v1/governance/implementation`
- `/v1/reports`, `/v1/integrations`

## 12. Deployment topology

Deployable processes:
- Web frontend.
- Backend API (modular monolith).
- Worker process (same codebase, worker profile).
- PostgreSQL.
- Object storage.
- Queue/cache.
- Search index (optional at MVP).
- Observability.

Environments:
- Local.
- Development.
- Staging / pilot rehearsal.
- Production.

Use containerized deployment, managed secrets, automated backup, migration strategy, health checks, feature flags, rollback and disaster recovery rehearsal.

## 13. Security, privacy and safeguarding

- Encryption in transit and at rest.
- Secrets never shipped to browser.
- Short-lived signed file URLs.
- File scanning and safe document parsing.
- Rate limits by user, tenant and endpoint.
- Audit role changes, grade approvals, KB approvals, policy publication, access denials and privileged support access.
- Data minimization for external AI providers.
- Separate operational admin access from academic content access.
- Parent relationship verification and revocation.
- Child-safe defaults for messaging and video.
- Moderation/report/block and escalation before opening student social features.
- Retention, deletion, export, consent and breach response policy before production.

## 14. Service extraction criteria

Do not start with microservices. Consider extraction only when:
- AI workloads need independent scaling or isolation.
- File/OCR processing competes with API resources.
- Teams require independent release cycles.
- A module has distinct compliance/availability boundary.
- Measured bottlenecks justify operational complexity.

Likely extraction candidates: AI workers, file processing, notifications/video integrations. Keep identity, authorization and policy semantics consistent.
