package org.github.holgerstolzenberg.htmltopdf;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

final class PdfGenerationException extends RuntimeException {
  PdfGenerationException(@Nonnull Throwable cause) {
    super(checkNotNull(cause));
  }
}
