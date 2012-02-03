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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.util.Log;

/**
 * Enricher that provide EJB field and setter method injection. It is used to lookup EJBs and inject them into objects
 * (often ActionBeans).
 *
 * @author Samuel Santos
 * @version $Revision$
 */
public class EJBInjectionEnricher {

    private static final Log log = Log.getInstance(EJBInjectionEnricher.class);

    /**
     * Internal constructor; not to be called as this class provides static utilities only.
     */
    private EJBInjectionEnricher() {
        throw new UnsupportedOperationException("No instances permitted");
    }

    /**
     * Lookup beans and inject them into objects.
     *
     * @param bean the binding process target
     * @param fields fields on a class that are annotated for injection
     * @param methods methods on a class that are annotated for injection
     * @throws Exception if the binding process produced unrecoverable errors
     */
    public static void bind(ActionBean bean, Collection<Field> fields, Collection<Method> methods) throws Exception {
        log.debug("Running @EJB dependency injection for instance of ", bean.getClass().getSimpleName());

        // First inject any properties that are annotated
        if (fields != null) {
            for (Field field : fields) {
                EJB ejbAnnotation = field.getAnnotation(EJB.class);
                Object ejb = lookupEJB(field.getType(), ejbAnnotation.lookup(), ejbAnnotation.mappedName());
                field.set(bean, ejb);
            }
        }

        // Then inject any values using annotated methods
        if (methods != null) {
            for (Method method : methods) {
                EJB ejbAnnotation = method.getAnnotation(EJB.class);
                Object ejb = lookupEJB(method.getParameterTypes()[0], ejbAnnotation.lookup(),
                        ejbAnnotation.mappedName());
                method.invoke(bean, ejb);
            }
        }
    }

    /**
     * Lookup EJBs and return it.
     *
     * @param fieldType The EJB class type
     * @param lookup A portable lookup string containing the JNDI name for the target EJB component
     * @param mappedName The product specific name of the EJB component to which this ejb reference should be mapped
     * @return EJB object
     * @throws Exception when no EJB found in JNDI
     */
    protected static Object lookupEJB(Class<?> fieldType, String lookup, String mappedName) throws Exception {
        Context context = new InitialContext();
        String appName = (String) context.lookup("java:app/AppName");
        String moduleName = (String) context.lookup("java:module/ModuleName");
        String[] jndiNames;

        if (lookup != null && !"".equals(lookup)) {
            jndiNames = new String[] { lookup };
        } else if (mappedName != null && !"".equals(mappedName)) {
            jndiNames = new String[] { mappedName };
        } else {
            // @formatter:off
            jndiNames = new String[] {
                "java:global/" + appName + "/" + moduleName + "/" + fieldType.getSimpleName() + "!" + fieldType.getName(),
                "java:global/" + appName + "/" + moduleName + "/" + fieldType.getSimpleName() + "Bean!" + fieldType.getName(),
                "java:global/" + appName + "/" + moduleName + "/" + fieldType.getSimpleName(),
                "java:global/" + appName + "/" + moduleName + "/" + fieldType.getSimpleName() + "Bean",
                "java:global/" + moduleName + "/" + fieldType.getSimpleName() + "!" + fieldType.getName(),
                "java:global/" + moduleName + "/" + fieldType.getSimpleName() + "Bean!" + fieldType.getName(),
                "java:global/" + moduleName + "/" + fieldType.getSimpleName(),
                "java:global/" + moduleName + "/" + fieldType.getSimpleName() + "Bean",
                "java:app/" + moduleName + "/" + fieldType.getSimpleName() + "!" + fieldType.getName(),
                "java:app/" + moduleName + "/" + fieldType.getSimpleName() + "Bean!" + fieldType.getName(),
                "java:app/" + moduleName + "/" + fieldType.getSimpleName(),
                "java:app/" + moduleName + "/" + fieldType.getSimpleName() + "Bean",
                "java:module/" + fieldType.getSimpleName() + "!" + fieldType.getName(),
                "java:module/" + fieldType.getSimpleName() + "Bean!" + fieldType.getName(),
                "java:module/" + fieldType.getSimpleName(),
                "java:module/" + fieldType.getSimpleName() + "Bean",
                // JBoss AS 6 or lower default binding
                appName + "/" + fieldType.getSimpleName() + "Bean/local",
                appName + "/" + fieldType.getSimpleName() + "Bean/remote",
                appName + "/" + fieldType.getSimpleName() + "/no-interface",
                fieldType.getSimpleName() + "Bean/local",
                fieldType.getSimpleName() + "Bean/remote",
                fieldType.getSimpleName() + "/no-interface",
                // WebSphere Application Server Local EJB default binding
                "ejblocal:" + fieldType.getCanonicalName(),
                // WebSphere Application Server Remote EJB default binding
                fieldType.getCanonicalName()
            };
            // @formatter:on
        }

        for (String jndiName : jndiNames) {
            try {
                Object ejb = context.lookup(jndiName);
                log.debug("EJB found in JNDI. JNDI name: ", jndiName);

                return ejb;
            } catch (NamingException e) {
                // no-op, try next
            }
        }

        throw new NamingException("No EJB found in JNDI, tried the following names: " + joinJndiNames(jndiNames));
    }

    /**
     * Simple helper for printing the jndi names.
     *
     * @param strings JNDI names array to join
     * @return comma separated JNDI names
     */
    private static String joinJndiNames(String[] strings) {
        StringBuilder sb = new StringBuilder();

        for (String string : strings) {
            sb.append(string).append(", ");
        }

        return sb.toString();
    }
}
