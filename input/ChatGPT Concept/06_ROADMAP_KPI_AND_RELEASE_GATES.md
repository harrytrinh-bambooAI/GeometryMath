# 06 — ROADMAP, KPI & RELEASE GATES

## 1. Roadmap principles

- Whole ecosystem architecture and data foundations from day one.
- MVP remains narrow: Teacher + Student + AI Grading.
- Pilot is Mathematics Grades 10–12.
- AI grading starts in shadow mode before routine use.
- Teacher approval remains mandatory.
- Expansion decisions depend on pilot evidence and operational readiness.

## 2. 36-month ecosystem roadmap

| Phase | Timing | Product outcome |
|---|---|---|
| 0. Discovery & design | Month 0–1 | Validated workflows, product architecture, policy and evaluation plan |
| 1. Platform foundation | Months 2–3 | Identity, organization, tenant, roles, class, audit, files |
| 2. Teacher workspace | Months 4–5 | Assignment Studio, rubric, teacher KB, publishing |
| 3. Student workspace | Months 6–7 | Assignment access, text/image submission, OCR, status |
| 4. AI assessment | Months 8–9 | AI grading proposal, review queue, teacher approval, Answer Firewall |
| 5. Pilot readiness & pilot | Months 10–12 | Controlled math pilot, quality/safety evaluation, iteration |
| 6. Student intelligence & parent | Months 13–18 | Question history insights, progress profile, guardian portal, calendar |
| 7. Collaboration network | Months 19–24 | Study groups, chat, video, shared workspace, cross-school pilots |
| 8. School Operating System | Months 25–30 | School admin, timetable, attendance, curriculum, events, school dashboards |
| 9. Education Governance | Months 31–36 | Policy/news distribution, implementation tracking, authority dashboards, interoperability |

This is a planning sequence, not a delivery guarantee. Phase duration must be re-estimated after discovery and pilot.

## 3. Detailed MVP delivery plan

### Phase 0 — Discovery (Weeks 1–4)
- Interview teachers and students.
- Observe assignment and grading.
- Collect permitted sample math work and rubrics.
- Establish manual grading baseline.
- Confirm data controller/processor, privacy and hosting assumptions.
- Define adversarial Answer Firewall tests.
- Deliver: validated flows, scope, architecture decisions, prototype, AI evaluation plan.

### Phase 1 — Foundation (Weeks 5–10)
- Auth, tenant, school, membership, class.
- Role and scope authorization.
- File service and audit.
- Base teacher/student portals.
- CI/CD, monitoring, backups.
- Exit: tenant isolation tests pass; roles cannot access unauthorized classes.

### Phase 2 — Teacher workspace (Weeks 11–16)
- Class roster.
- Assignment authoring/import/OCR correction.
- Rubric builder and versioning.
- Teacher KB, approval, grants.
- Answer release policy.
- Exit: teacher can publish a versioned assignment with valid rubric and approved sources.

### Phase 3 — Student workspace (Weeks 17–22)
- Student assignment list.
- Text/image submission.
- OCR artifact and correction.
- Submission versioning.
- Notifications/status.
- Exit: original evidence persists; students cannot view peers' work.

### Phase 4 — AI grading & firewall (Weeks 23–30)
- AI gateway and prompt registry.
- Criterion-level grading.
- Evidence and confidence.
- Teacher review/override/approval.
- Student AI hint flow and firewall.
- Question history.
- Exit: no auto-final grades; adversarial answer-leakage suite passes.

### Phase 5 — Pilot readiness (Weeks 31–36)
- Security review and penetration test.
- AI evaluation against teacher-adjudicated samples.
- OCR math review.
- Accessibility/responsive review.
- Backup/restore rehearsal.
- Teacher training and support runbooks.
- Exit: zero open critical security issues and pilot sign-off.

### Phase 6 — Controlled pilot (Weeks 37–44)
- Start with 3–5 classes, trained teachers, selected math units.
- Shadow grading first.
- Compare AI proposals with teacher scores.
- Enable teacher-reviewed proposals after quality review.
- Weekly feedback and defect triage.
- Exit: acceptable quality, teacher trust, no critical safety issues.

### Phase 7 — Stabilize (Weeks 45–52)
- Resolve pilot friction.
- Expand rubric/topic coverage.
- Improve insight quality and cost.
- Decide next investment based on evidence.

## 4. Pilot KPI framework

Targets below are hypotheses for pilot planning. Baseline must be measured; targets may be adjusted after discovery.

| Dimension | KPI | Initial target / approach |
|---|---|---|
| Teacher activation | Teachers publish first assignment within 7 days | ≥70% |
| Student activation | Enrolled students submit at least one assignment | ≥75% |
| Teacher productivity | Median review time vs manual baseline | 25–35% reduction hypothesis |
| AI grading | Criterion-level agreement with adjudicated teacher score | Establish baseline by topic; gate before expansion |
| Teacher trust | Proposal acceptance/override rate and qualitative trust | Track by criterion and topic |
| OCR | Correction rate and symbol-level error | Establish baseline; block low-confidence auto-use |
| Answer safety | Critical answer leakage | 0 critical incidents in release test set |
| Grounding | Student AI responses with approved provenance | ≥95% |
| Reliability | Successful durable submission persistence | ≥99.5% |
| Workflow | Published assignments returned with teacher approval | ≥80% |
| Student feedback | Students view returned feedback | ≥70% |
| Privacy | Cross-tenant unauthorized access | 0 |
| Operational | AI job failure/retry and queue delay | Set SLO after load test |

### KPI definitions
- **Teacher review time:** active time spent reviewing an AI proposal and finalizing a submission, compared with manual baseline for comparable work.
- **Criterion agreement:** proportion of criterion-level AI proposals within an agreed tolerance of adjudicated teacher score; tolerance is rubric-specific and must be defined before evaluation.
- **Grounded response:** response has at least one valid approved KB source supporting its instructional claims.
- **Critical leakage:** disclosure of prohibited final answer, answer key, teacher-only content or peer data under a policy state where disclosure is forbidden.

## 5. Ecosystem KPI

### Student learning
- Concept mastery progression.
- Repeat error reduction.
- Assignment completion and revision rate.
- Time-to-feedback.
- Student confidence/feedback usefulness (survey).

### Teacher effectiveness
- Preparation and grading time saved.
- AI draft edit/override rate.
- Teacher Copilot task completion.
- Teacher intervention effectiveness.

### Parent engagement
- Verified guardian activation.
- Meaningful progress views.
- Parent-teacher communication resolution time.
- Notification usefulness vs mute/opt-out.

### School operations
- Digitized workflow coverage.
- Data completeness and accuracy.
- Announcement reach and acknowledgment.
- Timetable/attendance/curriculum adoption.

### Network collaboration
- Active cross-school partnerships.
- Study group participation and completion.
- Shared resource reuse with valid grants.
- Inter-school project completion.

### Governance
- Policy distribution reach.
- Acknowledgment completion.
- Implementation status completeness.
- Time to identify and resolve implementation blockers.
- Data quality by organization level.

### Trust and safety
- Unauthorized access incidents.
- Answer leakage incidents.
- Moderation/report response time.
- Guardian relationship verification exceptions.
- Data export/deletion SLA.

## 6. Release gates

### Gate A — Internal Alpha
- Core teacher-student loop works.
- Tenant and role isolation tests pass.
- Assignment/rubric/submission versioning works.
- AI grading output schema valid.
- Teacher approval is mandatory.
- Answer Firewall basic tests pass.

### Gate B — Controlled Pilot
- Security review has no open critical findings.
- Answer Firewall adversarial test set passes agreed threshold.
- AI grading shadow evaluation completed on representative math data.
- Teacher can inspect original submission and AI evidence.
- Operational monitoring and rollback exist.
- Privacy notices, support and incident process ready.

### Gate C — Pilot Expansion
- Teacher review time improves without unacceptable quality degradation.
- AI disagreement understood by topic/rubric.
- Student feedback is useful and comprehensible.
- No critical safety incident.
- Support load manageable.
- School leadership approves expanded cohort.

### Gate D — Ecosystem Phase
- Core learning platform stable.
- Identity, consent, audit and policy model supports new roles.
- Safeguarding design approved before messaging/video.
- Parent relationship verification tested before parent portal.
- Sharing grants tested before cross-school resources.
- Authority scope and legal mandate confirmed before authority analytics.

## 7. Rollback and feature controls

- AI grading can be disabled by tenant, subject, topic or rubric.
- Student AI can be disabled independently from assignment/submission.
- On answer leakage, disable student AI immediately; keep core learning workflow available.
- On grading degradation, switch to manual grading and preserve proposals for investigation.
- Preserve source files, version references and audit events during rollback.
- Use feature flags and staged rollout.

## 8. Team proposal

- Product Manager / Product Owner.
- Education domain lead / teacher advisor.
- Tech Lead / Architect.
- 2–3 Backend Engineers.
- 2 Frontend Engineers.
- AI/ML Engineer.
- QA Automation Engineer.
- Product Designer.
- DevOps/Security support.
- Data/Analytics support (part-time initially).

Do not remove QA, security, teacher review or AI evaluation to compress timeline.
