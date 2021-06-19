package org.slaq.slaqworx.panoptes;

import java.io.Serial;
import javax.annotation.Nonnull;
import org.slaq.slaqworx.panoptes.asset.Security;

/**
 * Indicates an exceptional circumstance in which the data required to perform an evaluation was not
 * available.
 *
 * @author jeremy
 */
public class NoDataException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  private final @Nonnull
  String attributeName;

  /**
   * Creates a new {@link NoDataException} indicating that a {@link Security} attribute with the
   * given name was unavailable.
   *
   * @param attributeName
   *     the attribute name that was not found
   */
  public NoDataException(@Nonnull String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * Obtains the name of the missing attribute that gave rise to this {@link NoDataException}.
   *
   * @return an attribute name
   */
  @Nonnull
  public String getAttributeName() {
    return attributeName;
  }
}
