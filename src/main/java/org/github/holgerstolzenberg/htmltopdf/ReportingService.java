package org.github.holgerstolzenberg.htmltopdf;

import com.itextpdf.text.DocumentException;
import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.lang.System.nanoTime;
import static java.time.ZonedDateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

final class ReportingService {
  private static final Logger LOG = getLogger(ReportingService.class);
  private static final String SEPARATOR = "/";

  @Nonnull
  byte[] generate(@NotNull final String template) {
    checkArgument(!isNullOrEmpty(template));

    final String base = base(template);
    final String file = file(template);

    long start = nanoTime();
    LOG.debug("Launching report generation: {}", now());

    final byte[] pdf = doGenerate(base, file);

    LOG.debug("Took: {} ms", generationTimeMs(start));
    return pdf;
  }

  private byte[] doGenerate(final String base, final String file) {
    try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      String content = getContent(base, file);
      String baseUrl = getBaseUrl(base);

      render(os, content, baseUrl);

      return os.toByteArray();
    } catch (final DocumentException | IOException cause) {
      throw new PdfGenerationException(cause);
    }
  }

  private String base(final String template) {
    if (!template.contains(SEPARATOR)) {
      return template;
    }
    return template.substring(0, template.lastIndexOf(SEPARATOR) + 1);
  }

  private String file(final String template) {
    if (!template.contains(SEPARATOR)) {
      return template;
    }
    return template.substring(template.lastIndexOf(SEPARATOR));
  }

  private void render(ByteArrayOutputStream os, String content, String baseUrl) throws DocumentException, IOException {
    final ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(content, baseUrl);
    renderer.layout();
    renderer.createPDF(os);
  }

  private String getBaseUrl(final String base) throws IOException {
    return new ClassPathResource(base + SEPARATOR).getURL().toExternalForm();
  }

  private String getContent(final String base, final String file) throws IOException {
    return new String(toByteArray(inputStream(base, file)));
  }

  private InputStream inputStream(String base, String name) {
    return ReportingService.class.getResourceAsStream(base + name);
  }

  private long generationTimeMs(long start) {
    return TimeUnit.NANOSECONDS.toMillis(nanoTime() - start);
  }
}
