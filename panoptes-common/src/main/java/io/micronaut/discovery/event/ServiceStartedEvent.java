package io.micronaut.discovery.event;

import io.micronaut.discovery.ServiceInstance;

// FIXME this seems to be required temporarily in Micronaut 2.0.0-M2
public class ServiceStartedEvent extends ServiceReadyEvent {
    private static final long serialVersionUID = 1L;

    public ServiceStartedEvent(ServiceInstance source) {
        super(source);
    }
}
