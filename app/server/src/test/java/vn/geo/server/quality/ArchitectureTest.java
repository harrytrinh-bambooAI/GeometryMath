package vn.geo.server.quality;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/** Kiểm thử kiến trúc: ranh giới module, phân tầng, cấm JPA. Chỉ phân tích mã main. */
@AnalyzeClasses(packages = "vn.geo.server", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  private static final String ROOT = "vn.geo.server.";

  private static String moduleOf(String pkg) {
    if (!pkg.startsWith(ROOT)) {
      return "";
    }
    String rest = pkg.substring(ROOT.length());
    int dot = rest.indexOf('.');
    return dot < 0 ? rest : rest.substring(0, dot);
  }

  private static final ArchCondition<JavaClass> ONLY_USED_BY_OWN_MODULE =
      new ArchCondition<>("chỉ được module sở hữu dùng") {
        @Override
        public void check(JavaClass item, ConditionEvents events) {
          String owner = moduleOf(item.getPackageName());
          for (Dependency d : item.getDirectDependenciesToSelf()) {
            if (!moduleOf(d.getOriginClass().getPackageName()).equals(owner)) {
              events.add(SimpleConditionEvent.violated(d, d.getDescription()));
            }
          }
        }
      };

  /** Module khác chỉ được dùng gói api; internal là riêng tư. */
  @ArchTest
  static final ArchRule internalIsPrivateToModule =
      classes()
          .that(resideInAPackage("vn.geo.server.*.internal.."))
          .should(ONLY_USED_BY_OWN_MODULE);

  @ArchTest
  static final ArchRule noCyclesBetweenModules =
      slices().matching("vn.geo.server.(*)..").should().beFreeOfCycles();

  @ArchTest
  static final ArchRule commonDoesNotKnowModules =
      noClasses()
          .that()
          .resideInAPackage("vn.geo.server.common..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "vn.geo.server.iam..",
              "vn.geo.server.tenancy..",
              "vn.geo.server.content..",
              "vn.geo.server.geometry..",
              "vn.geo.server.learning..",
              "vn.geo.server.answers..",
              "vn.geo.server.knowledge..",
              "vn.geo.server.events..",
              "vn.geo.server.aigateway..",
              "vn.geo.server.assignment..",
              "vn.geo.server.tracking..",
              // Chặng 2–3 (T-261126-41, PLAN_code-s23 §5 luật 2): mỗi task thêm module mới NGAY
              // DƯỚI mốc của
              // mình, dạng "vn.geo.server.<module>..", (có dấu phẩy); giữ "sample.." ở cuối.
              // --- T-261126-41 (khung) ---
              "vn.geo.server.subject..",
              "vn.geo.server.notify..",
              // --- T-261126-18 (S2-E1) ---
              // --- T-261126-19 (S2-E2) ---
              // --- T-261126-20 (S2-E5) ---
              // --- T-261126-21 (S2-E8) ---
              // --- T-261126-22 (S2-E3) ---
              // --- T-261126-23 (S2-E6, S2-E7) ---
              // --- T-261126-24 (S2-E4) ---
              // --- T-261126-25 (S2-E9) ---
              // --- T-261126-42 (S3 dữ liệu mẫu) ---
              // --- T-261126-28 (S3-E1, S3-E5) ---
              // --- T-261126-29 (S3-E2) ---
              // --- T-261126-30 (S3-E3) ---
              // --- T-261126-31 (S3-E4, S3-E5) ---
              // --- T-261126-32 (S3-E6, S3-E5) ---
              // Chặng 5 (T-261128-51, PLAN_code-s5 §5 luật 2): mỗi task thêm module mới NGAY DƯỚI
              // mốc của mình.
              // --- T-261128-51 (khung) ---
              "vn.geo.server.lessonplan..",
              "vn.geo.server.exam..",
              "vn.geo.server.assessment..",
              "vn.geo.server.aigrade..",
              // --- T-261128-52 (S5-E2) ---
              // --- T-261128-53 (S5-E1) ---
              // --- T-261128-54 (S5-E1, S5-E2 xuất) ---
              // --- T-261128-55 (S5-E5) ---
              // --- T-261128-56 (S5-E3) ---
              // --- T-261128-57 (S5-E4) ---
              // --- T-261128-58 (S5-E3) ---
              // --- T-261128-59 (S5-E4) ---
              // --- T-261128-60 (S5-E4) ---
              // --- T-261128-61 (S5-E6) ---
              // --- T-261128-62 (S5-E6) ---
              // --- T-261128-63 (S5-E2, S5-E3) ---
              // --- T-261128-64 (S5-E5) ---
              // --- T-261128-65 (S5-E6) ---
              // --- FIX chặng 5 (task FIX T-261128-*) ---
              // --- FIX (task FIX T-261126-35, 43+) ---
              "vn.geo.server.sample..");

  @ArchTest
  static final ArchRule sampleIsRemovable =
      noClasses()
          .that()
          .resideOutsideOfPackage("vn.geo.server.sample..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("vn.geo.server.sample..");

  /** Lớp web không chạm dữ liệu: đi qua service. */
  @ArchTest
  static final ArchRule webDoesNotTouchDataAccess =
      noClasses()
          .that()
          .resideInAPackage("..web..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "..repo..",
              "vn.geo.server.common.db..",
              "org.springframework.jdbc..",
              "javax.sql..",
              "java.sql..");

  @ArchTest
  static final ArchRule layersGoDownOnly =
      noClasses()
          .that()
          .resideInAnyPackage("..repo..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..web..", "..service..");

  @ArchTest
  static final ArchRule serviceDoesNotCallWeb =
      noClasses()
          .that()
          .resideInAPackage("..service..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..web..");

  /** Truy cập JDBC chỉ trong repo, common.db. */
  @ArchTest
  static final ArchRule jdbcOnlyInRepositories =
      noClasses()
          .that()
          .resideOutsideOfPackages(
              "..repo..", "vn.geo.server.common.db..", "vn.geo.server.common.config..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework.jdbc..", "javax.sql..");

  @ArchTest
  static final ArchRule noJpaOrHibernate =
      noClasses()
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "jakarta.persistence..",
              "javax.persistence..",
              "org.hibernate..",
              "org.springframework.data.jpa..");

  @ArchTest
  static final ArchRule controllersLiveInWebPackages =
      classes()
          .that()
          .areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
          .should()
          .resideInAPackage("..web..");

  @ArchTest
  static final ArchRule repositoriesLiveInRepoPackages =
      classes()
          .that()
          .areAnnotatedWith(org.springframework.stereotype.Repository.class)
          .should()
          .resideInAPackage("..repo..");

  /** FIX4b m2: bean móc kiểm thử (Sandbox, TestHook) chỉ tạo ở profile dev/test, không lọt prod. */
  @ArchTest
  static final ArchRule testHooksAreProfileScoped =
      classes()
          .that()
          .areTopLevelClasses()
          .and()
          .haveNameMatching(".*\\.[^.$]*(Sandbox|TestHook)[^.$]*")
          .should()
          .beAnnotatedWith(org.springframework.context.annotation.Profile.class);

  @ArchTest static final ArchRule noFieldInjection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
}
