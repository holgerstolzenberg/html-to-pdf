package org.github.holgerstolzenberg.htmltopdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.util.XRLog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.slf4j.LoggerFactory.getLogger;

final class ClasspathUserAgentCallback extends ITextUserAgent {
  private static final Logger LOG = getLogger(ClasspathUserAgentCallback.class);

  ClasspathUserAgentCallback(ITextOutputDevice outputDevice, SharedContext sharedContext) {
    super(outputDevice);
    setSharedContext(sharedContext);
  }

  @Override
  public String resolveURI(String uri) {
    return uri;
  }

  @Override
  protected InputStream resolveAndOpenStream(String uri) {
    final URL url = getUrl(uri);

    try {
      return url.openStream();
    } catch (final IOException cause) {
      LOG.error("Error loading resource", cause);
      return null;
    }
  }

  private URL getUrl(String uri) {
    final URL url = ClasspathUserAgentCallback.class.getResource("/report/" + uri);
    if (url == null) {
      throw new IllegalArgumentException(String.format("Cannot resolve URL for relative URI '%s'", uri));
    }

    LOG.trace("Load: {}", url);
    return url;
  }
}
