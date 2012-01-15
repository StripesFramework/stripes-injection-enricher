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
package com.samaxes.stripes.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.inject.Inject;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.controller.ExecutionContext;
import net.sourceforge.stripes.controller.Interceptor;
import net.sourceforge.stripes.controller.Intercepts;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.exception.StripesRuntimeException;
import net.sourceforge.stripes.util.Log;
import net.sourceforge.stripes.util.ReflectUtil;

import com.samaxes.stripes.enricher.CDIInjectionEnricher;
import com.samaxes.stripes.enricher.EJBInjectionEnricher;
import com.samaxes.stripes.enricher.ResourceInjectionEnricher;

/**
 * <p>
 * Enricher that provide @Inject, @EJB and @Resource field and setter method injection. It is used to lookup beans and
 * resources, and inject them into objects (often ActionBeans).
 * </p>
 * <p>
 * Fields annotated with @Resources will only be injected if the current value is {@code NULL} or primitive default
 * value.
 * </p>
 * <p>
 * Methods and fields may be public, protected, package-access or private. If they are not public an attempt is made to
 * call {@link Method#setAccessible(boolean)} in order to make them accessible from this class. If the attempt fails, an
 * exception will be thrown.
 * </p>
 * <p>
 * The first time that any of the injection methods in this class is called with a specific type of object, the object's
 * class is examined for annotated fields and methods. The discovered fields and methods are then cached for future
 * usage.
 * </p>
 * <p>
 * To configure {@code InjectionEnricher}, add the following initialization parameters to your Stripes filter
 * configuration (in {@code web.xml}):
 * </p>
 *
 * <pre>
 * {@code
 * <init-param>
 *     <param-name>Extension.Packages</param-name>
 *     <param-value>com.samaxes.stripes.inject</param-value>
 * </init-param>
 * }
 * </pre>
 *
 * @author Samuel Santos
 * @version $Revision$
 */
@Intercepts(LifecycleStage.ActionBeanResolution)
public class InjectionEnricher implements Interceptor {

    private static final Log log = Log.getInstance(InjectionEnricher.class);

    /** Map of classes that have their fields parsed. */
    private static Map<Class<?>, Boolean> parsedFieldMap = new ConcurrentHashMap<Class<?>, Boolean>();

    /** Map of classes that have their mathods parsed. */
    private static Map<Class<?>, Boolean> parsedMethodMap = new ConcurrentHashMap<Class<?>, Boolean>();

    /** Lazily filled in map of Class to fields or methods annotated with @Inject. */
    private static Map<Class<?>, Boolean> cdiTargetMap = new ConcurrentHashMap<Class<?>, Boolean>();

    /** Lazily filled in map of Class to fields annotated with @EJB. */
    private static Map<Class<?>, Collection<Field>> ejbFieldMap = new ConcurrentHashMap<Class<?>, Collection<Field>>();

    /** Lazily filled in map of Class to methods annotated with @EJB. */
    private static Map<Class<?>, Collection<Method>> ejbMethodMap = new ConcurrentHashMap<Class<?>, Collection<Method>>();

    /** Lazily filled in map of Class to fields annotated with @Resource. */
    private static Map<Class<?>, Collection<Field>> resourceFieldMap = new ConcurrentHashMap<Class<?>, Collection<Field>>();

    /** Lazily filled in map of Class to methods annotated with @Resource. */
    private static Map<Class<?>, Collection<Method>> resourceMethodMap = new ConcurrentHashMap<Class<?>, Collection<Method>>();

    /**
     * Allows ActionBean resolution to proceed and then once the ActionBean has been located performs the injection
     * enrichment.
     *
     * @param ctx the current execution context
     * @return the Resolution produced by calling context.proceed()
     * @throws Exception if the binding process produced unrecoverable errors
     */
    @Override
    public Resolution intercept(ExecutionContext ctx) throws Exception {
        Resolution resolution = ctx.proceed();
        ActionBean bean = ctx.getActionBean();
        log.debug("Running injection enricher for instance of ", bean.getClass().getSimpleName());

        fillFieldMaps(bean.getClass());
        fillMethodMaps(bean.getClass());

        CDIInjectionEnricher.bind(bean, cdiTargetMap.get(bean.getClass()));
        EJBInjectionEnricher.bind(bean, ejbFieldMap.get(bean.getClass()), ejbMethodMap.get(bean.getClass()));
        ResourceInjectionEnricher.bind(bean, resourceFieldMap.get(bean.getClass()),
                resourceMethodMap.get(bean.getClass()));

        return resolution;
    }

    /**
     * Fetches the fields on a class that are annotated for injection. The first time it is called for a particular
     * class it will introspect the class and cache the results. All non-overridden fields are examined, including
     * protected and private fields. If a field is not public an attempt it made to make it accessible - if it fails it
     * is removed from the collection and an error is logged.
     *
     * @param clazz the class on which to look for annotated fields
     */
    protected void fillFieldMaps(Class<?> clazz) {
        if (!parsedFieldMap.containsKey(clazz)) {
            Collection<Field> ejbFields = null;
            Collection<Field> resourceFields = null;
            Collection<Field> fields = ReflectUtil.getFields(clazz);

            for (Field field : fields) {
                if (!cdiTargetMap.containsKey(clazz) && field.isAnnotationPresent(Inject.class)) {
                    cdiTargetMap.put(clazz, Boolean.TRUE);
                }

                if (field.isAnnotationPresent(EJB.class)) {
                    if (!field.isAccessible()) {
                        // If the field isn't public, try to make it accessible
                        try {
                            field.setAccessible(true);
                        } catch (SecurityException se) {
                            throw new StripesRuntimeException("Field " + clazz.getName() + "." + field.getName()
                                    + "is marked " + "with @EJB annotation and is not public. An attempt to call "
                                    + "setAccessible(true) resulted in a SecurityException. Please "
                                    + "either make the field public, annotate a public setter instead "
                                    + "or modify your JVM security policy to allow Stripes to setAccessible(true).", se);
                        }
                    }

                    if (ejbFields == null) {
                        ejbFields = new ArrayList<Field>();
                    }
                    ejbFields.add(field);
                }

                if (field.isAnnotationPresent(Resource.class)) {
                    if (!field.isAccessible()) {
                        // If the field isn't public, try to make it accessible
                        try {
                            field.setAccessible(true);
                        } catch (SecurityException se) {
                            throw new StripesRuntimeException("Field " + clazz.getName() + "." + field.getName()
                                    + "is marked " + "with @Resource annotation and is not public. An attempt to call "
                                    + "setAccessible(true) resulted in a SecurityException. Please "
                                    + "either make the field public, annotate a public setter instead "
                                    + "or modify your JVM security policy to allow Stripes to setAccessible(true).", se);
                        }
                    }

                    if (resourceFields == null) {
                        resourceFields = new ArrayList<Field>();
                    }
                    resourceFields.add(field);
                }
            }

            parsedFieldMap.put(clazz, Boolean.TRUE);
            if (ejbFields != null) {
                ejbFieldMap.put(clazz, ejbFields);
            }
            if (resourceFields != null) {
                resourceFieldMap.put(clazz, resourceFields);
            }
        }
    }

    /**
     * Fetches the methods on a class that are annotated for injection. The first time it is called for a particular
     * class it will introspect the class and cache the results. All non-overridden methods are examined, including
     * protected and private methods. If a method is not public an attempt it made to make it accessible - if it fails
     * it is removed from the collection and an error is logged.
     *
     * @param clazz the class on which to look for annotated methods
     */
    protected void fillMethodMaps(Class<?> clazz) {
        if (!parsedMethodMap.containsKey(clazz)) {
            Collection<Method> ejbMethods = null;
            Collection<Method> resourceMethods = null;
            Collection<Method> methods = ReflectUtil.getMethods(clazz);

            for (Method method : methods) {
                if (!cdiTargetMap.containsKey(clazz) && method.isAnnotationPresent(Inject.class)) {
                    cdiTargetMap.put(clazz, Boolean.TRUE);
                }

                if (method.isAnnotationPresent(EJB.class)) {
                    // Ensure the method has only the one parameter
                    if (method.getParameterTypes().length != 1) {
                        throw new StripesRuntimeException("@EJB only allowed on single argument methods");
                    }
                    // Ensure the method starts with 'set'
                    if (!method.getName().startsWith("set")) {
                        throw new StripesRuntimeException("@EJB only allowed on 'set' methods");
                    }
                    // If the method isn't public, try to make it accessible
                    if (!method.isAccessible()) {
                        try {
                            method.setAccessible(true);
                        } catch (SecurityException se) {
                            throw new StripesRuntimeException("Method " + clazz.getName() + "." + method.getName()
                                    + "is marked " + "with @EJB annotation and is not public. An attempt to call "
                                    + "setAccessible(true) resulted in a SecurityException. Please "
                                    + "either make the method public or modify your JVM security "
                                    + "policy to allow Stripes to setAccessible(true).", se);
                        }
                    }

                    if (ejbMethods == null) {
                        ejbMethods = new ArrayList<Method>();
                    }
                    ejbMethods.add(method);
                }

                if (method.isAnnotationPresent(Resource.class)) {
                    // Ensure the method has only the one parameter
                    if (method.getParameterTypes().length != 1) {
                        throw new StripesRuntimeException("@Resource only allowed on single argument methods");
                    }
                    // Ensure the method starts with 'set'
                    if (!method.getName().startsWith("set")) {
                        throw new StripesRuntimeException("@Resource only allowed on 'set' methods");
                    }
                    // If the method isn't public, try to make it accessible
                    if (!method.isAccessible()) {
                        try {
                            method.setAccessible(true);
                        } catch (SecurityException se) {
                            throw new StripesRuntimeException("Method " + clazz.getName() + "." + method.getName()
                                    + "is marked " + "with @Resource annotation and is not public. An attempt to call "
                                    + "setAccessible(true) resulted in a SecurityException. Please "
                                    + "either make the method public or modify your JVM security "
                                    + "policy to allow Stripes to setAccessible(true).", se);
                        }
                    }

                    if (resourceMethods == null) {
                        resourceMethods = new ArrayList<Method>();
                    }
                    resourceMethods.add(method);
                }
            }

            parsedMethodMap.put(clazz, Boolean.TRUE);
            if (ejbMethods != null) {
                ejbMethodMap.put(clazz, ejbMethods);
            }
            if (resourceMethods != null) {
                resourceMethodMap.put(clazz, resourceMethods);
            }
        }
    }
}
