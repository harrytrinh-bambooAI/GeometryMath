package vn.geo.server.lessonplan.internal.repo;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Danh mục dùng chung của trợ lý soạn giáo án (V525/V526, không theo trường): Tiết PPCT, đơn vị
 * kiến thức, kho bài tập. Chỉ đọc.
 */
@Repository
public class LessonCatalogRepository {

  /** Bài (Chương → Bài). */
  public record Lesson(int no, int chapterNo, String chapterTitle, String title) {}

  /** Tiết PPCT của bài. */
  public record Period(
      String id,
      int lessonNo,
      int ord,
      String label,
      int periods,
      List<String> focus,
      List<String> extraKnowledge) {}

  /** Đơn vị kiến thức. */
  public record Knowledge(String code, int lessonNo, String statement, String status) {}

  /** Bài tập trong kho. */
  public record Exercise(
      String code, int lessonNo, String level, String statement, String answer, String source) {}

  private final JdbcClient jdbc;

  public LessonCatalogRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  public List<Lesson> lessons() {
    return jdbc.sql(
            "SELECT lesson_no, chapter_no, chapter_title, title FROM lp_ppct_lesson"
                + " ORDER BY lesson_no LIMIT 200")
        .query((rs, i) ->
            new Lesson(
                rs.getInt("lesson_no"),
                rs.getInt("chapter_no"),
                rs.getString("chapter_title"),
                rs.getString("title")))
        .list();
  }

  public Optional<Lesson> lesson(int no) {
    return lessons().stream().filter(l -> l.no() == no).findFirst();
  }

  public List<Period> periods() {
    return jdbc.sql(
            "SELECT id, lesson_no, ord, label, periods, focus, extra_knowledge FROM lp_ppct_period"
                + " ORDER BY lesson_no, ord LIMIT 500")
        .query((rs, i) -> period(rs))
        .list();
  }

  public Optional<Period> period(String id) {
    return jdbc.sql(
            "SELECT id, lesson_no, ord, label, periods, focus, extra_knowledge FROM lp_ppct_period"
                + " WHERE id = :id LIMIT 1")
        .param("id", id)
        .query((rs, i) -> period(rs))
        .optional();
  }

  /** Mọi đơn vị kiến thức (danh mục nhỏ, vài trăm dòng). */
  public List<Knowledge> knowledge() {
    return jdbc.sql(
            "SELECT code, lesson_no, statement, status FROM lp_knowledge_unit"
                + " ORDER BY lesson_no, code LIMIT 2000")
        .query((rs, i) ->
            new Knowledge(
                rs.getString("code"),
                rs.getInt("lesson_no"),
                rs.getString("statement"),
                rs.getString("status")))
        .list();
  }

  /** Bài tập kho của một bài: bo-mau trước, rồi bai-tap (thứ tự cấp cho AI). */
  public List<Exercise> exercises(int lessonNo) {
    return jdbc.sql(
            "SELECT code, lesson_no, level, statement, answer, source FROM lp_exercise"
                + " WHERE lesson_no = :no ORDER BY (source <> 'baseline/bo-mau'), code LIMIT 200")
        .param("no", lessonNo)
        .query((rs, i) ->
            new Exercise(
                rs.getString("code"),
                rs.getInt("lesson_no"),
                rs.getString("level"),
                rs.getString("statement"),
                rs.getString("answer"),
                rs.getString("source")))
        .list();
  }

  private static Period period(ResultSet rs) throws SQLException {
    return new Period(
        rs.getString("id"),
        rs.getInt("lesson_no"),
        rs.getInt("ord"),
        rs.getString("label"),
        rs.getInt("periods"),
        strings(rs.getArray("focus")),
        strings(rs.getArray("extra_knowledge")));
  }

  private static List<String> strings(Array a) throws SQLException {
    return a == null ? List.of() : List.of((String[]) a.getArray());
  }
}
