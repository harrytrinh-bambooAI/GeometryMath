package vn.geo.server.geometry.internal;

/** Phép toán véc tơ 3 chiều (mảng double[3]), hàm thuần. */
final class V {

  private V() {}

  static double[] of(double x, double y, double z) {
    return new double[] {x, y, z};
  }

  static double[] sub(double[] a, double[] b) {
    return of(a[0] - b[0], a[1] - b[1], a[2] - b[2]);
  }

  static double dot(double[] a, double[] b) {
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
  }

  static double[] cross(double[] a, double[] b) {
    return of(a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]);
  }

  static double norm(double[] a) {
    return Math.sqrt(dot(a, a));
  }

  static double dist(double[] a, double[] b) {
    return norm(sub(a, b));
  }

  /** Khoảng cách từ p tới mặt phẳng qua a, b, c (NaN nếu ba điểm thẳng hàng). */
  static double distToPlane(double[] p, double[] a, double[] b, double[] c) {
    double[] n = cross(sub(b, a), sub(c, a));
    double nn = norm(n);
    return nn == 0 ? Double.NaN : Math.abs(dot(sub(p, a), n)) / nn;
  }

  /** Khoảng cách từ p tới đường thẳng ab. */
  static double distToLine(double[] p, double[] a, double[] b) {
    double[] u = sub(b, a);
    double nu = norm(u);
    return nu == 0 ? Double.NaN : norm(cross(sub(p, a), u)) / nu;
  }

  /** Diện tích đa giác phẳng (đỉnh theo thứ tự vòng). */
  static double polygonArea(double[]... pts) {
    double[] s = of(0, 0, 0);
    for (int i = 0; i < pts.length; i++) {
      double[] c = cross(pts[i], pts[(i + 1) % pts.length]);
      s = of(s[0] + c[0], s[1] + c[1], s[2] + c[2]);
    }
    return norm(s) / 2;
  }
}
