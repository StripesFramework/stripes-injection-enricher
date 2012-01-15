# Stripes Injection Enricher

## 1.0.2

* Updated Arquillian to version 1.0.0.CR7.
* Integration tests for JBoss AS 7.

## 1.0.1

* Lookup of CDI BeanManager is performed only if the annotation @Inject is present.

## 1.0

* Support for javax.ejb.EJB, javax.inject.Inject and javax.annotation.Resource standard Java EE annotations.
* Integration tests for JBoss AS 6 and Glassfish 3.1.
* Stripes 1.5.6 dependency. Previous versions of Stripes VFS had issues with JBoss AS 6.
