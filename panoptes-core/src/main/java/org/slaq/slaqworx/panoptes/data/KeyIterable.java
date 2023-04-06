package org.slaq.slaqworx.panoptes.data;

import com.hazelcast.map.MapLoader;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.TransactionManager;
import io.micronaut.transaction.TransactionStatus;
import java.io.Closeable;
import java.sql.Connection;
import javax.annotation.Nonnull;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.result.ResultIterator;

/**
 * A {@link Closeable} {@link ResultIterable} that retrieves keys on behalf of a Hazelcast {@link
 * MapLoader} (for use with the {@code loadAllKeys()} method) and performs post-query cleanup.
 * Hazelcast promises to call {@code close()} when it completes iterating through the results, at
 * which time the open transaction is completed and the {@link Jdbi} handle is closed.
 *
 * @param <K> the type of the keys to be loaded
 * @author jeremy
 */
public class KeyIterable<K> implements ResultIterable<K>, Closeable {
  @Nonnull private final SynchronousTransactionManager<Connection> transactionManager;
  @Nonnull private final Jdbi jdbi;
  @Nonnull private final String query;
  @Nonnull private final RowMapper<K> keyMapper;

  private TransactionStatus<Connection> transaction;
  private Handle jdbiHandle;

  /**
   * Creates a new {@link KeyIterable}.
   *
   * @param transactionManager the {@link TransactionManager} to use for {@code loadAllKeys()}
   * @param jdbi the {@link Jdbi} instance to use for database operations
   * @param query the key retrieval query to be used
   * @param keyMapper the {@link RowMapper} to be used to map key results
   */
  public KeyIterable(
      @Nonnull SynchronousTransactionManager<Connection> transactionManager,
      @Nonnull Jdbi jdbi,
      @Nonnull String query,
      @Nonnull RowMapper<K> keyMapper) {
    this.transactionManager = transactionManager;
    this.jdbi = jdbi;
    this.query = query;
    this.keyMapper = keyMapper;
  }

  @Override
  public void close() {
    if (jdbiHandle != null) {
      jdbiHandle.close();
    }

    if (transaction != null) {
      transactionManager.commit(transaction);
    }
  }

  @Override
  public ResultIterator<K> iterator() {
    // create a transaction and Jdbi handle, both of which will remain open until close() is
    // called
    transaction = transactionManager.getTransaction(TransactionDefinition.DEFAULT);
    jdbiHandle = jdbi.open();

    return jdbiHandle.createQuery(query).map(keyMapper).iterator();
  }
}
