package org.slaq.slaqworx.panoptes.data;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.ValueProvider;

/**
 * {@code JdbcSecurityAttributeLoader} is a service that initializes the known
 * {@code SecurityAttribute}s from a JDBC data source.
 */
@Singleton
public class JdbcSecurityAttributeLoader implements SecurityAttributeLoader {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcSecurityAttributeLoader.class);

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new {@code JdbcSecurityAttributeLoader} that uses the given {@code JdbcTemplate}.
     * Restricted because this class should be obtained through the {@code ApplicationContext}.
     *
     * @param jdbcTemplate
     *            a {@code JdbcTemplate} from which to obtain data
     */
    protected JdbcSecurityAttributeLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void loadSecurityAttributes() {
        LOG.info("loading {} SecurityAttributes", jdbcTemplate
                .queryForObject("select count(*) from security_attribute", Integer.class));
        jdbcTemplate.query("select name, index, type from security_attribute",
                (RowCallbackHandler)(rs -> {
                    String name = rs.getString(1);
                    int index = rs.getInt(2);
                    String className = rs.getString(3);
                    @SuppressWarnings("rawtypes")
                    Class clazz;
                    try {
                        clazz = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        clazz = String.class;
                        LOG.warn("cannot locate class {} for SecurityAttribute {}", className,
                                name);
                    }
                    @SuppressWarnings({ "unchecked", "unused" })
                    SecurityAttribute<?> notUsed =
                            SecurityAttribute.of(name, index, clazz, ValueProvider.forClass(clazz));
                }));
    }
}
