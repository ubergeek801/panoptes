package org.slaq.slaqworx.panoptes.data;

import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration;
import io.micronaut.configuration.jdbc.hikari.DatasourceFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * A Micronaut {@link Factory} that provides {@link Bean}s related to {@link DataSource}s, etc.
 *
 * @author jeremy
 */
@Factory
public class PanoptesDataConfiguration {
  /**
   * Creates a new {@link PanoptesDataConfiguration}. Restricted because instances of this class
   * should be obtained through the {@link ApplicationContext} (if it is needed at all).
   */
  protected PanoptesDataConfiguration() {
    // nothing to do
  }

  /**
   * Provides a {@link DataSource} which overrides the normally configured {@link
   * DatasourceConfiguration} with a dummy implementation suitable for test and standalone
   * scenarios, which shouldn't use a {@link DataSource} at all (and the dummy will fail if usage is
   * attempted).
   *
   * @param datasourceConfiguration
   *     the {@link DatasourceConfiguration} for which to provide a dummy {@link DataSource}
   *
   * @return a dummy {@link DataSource}
   */
  @Context
  @Replaces(value = DataSource.class, factory = DatasourceFactory.class)
  @EachBean(DatasourceConfiguration.class)
  @Requires(env = {Environment.TEST, "offline"})
  protected DataSource dataSource(DatasourceConfiguration datasourceConfiguration) {
    return new DataSource() {
      @Override
      public Connection getConnection() {
        return new DummyConnection();
      }

      @Override
      public Connection getConnection(String username, String password) {
        return new DummyConnection();
      }

      @Override
      public int getLoginTimeout() {
        return 0;
      }

      @Override
      public void setLoginTimeout(int seconds) {
        // nothing to do
      }

      @Override
      public PrintWriter getLogWriter() {
        return null;
      }

      @Override
      public void setLogWriter(PrintWriter out) {
        // nothing to do
      }

      @Override
      public Logger getParentLogger() {
        return null;
      }

      @Override
      public boolean isWrapperFor(Class<?> iface) {
        return false;
      }

      @Override
      public <T> T unwrap(Class<T> iface) {
        return null;
      }
    };
  }
}
