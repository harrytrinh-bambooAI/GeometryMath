package vn.geo.server.geometry.api;

/**
 * Bài tự học đã dựng hình (POST /api/v1/student/self-study, GET
 * /api/v1/student/self-study/{assignmentId}). Xem {@code app/docs/SCENE-CONTRACT.md} §4.
 */
public record SelfStudyDto(
    String assignmentId, String problemVersionId, String statement, SceneDto scene) {}
