# Stripes Injection Enricher

Stripes Injection Enricher enriches Stripes objects by satisfying injection points specified declaratively using annotations.  
There are three injection-based enrichers provided by Stripes Injection Enricher out of the box:

* `@Resource` - Java EE resource injections
* `@EJB` - EJB session bean reference injections
* `@Inject` - CDI injections

The first two enrichers use JNDI to lookup the instance to inject.  
The CDI injections are handled by treating the Stripes object as a bean capable of receiving standard CDI injections.

The `@Resource` annotation gives you access to any object which is available via JNDI.  
It follows the standard rules for `@Resource` (as defined in the Section 2.3 of the Common Annotations for the Java Platform specification).

The `@EJB` annotation performs a JNDI lookup for the EJB session bean reference using portable global JNDI names.  
If no matching beans were found in those locations the injection will fail.

However you can manually set the JNDI name to lookup using the `mappedName` attribute for `@EJB`, as well as the `mappedName` and `name` attributes for `@Resource`:

    public class MyActionBean implements ActionBean {

        @Inject
        private FooService fooService;

        @EJB(mappedName = "java:global/stripes-enricher/business/BarServiceBean")
        private BarService barService;

        @Resource(mappedName = "java:comp/env/greeting")
        private String greeting;
        // same as
        @Resource(name = "greeting")
        private String greeting;

    }

In order for CDI injections to work, the web archive must be a bean archive. That means adding beans.xml (can be empty) to the WEB-INF directory.

## Configuration

### Maven dependency

Add Stripes Injection Enricher dependency to your project:

    <dependency>
        <groupId>com.samaxes.stripes</groupId>
        <artifactId>stripes-injection-enricher</artifactId>
        <version>VERSION</version>
    </dependency>

### Stripes configuration

Add Stripes Injection Enricher to Stripes `Extension.Packages` property:

    <init-param>
        <param-name>Extension.Packages</param-name>
        <param-value>com.samaxes.stripes.inject</param-value>
    </init-param>

## Requirements

Stripes Injection Enricher requires a Java EE 6-compliant application server providing support for JSR-299 (CDI).  
It has been tested with:

* JBoss AS 6
* GlassFish 3.1

## Licensing

This distribution is licensed under the terms of the Apache License, Version 2.0 (see LICENSE.txt).
