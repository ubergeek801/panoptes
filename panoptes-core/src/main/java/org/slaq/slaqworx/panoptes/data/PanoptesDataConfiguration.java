package org.slaq.slaqworx.panoptes.data;

import java.sql.Connection;

import javax.sql.DataSource;

import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration;
import io.micronaut.configuration.jdbc.hikari.DatasourceFactory;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.jdbc.spring.DataSourceTransactionManagerFactory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
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
     * Provides a {@code DataSource} which overrides the normally configured
     * {@code DataSourceConfiguration} with a dummy implementation suitable for test and standalone
     * scenarios, which shouldn't use a {@code DataSource} at all (and the dummy will fail if usage
     * is attempted).
     *
     * @param datasourceConfiguration
     *            the {@DatasourceConfiguration} for which to provide a dummy {@code DataSource}
     * @return a dummy {@code DataSource}
     */
    @Context
    @Replaces(value = DataSource.class, factory = DatasourceFactory.class)
    @EachBean(DatasourceConfiguration.class)
    @Requires(env = { Environment.TEST, "offline" })
    protected DataSource dataSource(DatasourceConfiguration datasourceConfiguration) {
        return new AbstractDataSource() {
            @Override
            public Connection getConnection() {
                return null;
            }

            @Override
            public Connection getConnection(String username, String password) {
                return null;
            }
        };
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
     * Provides a {@code PlatformTransactionManager} which overrides the default transaction manager
     * with a dummy implementation suitable for test and standalone scenarios, which shouldn't use
     * {@code DataSource}s and transactions at all.
     *
     * @param dataSource
     *            the {DataSource} for which to provide a dummy {@code PlatformTransactionManager}
     * @return a dummy {@code PlatformTransactionManager}
     */
    @Replaces(value = PlatformTransactionManager.class,
            factory = DataSourceTransactionManagerFactory.class)
    @EachBean(DataSource.class)
    @Requires(env = { Environment.TEST, "offline" })
    protected PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new PlatformTransactionManager() {
            @Override
            public void commit(TransactionStatus status) {
                // no-op
            }

            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void rollback(TransactionStatus status) {
                // no-op
            }
        };
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
