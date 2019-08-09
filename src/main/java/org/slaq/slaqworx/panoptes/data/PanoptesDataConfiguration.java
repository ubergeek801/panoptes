package org.slaq.slaqworx.panoptes.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;

/**
 * {@code PanoptesDataConfiguration} is a Spring {@code Configuration} that provides {@code Bean}s
 * related to {@code DataSource}s, {@code EntityManager}s, etc.
 *
 * @author jeremy
 */
@Configuration
public class PanoptesDataConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(PanoptesDataConfiguration.class);

    /**
     * Creates a new {@code PanoptesDataConfiguration}. Restricted because instances of this class
     * should be obtained through Spring (if it is needed at all).
     */
    protected PanoptesDataConfiguration() {
        // nothing to do
    }

    /**
     * A pseudo-{@code Bean} that initializes the known {@code SecurityAttribute}s. Dependent
     * services such as the Hazelcast cache may {@code @DependsOn} this.
     *
     * @param jdbcTemplate
     *            a {@code JdbcTemplate} from which to obtain data
     * @return a meaningless {@code Void}
     */
    @Bean
    protected Void securityAttributeLoader(JdbcTemplate jdbcTemplate) {
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

        return null;
    }
}
