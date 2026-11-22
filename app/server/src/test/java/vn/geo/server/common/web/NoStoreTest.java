package vn.geo.server.common.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.geo.server.common.error.AppException;
import vn.geo.server.common.error.ErrorCode;
import vn.geo.server.common.error.GlobalExceptionHandler;

class NoStoreTest {

  @RestController
  static class Mixed {
    @NoStore
    @GetMapping("/sensitive")
    String sensitive() {
      return "lời giải";
    }

    @NoStore
    @GetMapping("/sensitive-error")
    String sensitiveError() {
      throw new AppException(ErrorCode.FORBIDDEN);
    }

    @GetMapping("/plain")
    String plain() {
      return "công khai";
    }
  }

  @NoStore
  @RestController
  static class WholeController {
    @GetMapping("/whole")
    String whole() {
      return "trạng thái khóa";
    }
  }

  private final MockMvc mvc =
      MockMvcBuilders.standaloneSetup(new Mixed(), new WholeController())
          .addInterceptors(new NoStoreInterceptor())
          .setControllerAdvice(
              new GlobalExceptionHandler(new HttpProperties(0, Duration.ofSeconds(2))))
          .build();

  @Test
  void annotatedMethodGetsNoStore() throws Exception {
    mvc.perform(get("/sensitive"))
        .andExpect(status().isOk())
        .andExpect(header().string("Cache-Control", "no-store"));
  }

  @Test
  void errorResponseOfAnnotatedEndpointIsAlsoNoStore() throws Exception {
    mvc.perform(get("/sensitive-error"))
        .andExpect(status().isForbidden())
        .andExpect(header().string("Cache-Control", "no-store"));
  }

  @Test
  void annotatedControllerAppliesToAllMethods() throws Exception {
    mvc.perform(get("/whole")).andExpect(header().string("Cache-Control", "no-store"));
  }

  @Test
  void unannotatedEndpointIsUntouched() throws Exception {
    mvc.perform(get("/plain")).andExpect(header().doesNotExist("Cache-Control"));
  }
}
