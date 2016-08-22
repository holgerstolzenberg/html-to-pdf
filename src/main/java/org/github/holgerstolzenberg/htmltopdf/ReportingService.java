package org.github.holgerstolzenberg.htmltopdf;

import com.itextpdf.text.DocumentException;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.XMLResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.System.nanoTime;
import static java.time.ZonedDateTime.now;
import static org.slf4j.LoggerFactory.getLogger;

final class ReportingService {
  private static final Logger LOG = getLogger(ReportingService.class);

  byte[] generate(final String uri) {
    checkArgument(!isNullOrEmpty(uri));

    long start = nanoTime();
    LOG.debug("Launching report generation: {}", now());

    final byte[] pdf = doGenerate(uri);

    LOG.debug("Took: {} ms", generationTimeMs(start));
    return pdf;
  }

  private byte[] doGenerate(final String uri) {
    try (final ByteArrayOutputStream os = new ByteArrayOutputStream()) {
      final Document doc = XMLResource.load(ReportingService.class.getResourceAsStream(uri)).getDocument();

      ITextRenderer renderer = new ITextRenderer();
      renderer.setDocument(doc, uri);
      renderer.layout();
      renderer.createPDF(os);

      return os.toByteArray();
    } catch (final DocumentException | IOException cause) {
      throw new PdfGenerationException(cause);
    }
  }

  private long generationTimeMs(long start) {
    return TimeUnit.NANOSECONDS.toMillis(nanoTime() - start);
  }

  private class PdfGenerationException extends RuntimeException {
    private PdfGenerationException(Throwable cause) {
      super(cause);
    }
  }
}
