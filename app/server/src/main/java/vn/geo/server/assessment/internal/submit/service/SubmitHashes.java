package vn.geo.server.assessment.internal.submit.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Băm SHA-256 (hex) của tệp ảnh gốc (so với băm tệp máy đã tải lên, US-S5E4-03-SC1/SC2). */
final class SubmitHashes {

  private SubmitHashes() {}

  static String sha256(byte[] b) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
