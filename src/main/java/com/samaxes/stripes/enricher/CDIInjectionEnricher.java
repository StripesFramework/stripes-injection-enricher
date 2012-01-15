/*
 * $Id$
 *
 * Copyright 2011 samaxes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samaxes.stripes.enricher;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.naming.InitialContext;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.util.Log;

/**
 * Enricher that provide JSR-299 CDI field and method argument injection. It is used to lookup managed beans and inject
 * them into objects (often ActionBeans).
 *
 * @author Samuel Santos
 * @version $Revision$
 */
public class CDIInjectionEnricher {

    private static final Log log = Log.getInstance(CDIInjectionEnricher.class);

    private static final String STANDARD_BEAN_MANAGER_JNDI_NAME = "java:comp/BeanManager";

    private static final String SERVLET_BEAN_MANAGER_JNDI_NAME = "java:comp/env/BeanManager";

    /**
     * Internal constructor; not to be called as this class provides static utilities only.
     */
    private CDIInjectionEnricher() {
        throw new UnsupportedOperationException("No instances permitted");
    }

    /**
     * Lookup beans and inject them into objects.
     *
     * @param bean the binding process target
     * @param injectAnnotationPresent whether @Inject is present in class or not
     */
    public static void bind(ActionBean bean, Boolean injectAnnotationPresent) {
        log.debug("Running CDI dependency injection for instance of ", bean.getClass().getSimpleName());

        if (bean != null && injectAnnotationPresent != null && injectAnnotationPresent) {
            BeanManager beanManager = lookupBeanManager();

            if (beanManager != null) {
                injectNonContextualInstance(beanManager, bean);
            } else {
                // Better would be to raise an exception if @Inject is present in class and BeanManager cannot be found
                log.error("BeanManager cannot be located in context. Either you are using an archive with no beans.xml"
                        + ", or the BeanManager has not been produced.");
            }
        }
    }

    /**
     * Inject non contextual object instance.
     *
     * @param manager the BeanManager
     * @param instance the object instance
     */
    @SuppressWarnings("unchecked")
    protected static void injectNonContextualInstance(BeanManager manager, Object instance) {
        CreationalContext<Object> creationalContext = manager.createCreationalContext(null);
        InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) manager.createInjectionTarget(manager
                .createAnnotatedType(instance.getClass()));
        injectionTarget.inject(instance, creationalContext);
    }

    /**
     * Lookup BeanManager and return it.
     *
     * @return the BeanManager
     */
    protected static BeanManager lookupBeanManager() {
        try {
            return (BeanManager) new InitialContext().lookup(STANDARD_BEAN_MANAGER_JNDI_NAME);
        } catch (Exception e) {
            try {
                return (BeanManager) new InitialContext().lookup(SERVLET_BEAN_MANAGER_JNDI_NAME);
            } catch (Exception se) {
                return null;
            }
        }
    }
}
