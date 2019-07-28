package org.slaq.slaqworx.panoptes.asset;

/**
 * PortfolioKey is a key used to reference Portfolios.
 *
 * @author jeremy
 */
public class PortfolioKey extends AssetKey {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new PortfolioKey with the given ID and version.
     *
     * @param id
     *            the ID to assign to the key, or null to generate one
     * @param version
     *            the version to assign to the key
     */
    public PortfolioKey(String id, long version) {
        super(id, version);
    }
}
