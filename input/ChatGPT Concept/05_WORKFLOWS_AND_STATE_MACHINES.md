# 05 — WORKFLOWS & STATE MACHINES

## 1. Workflow A — Teacher authors and publishes assignment

1. Teacher opens an assigned class.
2. Selects Create Assignment.
3. Enters title, instructions, subject, grade, topic and learning objectives.
4. Adds question by typing or importing image/PDF/document/lesson material.
5. OCR/extraction generates editable draft text.
6. Teacher reviews math notation and corrects extraction.
7. Teacher selects or creates rubric and partial-credit rules.
8. Teacher links approved KB versions and permitted resources.
9. Teacher configures due date, attempts, late policy and answer-release policy.
10. Teacher previews student view.
11. Teacher publishes.
12. System freezes published assignment/rubric/resource references into immutable versions and notifies assigned students.

**Failure paths:** OCR uncertain → manual correction; rubric invalid → block publication; KB unapproved → block student grant; teacher lacks class permission → deny.

## 2. Workflow B — Student attempts and submits work

1. Student opens assignment from own class.
2. System checks enrollment, assignment status and allowed materials.
3. Student enters text and/or uploads multiple images.
4. System validates file type/size and scans files.
5. OCR creates a derived transcription.
6. Student may correct OCR if enabled.
7. Student submits.
8. System creates immutable submission version and queues analysis.
9. Student sees status updates.
10. Student cannot see AI grading proposal, hidden answer or teacher-only rubric notes.

**Retry rule:** duplicate submit request with same idempotency key must not create duplicate submission versions.

## 3. Workflow C — AI grading and teacher approval

1. Worker loads exact assignment version, rubric version and submission version.
2. Original evidence and OCR artifacts are made available to the grading model.
3. AI proposes criterion-level score, rationale, evidence and confidence.
4. Deterministic validator checks schema, criterion IDs, score bounds and total.
5. Anomaly/uncertainty detector flags low-confidence or contradictory cases.
6. Proposal is persisted as immutable AI output.
7. Teacher opens grading queue.
8. Teacher compares original image, OCR text, rubric and AI evidence.
9. Teacher accepts/edits/rejects each criterion.
10. Teacher approves final grade.
11. System records approver, timestamp, overrides and comments.
12. Feedback is released according to teacher policy.
13. Student receives approved score and feedback.

**Mandatory rule:** no direct transition from AI proposal to ApprovedGrade without authorized teacher action.

## 4. Workflow D — Student asks AI during an assignment

1. Student asks question from assignment context.
2. Answer Firewall authenticates and resolves assignment state.
3. Checks if AI help is enabled and which response modes are permitted.
4. Retrieves only approved KB versions explicitly in scope.
5. Generates a hint or concept explanation with bounded output.
6. Validates source provenance and scans for answer leakage.
7. If allowed, returns answer and source references.
8. If not allowed, returns safe fallback or asks student to consult teacher.
9. Stores question, response, source chunks and policy decision.
10. Teacher can review question history in authorized scope.

## 5. Workflow E — Teacher creates / governs KB

1. Teacher creates or imports knowledge item.
2. System extracts text and metadata.
3. Teacher sets subject, grade, topic, curriculum scope and visibility.
4. Content enters Draft.
5. Teacher reviews and submits for approval (self-approval may be allowed for personal class KB if school policy permits).
6. Approved version is indexed.
7. Teacher grants version to class/assignment.
8. Changes create a new version; old version remains referenced by historical interactions.
9. Archived/expired versions are removed from current retrieval.

## 6. Workflow F — Student question history and teacher intervention

1. System captures each student question with class, assignment, topic and time.
2. AI may suggest a concept tag and possible misconception with confidence.
3. Teacher opens student profile and reviews actual questions/answers and submitted work.
4. Teacher confirms, edits or dismisses suggested interpretation.
5. Teacher records an intervention or recommended practice.
6. Follow-up activity is assigned.
7. System records outcome evidence.
8. Student profile updates from verified signals, not unsupported inference.

## 7. Workflow G — Teacher AI Copilot

1. Teacher selects task: lesson plan, quiz, rubric, answer key, feedback summary, announcement, parent report or reminder.
2. Teacher selects source materials/KB and class context.
3. Copilot checks access and scope.
4. AI creates a draft with source references and confidence/warnings.
5. Teacher edits.
6. Teacher explicitly approves and publishes/sends.
7. Audit records author, approver, source and version.

No message or grade is sent/changed without explicit teacher confirmation.

## 8. Workflow H — Parent access

1. Guardian requests link to student.
2. School verifies relationship through approved process.
3. Guardian relationship is created with allowed permissions.
4. Parent sees only approved child's information.
5. Revocation, custody change or school decision updates relationship state.
6. Every access is checked and audited.

## 9. Workflow I — Cross-school collaboration

1. School A publishes collaboration opportunity or resource.
2. School B discovers public/authorized metadata.
3. School B requests participation or access.
4. School A approves scope, users, purpose and duration.
5. Platform creates SharingGrant/SchoolPartnership.
6. Members join a moderated workspace.
7. Shared content remains under owner and license policy.
8. Grant expires/revokes; access is removed and audit preserved.

## 10. Workflow J — Authority policy distribution

1. Authority drafts policy/notice with issuer, legal basis/reference, audience scope and effective date.
2. Policy is reviewed/approved by authorized issuer.
3. System resolves recipient schools/roles from registry and delegation.
4. Notice is distributed and acknowledged.
5. Schools submit implementation status/evidence when required.
6. Authority sees scoped status dashboard.
7. Exceptions are escalated to responsible level.
8. Acknowledgment and implementation are tracked separately.

## 11. State machines

### Assignment
`draft → review_ready → published → closed → archived`
- Published edits create a new version.
- A closed assignment cannot accept new submission unless reopened by authorized teacher.

### Submission
`draft → submitted → processing → grading_ready → teacher_review → approved → released`
Exceptional: `ocr_uncertain`, `processing_failed`, `needs_resubmission`, `withdrawn`.

### Grading proposal
`queued → analyzing → proposed → teacher_review → accepted_or_edited → approved`
Exceptional: `abstained`, `failed`, `rejected`, `regraded`.

### Knowledge version
`draft → pending_review → approved → active → archived/expired`
Approval and active status may be combined where policy permits, but historical version is immutable.

### Guardian relationship
`requested → verification_pending → active → suspended/revoked/expired`.

### Sharing grant
`requested → approved → active → expired/revoked`.

### Governance notice
`draft → review → approved → scheduled → published → acknowledged/under_implementation → completed/closed`.

## 12. Cross-cutting workflow requirements

- Every transition validates actor, scope and current state.
- State transitions are atomic and audited.
- Background processing is idempotent.
- Notifications are emitted through an outbox after transaction commit.
- Retries cannot duplicate grades, messages or submission records.
- Rejected/failed transitions return actionable error codes.
