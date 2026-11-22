# work/src — code as the source of truth
> Template file of `references/feature-scaffold/` (v1.00, 2026-11-11). Replace every `<…>`; delete this line. Format rules: `references/06_working-artifact-templates.md`; catalog: `references/05_artifact-catalog.md`.
| File | Role |
|---|---|
| `metrics.py` | golden dataset (oracle 1) and expected values |
| `terms.py` | display terms from the glossary |
| `gen_epic_<epic>.py` | one per epic: stories, AC, design, cases as objects → `work/epics/<EPIC>/*.md` |
| `verify.py` | V1–V14 recomputed independently (oracle 2) → `work/08_verification_report.md` |
| `xcheck.py` | package cross-check |
| `gen_final.py` | (A only) deliverables `06`, `07*`, `13`, `14*`, `15`, `16` |
Run discipline (R10): `python3 gen_epic_<x>.py && python3 verify.py`; read only the summary lines.
