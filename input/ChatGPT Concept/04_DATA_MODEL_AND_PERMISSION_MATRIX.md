# 04 — DATA MODEL & PERMISSION MATRIX

## 1. Modeling principles

1. Every education resource has owner, tenant, scope and lifecycle.
2. Organization hierarchy is configurable, not hard-coded to one national structure.
3. Users and roles are separate: a person can hold multiple scoped memberships.
4. Student data is private by default.
5. Cross-school access is explicit, purpose-limited, time-limited and audited.
6. AI artifacts are first-class records with model/prompt/source provenance.
7. Published assignment, rubric, KB and policy versions are immutable.
8. Derived insights retain evidence references and confidence.

## 2. Organization and identity entities

| Entity | Key fields | Purpose |
|---|---|---|
| User | id, display_name, contact, status | Global person identity |
| AuthIdentity | id, user_id, provider, subject_id | Login identity |
| Organization | id, parent_id, org_type, name, status | Generic hierarchy node |
| Tenant | id, organization_id, isolation_mode, settings | Data boundary |
| SchoolProfile | id, organization_id, branding, public_profile | School identity |
| AuthorityDelegation | id, issuer_org_id, target_org_id, scope_json, valid_from/to | Delegated authority |
| Membership | id, user_id, organization_id, role_id, scope_json, status | Scoped membership |
| Role | id, name, role_type, permission_bundle | Role definition |
| Permission | id, resource, action, constraints | Atomic permission |
| Policy | id, issuer_org_id, scope, version, rules_json, effective dates | Federated policy |
| GuardianRelationship | id, guardian_user_id, student_user_id, verification_status, permissions | Parent access |
| ConsentRecord | id, subject_id, purpose, scope, status, timestamp | Consent/notice evidence |

## 3. Academic entities

| Entity | Key fields |
|---|---|
| AcademicYear | id, tenant_id, school_id, name, start_date, end_date |
| Department | id, tenant_id, school_id, name |
| Subject | id, code, name, grade_band |
| CurriculumStandard | id, authority_scope, subject_id, grade, topic, standard_code |
| Class | id, tenant_id, school_id, academic_year_id, subject_id, grade, name |
| ClassMembership | id, class_id, user_id, role, status, joined_at |
| Lesson | id, tenant_id, class_id, owner_id, status |
| LessonVersion | id, lesson_id, version, content_json, source_refs |
| CalendarEvent | id, tenant_id, scope, event_type, starts_at, ends_at |
| AttendanceRecord | id, class_id, student_id, event_id, status, recorded_by |

## 4. Learning and assessment entities

| Entity | Key fields | Notes |
|---|---|---|
| Assignment | id, tenant_id, class_id, owner_id, status | Stable aggregate |
| AssignmentVersion | id, assignment_id, version, content_json, release_policy, published_at | Immutable |
| AssignmentResource | id, assignment_version_id, resource_id, resource_version_id | Allowed resources |
| Rubric | id, tenant_id, owner_id, title | Stable aggregate |
| RubricVersion | id, rubric_id, version, criteria_json, max_score | Immutable |
| RubricCriterion | id, rubric_version_id, name, description, max_points, scoring_guidance | Criterion |
| Submission | id, tenant_id, assignment_version_id, student_id, current_version, status | Student attempt aggregate |
| SubmissionVersion | id, submission_id, version, submitted_at, text_content | Immutable attempt |
| SubmissionFile | id, submission_version_id, object_key, checksum, mime_type | Original file |
| OCRArtifact | id, submission_version_id, extracted_text, confidence, engine_version | Derived artifact |
| GradingJob | id, submission_version_id, status, attempts, queued_at | Async work |
| GradingProposal | id, grading_job_id, rubric_version_id, result_json, model_version, prompt_version | AI suggestion |
| CriterionAssessment | id, proposal_id, criterion_id, proposed_points, evidence_json, confidence | Per criterion |
| ApprovedGrade | id, submission_id, approved_by, score, feedback, version, released_at | Teacher final |
| GradeReview | id, approved_grade_id, requested_by, status, resolution | Appeal/review |

## 5. Knowledge and AI entities

| Entity | Key fields | Notes |
|---|---|---|
| KnowledgeItem | id, tenant_id, owner_id, subject, grade, topic, scope, status | Logical content |
| KnowledgeVersion | id, knowledge_item_id, version, content_hash, approval, effective dates | Immutable |
| KnowledgeChunk | id, knowledge_version_id, chunk_text, locator, embedding_ref | Retrieval unit |
| KnowledgeGrant | id, knowledge_version_id, grantee_scope, purpose, valid_to | Explicit access |
| AssignmentKnowledgeGrant | id, assignment_version_id, knowledge_version_id | Assignment allowlist |
| AIInteraction | id, tenant_id, actor_id, context_type, context_id, question, answer, policy_decision | Question history |
| AIRetrievalTrace | id, interaction_id, source_chunk_ids, ranking_metadata | Provenance |
| AIPolicyDecision | id, interaction_id, policy_version, decision, reason_codes | Firewall result |
| AIModelRun | id, task_type, provider, model, prompt_version, latency, token/cost metadata | AI trace |
| KeywordLecture | id, tenant_id, slug, term, lecture, source_refs, version | Reusable concept lecture |
| LearningSignal | id, student_id, concept_id, signal_type, evidence_refs, confidence | Observed learning signal |
| MasterySnapshot | id, student_id, subject_id, concept_id, level_estimate, confidence, calculated_at | Estimate |
| Intervention | id, student_id, teacher_id, signal_refs, action, follow_up_at, outcome | Teacher action |

## 6. Communication, collaboration and governance entities

| Entity | Key fields |
|---|---|
| Announcement | id, issuer_id, scope, category, content, status, published_at |
| Notification | id, recipient_id, event_id, channel, status, delivered_at |
| Conversation | id, scope, conversation_type, created_by, moderation_policy |
| ConversationMember | id, conversation_id, user_id, role, joined_at |
| Message | id, conversation_id, sender_id, content, moderation_state, created_at |
| StudyGroup | id, owner_id, school_id, visibility, purpose, status |
| GroupMembership | id, group_id, user_id, role, status |
| CollaborationArtifact | id, group_id, artifact_type, object_key, version |
| SafetyReport | id, reporter_id, target_type, target_id, reason, status |
| ModerationCase | id, report_id, assigned_to, action, resolution |
| SchoolPartnership | id, source_school_id, target_school_id, scope, status |
| SharingGrant | id, owner_org_id, recipient_org_id, resource_id, purpose, expiry |
| OfficialPolicyNotice | id, issuer_org_id, policy_version_id, audience_scope, effective_at |
| ImplementationReport | id, school_id, notice_id, status, evidence_refs, submitted_at |
| AuditEvent | id, tenant_id, actor_id, action, resource_type, resource_id, timestamp, metadata |
| IntegrationClient | id, organization_id, scopes, credentials_ref, status |
| ExternalIdMapping | id, source_system, external_id, internal_entity_type, internal_id |

## 7. Required metadata for protected resources

- `tenant_id`
- `owner_org_id`
- `owner_user_id`
- `visibility_scope`
- `allowed_roles`
- `resource_status`
- `created_by`, `created_at`, `updated_at`
- `version` / `version_id`
- `effective_from`, `effective_to` where applicable
- `sharing_grant_id` where cross-org
- `consent_reference` where required
- `audit_reference`

## 8. Permission matrix

Legend:
- **Own:** own resource.
- **Class:** class assigned to teacher / enrolled student.
- **School:** school scope.
- **Delegated:** explicit authority delegation.
- **Assigned:** explicit admin/security task.
- **No:** no default access.

| Resource/action | Platform Admin | Authority | School Admin | Teacher | Student | Parent |
|---|---|---|---|---|---|---|
| Platform config | Full | No | No | No | No | No |
| Authority policy | Platform governance | Delegated | No | No | No | No |
| School profile | Support only | Read scoped | Manage own | Limited view | Public/own school | Public/child school |
| School membership | Support break-glass | Delegated oversight | Manage own | View assigned class | Own membership | Child relationship |
| Class roster | No default content access | Aggregate/delegated | School scope | Assigned class | Own class context | Child class context |
| Assignment draft | No | No | No default | Own/class | No | No |
| Published assignment | No | Aggregate | School scope if delegated | Class | Enrolled class | Child's class if allowed |
| Teacher KB draft | No | No | No default | Own | No | No |
| Approved KB | No | Authority-published scope | School-granted | Class/granted | Assignment-granted | No default |
| Submission | No default | Aggregate or delegated | Limited policy scope | Assigned class | Own | Child-approved view only |
| AI grading proposal | No | No | No default | Assigned class | No | No |
| Approved grade | No default | Aggregate/delegated | Authorized academic scope | Assigned class | Own | Child-approved view |
| Student question history | No default | Aggregate/delegated | Policy-defined | Assigned students | Own | No default |
| Student intelligence profile | No default | Aggregate/delegated | Authorized academic scope | Assigned students | Own | Verified child, scoped |
| Guardian relationship | No default | No | Verify per policy | Limited awareness | Own status | Own relationship |
| Study group | No | No | School moderated scope | Groups they moderate | Member groups | No default |
| Cross-school resource | No | Delegated policy | Explicit school grant | Owner/granted | Granted context | No default |
| Official notice | Platform issuer | Issuer/delegated | Receive/acknowledge | Receive | Receive if audience | Receive if audience |
| Implementation report | Platform audit | Delegated schools | Submit own | Contribute if assigned | No | No |
| Audit log | Security scope | Delegated scope | Own school scope | Own actions/relevant | Own relevant events | Own relevant notices |

**Critical rule:** Platform Admin does not automatically receive unrestricted academic content access. Break-glass access must be purpose-bound, time-limited and audited.

## 9. Access decision algorithm

For every request:
1. Authenticate actor.
2. Resolve active membership(s).
3. Resolve tenant and organization scope.
4. Check role permission.
5. Check resource ownership and visibility.
6. Check relationship (teacher-class, student-class, guardian-child, authority-school).
7. Check resource state and effective dates.
8. Check policy inheritance and explicit grants.
9. Check consent/notice requirements where applicable.
10. Log sensitive access and deny by default on ambiguity.

## 10. Data classification

| Class | Examples | Controls |
|---|---|---|
| Public | Public school profile, published public news | Public read, moderation |
| Internal | Internal announcements, school operating info | Organization membership |
| Confidential | Student assignments, grades, teacher notes | Strict role/scope authorization |
| Highly sensitive | Child identity/contact, safeguarding cases, special support records | Need-to-know, restricted audit, minimized processing |

Special-category data should not be collected unless required, lawful and specifically governed.

## 11. Retention and deletion

Retention periods are TBD by legal and school policy. Design must support:
- Record-level retention class.
- Legal hold where applicable.
- User/guardian access and correction workflows.
- Export of authorized records.
- Deletion/anonymization with audit.
- Preservation of integrity where academic record retention is legally required.
