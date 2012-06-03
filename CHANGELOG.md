# Stripes Injection Enricher

## 1.0.2

* Support for EJB 3.1 portable global JNDI names.
* Integration tests for JBoss AS 7.
* Moved from TestNG to JUnit (better integration with Arquillian).
* Updated Arquillian to version 1.0.1.Final.
* Updated Stripes Framework to 1.5.7.

## 1.0.1

* Lookup of CDI BeanManager is performed only if the annotation @Inject is present.

## 1.0

* Support for javax.ejb.EJB, javax.inject.Inject and javax.annotation.Resource standard Java EE annotations.
* Integration tests for JBoss AS 6 and Glassfish 3.1.
* Stripes 1.5.6 dependency. Previous versions of Stripes VFS had issues with JBoss AS 6.
