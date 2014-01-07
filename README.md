# WebDAV-Handler

## What is it?

A generic service that brings basic WebDAV access to any store. Only 1 interface 
(IWebdavStorage) has to be implemented, an example (LocalFileSystemStorage)
which uses the local filesystem, is provided.
  
**NOTE:** Additionally, a generic request/response SPI must be implemented to 
integrate with APIs like servlets or Vert.x. A few such adapters will be provided.
  
Unlike large systems (like slide), this servlet only supports the most basic
data access options. versioning or user management are not supported
  
## REQUIREMENTS

  JDK 1.6 or above

## INSTALLATION & CONFIGURATION

## Notes on Forking Webdav-Servlet

This codebase was forked from http://sourceforge.net/p/webdav-servlet/code/HEAD/tree/tags/Release_2.0.1/

The dependency on servlet-api was removed, all references to that api were re-stubbed into local SPI interfaces, and the implementation was repaired. The plan moving forward is to re-implement adapters for servlet-api and vert.x (at least) so they integrate with the new SPI.

## CREDITS

We want to thank Remy Maucherat for the original webdav-servlet
and the dependent files that come with tomcat,
and Oliver Zeigermann for the slide-WCK. Our IWebdavStorage class is modeled
after his BasicWebdavStore.
