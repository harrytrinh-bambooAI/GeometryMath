/**
 * Module assessment (PLAN_code-s5 §2, S5-E4): rubric, bài có chấm, kho ảnh (cổng api.ImageStore),
 * nộp bài viết tay, OCR, chấm tay, trả bài; sau cờ geo.s5.grading.enabled. Gói do T-261128-51 tạo
 * sẵn (chỉ tệp này); chủ: T-261128-57 (rubric/, assign/, storage/, api/), T-261128-59 (submit/,
 * ocr/), T-261128-60 (queue/, review/, returned/). Module khác chỉ dùng
 * vn.geo.server.assessment.api hoặc DomainEvents.
 */
package vn.geo.server.assessment;
