package org.commonjava.web.s3.impl;

import java.security.Principal;
import java.util.UUID;

import net.sf.webdav.spi.ITransaction;

/**
 * @author Ryan Heaton
 */
public class S3WebdavTransaction implements ITransaction {

  private final String id;
  private final Principal principal;

  public S3WebdavTransaction(Principal principal) {
    this.id = UUID.randomUUID().toString();
    this.principal = principal;
  }

  @Override
  public Principal getPrincipal() {
    return this.principal;
  }

  @Override
  public String toString() {
    return "S3WebdavTransaction{id='" + id + "', principal=" + principal + '}';
  }
}
