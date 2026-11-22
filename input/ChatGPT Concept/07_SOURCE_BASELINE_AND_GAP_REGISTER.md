# 07 — SOURCE BASELINE & GAP REGISTER
# GeometryMathSupport → Unified AI Education Ecosystem

## 1. Source baseline

This document maps the existing `SPEC.md` and `GUIDELINE.pdf` to the target ecosystem. The source documents describe an existing GeometryMathSupport artifact reconstructed from a single-file HTML/JS implementation updated 19/09/2026. The screenshots are explicitly described as local runs using mock AI and mock DB; they illustrate layout and behavior, not proof of live AI or production persistence.

## 2. Existing product capability

### 2.1 Product intent
Help Vietnamese THCS/THPT students visualize geometry, especially spatial geometry:
- Enter a problem by text or image.
- AI transcribes and solves step by step.
- Each step explains the principle behind it.
- Clickable terms open short AI-generated lessons.
- 2D SVG and rotatable/zoomable 3D geometry.
- Students can add lines, planes, intersections and axes to explore.
- Contextual chat and Q&A journal.
- Searchable, paginated history.

### 2.2 Existing UI structure
Desktop at ≥1150px:
- Left: 3D tools.
- Center: problem input, history, journal and solution.
- Right: contextual chat.
Below 1150px: single-column layout.

Mobile screenshot reference: 420px, single column.

### 2.3 Existing input requirements
- Level selector: `thcs` / `thpt`, default THCS.
- Multiline problem input.
- PNG/JPEG/WebP/GIF file selection or clipboard paste.
- If image is present and text is empty, AI OCR fills editable text.
- If text exists, text takes priority and is not overwritten.
- Re-OCR action can replace text.
- If images unsupported, hide image upload and request typed input.
- Empty text and no image returns validation message.

### 2.4 Existing solution output
- Reconstructed `problem_text`.
- `figure.type`: `2d`, `3d` or `none`.
- `figure.svg` for 2D.
- `figure.solid` with vertices, edges, optional faces for 3D.
- `steps[]`: title, 2–5 bullet items, `principle`.
- `{{term}}` syntax becomes clickable keyword.
- Step title opens principle modal.
- 2D SVG is embedded in solution panel.

### 2.5 Existing 3D tools
- Three.js scene with independently rendered faces, edges and labeled vertices.
- Pointer rotate and wheel zoom.
- Highlight edges/faces; optionally extend edge or expand face.
- Add line with segment/extendA/extendB modes.
- Optional line intersection with edge or face.
- Add plane from three points; optional intersection with edge or face.
- New points/lines/faces register for subsequent operations.
- Oxyz axes with configurable length.
- One active 3D render loop at a time.

### 2.6 Existing chat, history and keyword lecture
- Contextual chat uses problem and solution summary.
- Answer up to 3–4 sentences, 1–3 suggested keywords.
- Context limited to recent turns.
- Q&A pairs stored in `chat_history`, shown in journal.
- Up to 20 recent journal entries.
- Up to 200 problem history records, 10 per page.
- Search ignores Vietnamese diacritics/case.
- Selecting a history item reconstructs stored solution without another AI call.
- Keyword lecture cached by normalized slug; lecture length 150–250 Vietnamese words.

### 2.7 Existing technical implementation
- One static HTML file and plain JavaScript IIFE.
- Three.js r128 loaded from CDN.
- Artifact runtime dependencies: `claude.use('sample')` and `claude.use('db')`.
- Proposed separation modules in source: solver, solid3d, geometry-math, store, UI.
- Geometry helpers: vector operations, plane construction, ray-plane intersection, closest points between lines and plane-plane intersection.
- AI solution response is JSON; chat response is JSON.

### 2.8 Existing visual design
- Ink `#1B2A4A`.
- Paper `#F0F2ED`.
- Bronze `#B98A2E`.
- Teal `#2F6F68`.
- Error red `#B0432E`.
- Source Serif 4 headings; Inter body.
- Light theme.
- Clickable keyword teal dotted underline; step title bronze underline.
- Responsive layout.

## 3. Known limitations from SPEC

1. Duplicate “Mặt Mặt …” label.
2. No free point, midpoint or ratio point creation.
3. No drag-handle plane resizing.
4. Skew-line/edge intersection can be represented by closest-point approximation without warning.
5. Fan triangulation may fail for concave or unsorted polygon faces.
6. No automatic hidden-edge rendering.
7. No pinch zoom or reset view on mobile.
8. Q&A journal is not filtered by current problem.
9. AI-generated geometry coordinates are not mathematically validated.
10. No login or per-student data isolation; history is shared at artifact level.
11. Current prompt uses Vietnamese without diacritics for input compatibility; output requires Vietnamese with diacritics.

## 4. Existing acceptance criteria to preserve

- Enter a pyramid problem and render 3D figure plus at least three solution steps within 60 seconds (source acceptance target).
- Rotate 3D by drag and zoom by wheel; vertex labels remain readable.
- Highlight edge SC and expand highlighted face.
- Add line A→C intersecting edge BD at O; O appears in scene and point list.
- Build plane (S,B,D) intersecting base; show MN; deleting it removes line, M/N and labels.
- Clipboard image OCR fills editable text; existing text is not overwritten.
- Repeated keyword uses cache.
- Search without diacritics finds accented history.
- Chat returns ≤4 sentences and ≤3 keywords; Q&A appears in journal.
- 420px mobile layout has one column and no horizontal overflow.

These criteria apply to the existing GeometryMathSupport experience. They do not replace new platform acceptance criteria.

## 5. Target integration mapping

| Existing capability | Target module | Target evolution |
|---|---|---|
| Problem input text/image | Assignment Studio / Solver | Teacher authors question; student submits work |
| OCR and editable transcription | File & Submission Processing | Persist source image + OCR artifact + correction history |
| Step-by-step solution JSON | Solver / Teacher KB | Teacher-authorized worked solution; student mode gated by Answer Firewall |
| `principle` modal | Learning Content | Preserve as concept rationale, cite approved source where available |
| Clickable keywords / lecture cache | Knowledge Service | Tenant/school/class scoped, versioned, approved KB |
| 2D SVG | Geometry Renderer | Reuse with sanitization and validation |
| Three.js 3D scene | Geometry Renderer | Reuse as isolated frontend module |
| Highlight / add line / plane / Oxyz | Geometry Exploration | Keep in geometry learning experience |
| Contextual chat | Student AI | Add assignment scope, teacher KB and Answer Firewall |
| Q&A journal | Student Intelligence | Per-user, per-class, per-assignment question history |
| Problem history | Student Portfolio / Teacher View | Per-user ownership, authorization and audit |
| Mock DB / Artifact runtime | Backend persistence | Replace with API, relational DB and object storage |
| `sample` AI runtime | AI Gateway | Server-side model provider and structured output validation |

## 6. Migration requirements

- Preserve geometry rendering and pure math helpers behind a stable module boundary.
- Replace runtime AI access with server-side AI Gateway; never expose provider keys.
- Replace artifact DB with tenant-scoped persistence.
- Separate original input, OCR text, AI solution and teacher/student interaction.
- Add authenticated user, tenant, class and ownership to every history record.
- Preserve existing JSON fields where practical; introduce schema version and validation.
- In student assessment mode, full solutions are not automatically returned; release policy controls disclosure.
- Teacher mode may use full solver output to prepare lessons, examples and answer references.
- Sanitize SVG and validate 3D geometry before rendering.
- Add geometric validation and uncertainty indicators before relying on generated coordinates.

## 7. Gap register

| ID | Gap | Priority | Proposed action |
|---|---|---|---|
| GAP-01 | No authentication / per-user history | P0 | Identity and membership foundation |
| GAP-02 | No tenant/school/class model | P0 | Multi-tenant organization model |
| GAP-03 | No assignment/submission workflow | P0 | Teacher/Student workspace |
| GAP-04 | No rubric/AI grading | P0 | Assessment domain |
| GAP-05 | No teacher-controlled KB | P0 | KB approval/version/grant |
| GAP-06 | No Answer Firewall | P0 | Server-side policy pipeline |
| GAP-07 | Mock AI/DB in screenshots | P0 | Production backend and provider evaluation |
| GAP-08 | OCR/math notation uncertainty | P0 | Original evidence + correction + confidence |
| GAP-09 | Geometry output not validated | P1 | Geometry consistency validator |
| GAP-10 | Q&A journal not scoped by problem | P1 | Context filters and ownership |
| GAP-11 | No teacher/parent insight profile | P1 | Student Intelligence and guardian access |
| GAP-12 | No collaboration/safeguarding | P2 | Moderation and child-safe group design |
| GAP-13 | No school operating/governance layer | P2 | School OS and Authority Governance |
| GAP-14 | Mobile 3D limitations | P1 | Pinch zoom, reset view, touch testing |
| GAP-15 | 3D face triangulation limitations | P1 | Robust polygon triangulation and validation |
| GAP-16 | No hidden-edge rendering | P1 | Visibility algorithm and diagram settings |

## 8. Product integrity notes

- Existing app is a learning/geometry prototype, not a complete education platform.
- Mock screenshot data must not be presented as evidence of live AI accuracy.
- Proposed architecture and feature scope are target designs, not implemented features.
- Existing limitations remain visible in backlog until fixed and tested.
- Existing source-specific behaviors should be retained unless a product decision explicitly replaces them.
