package org.slaq.slaqworx.panoptes.data;

import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.TransactionStatus;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.result.ResultIterator;

/**
 * A {@code Closeable} {@code ResultIterable} that retrieves keys on behalf of a Hazelcast {@code
 * MapLoader} (for use with the {@code loadAllKeys()} method) and performs post-query cleanup.
 * Hazelcast promises to call {@code close()} when it completes iterating through the results, at
 * which time the open transaction is completed and the {@code Jdbi} handle is closed.
 *
 * @param <K>
 *     the type of the keys to be loaded
 *
 * @author jeremy
 */
public class KeyIterable<K> implements ResultIterable<K>, Closeable {
  private final SynchronousTransactionManager<Connection> transactionManager;
  private final Jdbi jdbi;
  private final String query;
  private final RowMapper<K> keyMapper;

  private TransactionStatus<Connection> transaction;
  private Handle jdbiHandle;

  /**
   * Creates a new {@code KeyIterable}.
   *
   * @param transactionManager
   *     the {@code TransactionManager} to use for {@code loadAllKeys()}
   * @param jdbi
   *     the {@code Jdbi} instance to use for database operations
   * @param query
   *     the key retrieval query to be used
   * @param keyMapper
   *     the {@code RowMapper} to be used to map key results
   */
  public KeyIterable(SynchronousTransactionManager<Connection> transactionManager, Jdbi jdbi,
                     String query, RowMapper<K> keyMapper) {
    this.transactionManager = transactionManager;
    this.jdbi = jdbi;
    this.query = query;
    this.keyMapper = keyMapper;
  }

  @Override
  public void close() throws IOException {
    if (jdbiHandle != null) {
      jdbiHandle.close();
    }

    if (transaction != null) {
      transactionManager.commit(transaction);
    }
  }

  @Override
  public ResultIterator<K> iterator() {
    // create a transaction and Jdbi handle, both of which will remain open until close{} is
    // called
    transaction = transactionManager.getTransaction(TransactionDefinition.DEFAULT);
    jdbiHandle = jdbi.open();

    return jdbiHandle.createQuery(query).map(keyMapper).iterator();
  }
}
