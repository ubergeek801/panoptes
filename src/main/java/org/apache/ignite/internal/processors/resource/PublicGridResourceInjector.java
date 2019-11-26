package org.apache.ignite.internal.processors.resource;

import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.managers.deployment.GridDeployment;

/**
 * {@code PublicGridResourceInjector} is an abuse of {@code GridResourceInjector} which makes it
 * usable outside its package.
 *
 * @author jeremy
 */
public abstract class PublicGridResourceInjector implements GridResourceInjector {
    @Override
    public void inject(GridResourceField field, Object target, Class<?> depCls, GridDeployment dep)
            throws IgniteCheckedException {
        inject(new PublicGridResourceField(field), target, depCls, dep);
    }

    @Override
    public void inject(GridResourceMethod mtd, Object target, Class<?> depCls, GridDeployment dep)
            throws IgniteCheckedException {
        inject(new PublicGridResourceMethod(mtd), target, depCls, dep);
    }

    @Override
    public void undeploy(GridDeployment dep) {
        // nothing to do
    }

    /**
     * Injects resource into field. Caches injected resource with the given key if needed.
     *
     * @param field
     *            Field to inject.
     * @param target
     *            Target object the field belongs to.
     * @param depCls
     *            Deployed class.
     * @param dep
     *            Deployment.
     * @throws IgniteCheckedException
     *             If injection failed.
     */
    protected abstract void inject(PublicGridResourceField field, Object target, Class<?> depCls,
            GridDeployment dep) throws IgniteCheckedException;

    /**
     * Injects resource with a setter method. Caches injected resource with the given key if needed.
     *
     * @param mtd
     *            Setter method.
     * @param target
     *            Target object the field belongs to.
     * @param depCls
     *            Deployed class.
     * @param dep
     *            Deployment.
     * @throws IgniteCheckedException
     *             If injection failed.
     */
    protected abstract void inject(PublicGridResourceMethod mtd, Object target, Class<?> depCls,
            GridDeployment dep) throws IgniteCheckedException;
}
