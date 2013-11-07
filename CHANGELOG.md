# Stripes Injection Enricher

## 1.0.3

* Update Arquillian dependency to 1.1.1.Final and Shrinkwrap Resolver to 2.0.0.
* Update project URLs to point to the [StripesFramework](https://github.com/StripesFramework) organization.

## 1.0.2

* Support for EJB 3.1 portable global JNDI names.
* Integration tests for JBoss AS 7.
* Move from TestNG to JUnit which has better integration with Arquillian.
* Update Arquillian dependency to 1.0.1.Final.
* Update Stripes Framework dependency to 1.5.7.

## 1.0.1

* Lookup of CDI BeanManager is performed only if the `@Inject` annotation is present.

## 1.0

* Support for `javax.ejb.EJB`, `javax.inject.Inject` and `javax.annotation.Resource` standard Java EE annotations.
* Integration tests for JBoss AS 6 and Glassfish 3.1.
* Stripes 1.5.6 dependency. Previous versions of Stripes VFS had issues with JBoss AS 6.
