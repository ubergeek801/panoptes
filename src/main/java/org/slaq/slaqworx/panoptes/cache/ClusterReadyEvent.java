package org.slaq.slaqworx.panoptes.cache;

/**
 * {@code ClusterReadyEvent} indicates that the cluster is ready for whatever.
 *
 * @author jeremy
 */
public class ClusterReadyEvent {
    /**
     * Creates a new {@code ClusterReadyEvent}. Restricted because you can't have just any random
     * code publishing these things...
     */
    protected ClusterReadyEvent() {
        // nothing to do
    }
}
