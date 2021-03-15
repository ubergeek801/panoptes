package org.slaq.slaqworx.panoptes.data;

import io.micronaut.context.ApplicationContext;
import jakarta.inject.Singleton;
import javax.transaction.Transactional;
import org.jdbi.v3.core.Jdbi;
import org.slaq.slaqworx.panoptes.asset.SecurityAttribute;
import org.slaq.slaqworx.panoptes.rule.ValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service that initializes the known {@link SecurityAttribute}s from a JDBC data source.
 */
@Singleton
public class JdbcSecurityAttributeLoader implements SecurityAttributeLoader {
  private static final Logger LOG = LoggerFactory.getLogger(JdbcSecurityAttributeLoader.class);

  private final Jdbi jdbi;

  /**
   * Creates a new {@link JdbcSecurityAttributeLoader} that uses the given {@link Jdbi}. Restricted
   * because this class should be obtained through the {@link ApplicationContext}.
   *
   * @param jdbi
   *     the {@link Jdbi} instance through which to access the database
   */
  protected JdbcSecurityAttributeLoader(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  @Transactional
  public void loadSecurityAttributes() {
    jdbi.withHandle(handle -> {
      int numAttributes =
          handle.select("select count(*) from security_attribute").mapTo(Integer.class).one();
      LOG.info("loading {} SecurityAttributes", numAttributes);

      handle.select("select name, index, type from security_attribute").map((rs, ctx) -> {
        String name = rs.getString(1);
        int index = rs.getInt(2);
        String className = rs.getString(3);
        Class clazz;
        try {
          clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
          clazz = String.class;
          LOG.warn("cannot locate class {} for SecurityAttribute {}", className, name);
        }

        SecurityAttribute<?> attribute =
            SecurityAttribute.of(name, index, clazz, ValueProvider.forClassIfAvailable(clazz));
        return attribute;
      });

      return null;
    });
  }
}
