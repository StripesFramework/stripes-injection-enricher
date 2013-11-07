# Stripes Injection Enricher

Stripes Injection Enricher enriches [Stripes Framework](http://www.stripesframework.org/) objects by satisfying injection points specified declaratively using annotations.  
There are three injection-based enrichers provided by Stripes Injection Enricher out of the box:

* `@Resource` - Java EE resource injections
* `@EJB` - EJB session bean reference injections
* `@Inject` - CDI injections

The first two enrichers use JNDI to lookup the instance to inject.  
The CDI injections are handled by treating the Stripes object as a bean capable of receiving standard CDI injections.

The `@Resource` annotation gives you access to any object which is available via JNDI.  
It follows the standard rules for `@Resource` (as defined in the Section 2.3 of the Common Annotations for the Java Platform specification).

The `@EJB` annotation performs a JNDI lookup for the EJB session bean reference using EJB 3.1 portable global JNDI names.  
If no matching beans were found in those locations the injection will fail.

However, you can manually set the JNDI name to lookup using the `mappedName` attribute for `@EJB`, as well as the attributes `mappedName` and `name` attributes for `@Resource`:

```java
public class MyActionBean implements ActionBean {

    @Inject
    private FooService fooService;

    @EJB
    // or manually @EJB(mappedName = "java:global[/<app-name>]/<module-name>/BarService")
    private BarService barService;

    @Resource(name = "greeting")
    // same as @Resource(mappedName = "java:comp/env/greeting")
    private String greeting;

}
```

**Note:** In order for CDI injections to work, the web archive must be a bean archive. That means adding a `beans.xml` file (can be empty) to the `WEB-INF` directory.

## Configuration

### Maven dependency

Add Stripes Injection Enricher dependency to your project:

```xml
<dependency>
    <groupId>com.samaxes.stripes</groupId>
    <artifactId>stripes-injection-enricher</artifactId>
    <version>VERSION</version>
</dependency>
```

### Stripes filter configuration

Add Stripes Injection Enricher to Stripes filter `Extension.Packages` configuration in `web.xml`:

```xml
<init-param>
    <param-name>Extension.Packages</param-name>
    <param-value>com.samaxes.stripes.inject</param-value>
</init-param>
```

## Requirements

Stripes Injection Enricher requires a Java EE 6-compliant application server providing support for JSR-299 (CDI).  
It has been tested with the following application servers:

* GlassFish 3.1
* JBoss AS 7
* JBoss AS 6

## License

This distribution is licensed under the terms of the Apache License, Version 2.0 (see LICENSE.txt).
