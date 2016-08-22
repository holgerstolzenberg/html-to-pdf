package org.github.holgerstolzenberg.htmltopdf;

import com.itextpdf.text.DocumentException;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.util.XRLog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
      renderer.setDocument(doc, "classpath://report/");
      renderer.getSharedContext().setUserAgentCallback(new UserAgentCallback(renderer.getOutputDevice(), renderer.getSharedContext()));
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

  private static class UserAgentCallback extends ITextUserAgent {
    UserAgentCallback(ITextOutputDevice outputDevice, SharedContext sharedContext) {
      super(outputDevice);
      setSharedContext(sharedContext);
    }

    @Override
    public String resolveURI(String uri) {
      return uri;
    }

    @Override
    protected InputStream resolveAndOpenStream(String uri) {
      java.io.InputStream is = null;
      System.out.println("------> " + uri);
      URL url = UserAgentCallback.class.getResource("/report/" + uri);
      System.out.println("-----> " + url);
      if (url == null) {
        XRLog.load("Didn't find resource [" + uri + "].");
        return null;
      }
      try {
        is = url.openStream();
      } catch (java.net.MalformedURLException e) {
        XRLog.exception("bad URL given: " + uri, e);
      } catch (java.io.FileNotFoundException e) {
        XRLog.exception("item at URI " + uri + " not found");
      } catch (java.io.IOException e) {
        XRLog.exception("IO problem for " + uri, e);
      }
      return is;
    }
  }
}
