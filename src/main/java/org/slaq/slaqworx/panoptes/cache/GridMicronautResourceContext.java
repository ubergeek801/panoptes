package org.slaq.slaqworx.panoptes.cache;

import io.micronaut.context.BeanContext;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.managers.deployment.GridDeployment;
import org.apache.ignite.internal.processors.resource.GridSpringResourceContext;
import org.apache.ignite.internal.processors.resource.PublicGridResourceField;
import org.apache.ignite.internal.processors.resource.PublicGridResourceInjector;
import org.apache.ignite.internal.processors.resource.PublicGridResourceMethod;

/**
 * {@code GridMicronautResourceContext} pretends to be a {@code GridSpringResourceContext} but
 * actually injects a Micronaut context.
 *
 * @author jeremy
 */
public class GridMicronautResourceContext implements GridSpringResourceContext {
    private final BeanContext applicationContext;

    /**
     * Creates a new {@code GridMicronautResourceContext} which injects the given
     * {@code BeanContext}.
     *
     * @param applicationContext
     */
    public GridMicronautResourceContext(BeanContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public PublicGridResourceInjector springBeanInjector() {
        return new PublicGridResourceInjector() {
            @Override
            protected void inject(PublicGridResourceField field, Object target, Class<?> depCls,
                    GridDeployment dep) throws IgniteCheckedException {
                try {
                    field.getField().set(target, applicationContext.getBean(depCls));
                } catch (Exception e) {
                    throw new IgniteCheckedException(
                            "could not inject " + depCls.getName() + " from ApplicationContext", e);
                }
            }

            @Override
            protected void inject(PublicGridResourceMethod mtd, Object target, Class<?> depCls,
                    GridDeployment dep) throws IgniteCheckedException {
                try {
                    mtd.getMethod().invoke(target, target, applicationContext.getBean(depCls));
                } catch (Exception e) {
                    throw new IgniteCheckedException(
                            "could not inject " + depCls.getName() + " from ApplicationContext", e);
                }
            }
        };
    }

    @Override
    public PublicGridResourceInjector springContextInjector() {
        return new PublicGridResourceInjector() {
            @Override
            protected void inject(PublicGridResourceField field, Object target, Class<?> depCls,
                    GridDeployment dep) throws IgniteCheckedException {
                try {
                    field.getField().set(target, applicationContext);
                } catch (Exception e) {
                    throw new IgniteCheckedException("could not inject ApplicationContext", e);
                }
            }

            @Override
            protected void inject(PublicGridResourceMethod mtd, Object target, Class<?> depCls,
                    GridDeployment dep) throws IgniteCheckedException {
                try {
                    mtd.getMethod().invoke(target, applicationContext);
                } catch (Exception e) {
                    throw new IgniteCheckedException("could not inject ApplicationContext", e);
                }
            }
        };
    }

    @Override
    public Object unwrapTarget(Object target) {
        return target;
    }
}
