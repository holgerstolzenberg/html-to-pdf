package org.github.holgerstolzenberg.htmltopdf;

import com.google.common.io.ByteStreams;
import com.itextpdf.text.DocumentException;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.ByteStreams.*;
import static com.google.common.net.HttpHeaders.LOCATION;
import static java.lang.System.nanoTime;
import static java.time.ZonedDateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

final class ReportingService {
  private static final Logger LOG = getLogger(ReportingService.class);

  byte[] generate(final String base, final String file) {
    checkArgument(!isNullOrEmpty(base));
    checkArgument(!isNullOrEmpty(file));

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

  private void render(ByteArrayOutputStream os, String content, String baseUrl) throws DocumentException, IOException {
    final ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(content, baseUrl);
    renderer.layout();
    renderer.createPDF(os);
  }

  private String getBaseUrl(final String base) throws IOException {
    return new ClassPathResource(base + "/").getURL().toExternalForm();
  }

  private String getContent(final String base, final String file) throws IOException {
    return new String(toByteArray(inputStream(base, file)));
  }

  private InputStream inputStream(String base, String name) {
    return ReportingService.class.getResourceAsStream(base + "/" + name);
  }

  private long generationTimeMs(long start) {
    return TimeUnit.NANOSECONDS.toMillis(nanoTime() - start);
  }

}
