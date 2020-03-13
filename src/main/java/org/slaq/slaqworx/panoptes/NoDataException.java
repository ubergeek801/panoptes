package org.slaq.slaqworx.panoptes;

/**
 * {@code NoDataException} indicates an exceptional circumstance in which the data required to
 * perform an evaluation was not available.
 *
 * @author jeremy
 */
public class NoDataException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String attributeName;

    /**
     * Creates a new {@code NoDataException} indicating that a {@code Security} attribute with the
     * given name was unavailable.
     *
     * @param attributeName
     *            the attribute name that was not found
     */
    public NoDataException(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Obtains the name of the missing attribute that gave rise to this {@code NoDataException}.
     *
     * @return an attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }
}