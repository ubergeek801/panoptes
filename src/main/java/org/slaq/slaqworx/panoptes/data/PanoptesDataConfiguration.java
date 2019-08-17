package org.slaq.slaqworx.panoptes.data;

import javax.sql.DataSource;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * {@code PanoptesDataConfiguration} is a Micronaut {@code Factory} that provides {@code Bean}s
 * related to {@code DataSource}s, {@code EntityManager}s, etc.
 *
 * @author jeremy
 */
@Factory
public class PanoptesDataConfiguration {
    /**
     * Creates a new {@code PanoptesDataConfiguration}. Restricted because instances of this class
     * should be obtained through the {@code ApplicationContext} (if it is needed at all).
     */
    protected PanoptesDataConfiguration() {
        // nothing to do
    }

    /**
     * Provides a {@code JdbcTemplate} for the specified (default) {@code DataSource}.
     *
     * @param dataSource
     *            the {@code DataSource} for which to provide a {@code JdbcTemplate}
     * @return a {@code JdbcTemplate} for the given {@code DataSource}
     */
    @Bean
    protected JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Provides a {@code TransactionTemplate} using the specified
     * {@code PlatformTransactionManager}.
     * 
     * @param transactionManager
     *            the {@code PlatformTransactionManager} to use to manage template transactions
     * @return a {@code TransactionTemplate} that uses the given transaction manager
     */
    @Bean
    protected TransactionTemplate
            transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
