package org.slaq.slaqworx.panoptes.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * CrudRepositoryWithAllIdsQuery is a CrudRepository that adds a <code>getAllIds()</code> query to
 * obtain all primary keys (which is useful when implementing Hazelcast <code>MapLoader</code>s).
 *
 * @author jeremy
 * @param <T>
 *            the entity type
 * @param <ID>
 *            the entity primary key type
 */
@NoRepositoryBean
public interface CrudRepositoryWithAllIdsQuery<T, ID> extends CrudRepository<T, ID> {
    /**
     * Obtains an Iterable<ID> over all keys in the Repository.
     *
     * @return an Iterable<ID> over all keys
     */
    @Query("select e.id from #{#entityName} e")
    public Iterable<ID> getAllIds();
}
