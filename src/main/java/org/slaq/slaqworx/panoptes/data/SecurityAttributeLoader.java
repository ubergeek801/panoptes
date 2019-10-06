package org.slaq.slaqworx.panoptes.data;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * {@code SecurityAttributeLoader} is a service that initializes the known
 * {@code SecurityAttribute}s.
 */
@Singleton
public class SecurityAttributeLoader {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityAttributeLoader.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new {@code SecurityAttributeLoader} that uses the given {@code JdbcTemplate}.
     * Restricted because this class should be obtained through the {@code ApplicationContext}.
     *
     * @param jdbcTemplate
     *            a {@code JdbcTemplate} from which to obtain data
     */
    protected SecurityAttributeLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Initializes the {@code SecurityAttribute}s from persistent data.
     */
    public void loadSecurityAttributes() {
        LOG.info("loading {} SecurityAttributes", jdbcTemplate
                .queryForObject("select count(*) from security_attribute", Integer.class));
        jdbcTemplate.query("select name, index, type from security_attribute",
                (RowCallbackHandler)(rs -> {
                    String name = rs.getString(1);
                    int index = rs.getInt(2);
                    String className = rs.getString(3);
                    Class<?> clazz;
                    try {
                        clazz = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        clazz = String.class;
                        LOG.warn("cannot locate class {} for SecurityAttribute {}", className,
                                name);
                    }
                    SecurityAttribute.of(name, index, clazz);
                }));
    }
}
