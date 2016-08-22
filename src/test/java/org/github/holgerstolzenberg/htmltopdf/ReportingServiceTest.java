package org.github.holgerstolzenberg.htmltopdf;

import com.google.common.io.Files;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class ReportingServiceTest {
  private ReportingService reportingService;

  @BeforeMethod
  public void setUp() throws Exception {
    reportingService = new ReportingService();
  }

  @Test
  public void testGenerate() throws IOException {
    final byte[] pdf = reportingService.generate("/report.html");
    Files.write(pdf, testFile());
    assertThat(pdf).isNotEmpty();
  }

  private File testFile() {
    return new File(userHome() + File.separator + "reportingService.pdf");
  }

  private String userHome() {
    return System.getProperty("user.home");
  }
}
