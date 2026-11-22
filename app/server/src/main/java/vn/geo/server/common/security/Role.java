package vn.geo.server.common.security;

/**
 * Vai theo PO-01 (học sinh, phụ huynh, giáo viên, quản trị trường, cơ quan quản lý). Tên sẽ do ARCH
 * chốt.
 */
public enum Role {
  STUDENT,
  PARENT,
  TEACHER,
  SCHOOL_ADMIN,
  AGENCY_ADMIN
}
