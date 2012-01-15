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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.util.Log;

/**
 * <p>
 * Enricher that provide @Resource field and setter method injection. It is used to lookup resources and inject them
 * into objects (often ActionBeans).
 * </p>
 * <p>
 * Field resources will only be injected if the current value is NULL or primitive default value.
 * </p>
 *
 * @author Samuel Santos
 * @version $Revision$
 */
public class ResourceInjectionEnricher {

    private static final Log log = Log.getInstance(ResourceInjectionEnricher.class);

    private static final String RESOURCE_LOOKUP_PREFIX = "java:comp/env";

    /**
     * Internal constructor; not to be called as this class provides static utilities only.
     */
    private ResourceInjectionEnricher() {
        throw new UnsupportedOperationException("No instances permitted");
    }

    /**
     * Lookup resources and inject them into objects.
     *
     * @param bean the binding process target
     * @param fields fields on a class that are annotated for injection
     * @param methods methods on a class that are annotated for injection
     * @throws Exception if the binding process produced unrecoverable errors
     */
    public static void bind(ActionBean bean, Collection<Field> fields, Collection<Method> methods) throws Exception {
        log.debug("Running @Resource dependency injection for instance of ", bean.getClass().getSimpleName());

        // First inject any properties that are annotated
        if (fields != null) {
            for (Field field : fields) {
                Object currentValue = field.get(bean);
                if (shouldInject(field, currentValue)) {
                    Object resource = resolveResource(field);
                    field.set(bean, resource);
                }
            }
        }

        // Then inject any values using annotated methods
        if (methods != null) {
            for (Method method : methods) {
                Object resource = resolveResource(method);
                method.invoke(bean, resource);
            }
        }
    }

    /**
     * Looks up the JNDI resource for any given annotated element.
     *
     * @param element any annotated element (field, method, etc.)
     * @return the located resource
     * @throws Exception when no resource found in JNDI
     */
    protected static Object resolveResource(AnnotatedElement element) throws Exception {
        Context context = new InitialContext();
        Object resolvedResource = null;

        // This implementation is based on previous behavior in injectClass()
        if (Field.class.isAssignableFrom(element.getClass())) {
            resolvedResource = context.lookup(getResourceName((Field) element));
        } else if (Method.class.isAssignableFrom(element.getClass())) {
            resolvedResource = context.lookup(getResourceName(element.getAnnotation(Resource.class)));
        }

        return resolvedResource;
    }

    /**
     * Get the resource JNDI name.
     *
     * @param field the annotated field
     * @return the resource JNDI name
     */
    protected static String getResourceName(Field field) {
        Resource resource = field.getAnnotation(Resource.class);
        String resourceName = getResourceName(resource);
        if (resourceName != null) {
            return resourceName;
        }

        String propertyName = field.getName();
        String className = field.getDeclaringClass().getName();
        return RESOURCE_LOOKUP_PREFIX + "/" + className + "/" + propertyName;
    }

    /**
     * Get the resource JNDI name.
     *
     * @param resource the element annotation
     * @return the resource JNDI name
     */
    protected static String getResourceName(Resource resource) {
        String mappedName = resource.mappedName();
        if (!mappedName.equals("")) {
            return mappedName;
        }

        String name = resource.name();
        if (!name.equals("")) {
            return RESOURCE_LOOKUP_PREFIX + "/" + name;
        }

        return null;
    }

    private static boolean shouldInject(Field field, Object currentValue) {
        Class<?> type = field.getType();

        if (type.isPrimitive() && isPrimitiveNull(currentValue)) {
            log.debug("Primitive field " + field.getName() + " has been detected to have the default primitive value, "
                    + "can not determine if it has already been injected. Re-injecting field.");
            return true;
        } else if (currentValue == null) {
            return true;
        }

        return false;
    }

    private static boolean isPrimitiveNull(Object currentValue) {
        String stringValue = String.valueOf(currentValue);

        if ("0".equals(stringValue) || "0.0".equals(stringValue) || "false".equals(stringValue)) {
            return true;
        } else if (Character.class.isInstance(currentValue)) {
            if (Character.class.cast(currentValue) == (char) 0) {
                return true;
            }
        }

        return false;
    }
}
