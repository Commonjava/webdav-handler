package org.commonjava.web.s3.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import net.sf.webdav.StoredObject;
import net.sf.webdav.exceptions.AccessDeniedException;
import net.sf.webdav.exceptions.ObjectAlreadyExistsException;
import net.sf.webdav.exceptions.ObjectNotFoundException;
import net.sf.webdav.exceptions.UnauthenticatedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Webdav service provider implemented via S3 bucket.
 */
public class S3Store implements IWebdavStore {

  private final AmazonS3 s3client;
  private final S3Properties s3Properties;

  private static final Logger LOG = LoggerFactory.getLogger(S3Store.class);

  public S3Store(AmazonS3 s3client, S3Properties s3Properties) {
    this.s3client = s3client;
    this.s3Properties = s3Properties;
  }

  @Override
  public ITransaction begin(Principal principal) throws WebdavException {
    LOG.debug("Begin transaction for {}", principal);
    return new S3WebdavTransaction(principal);
  }

  @Override
  public void checkAuthentication(ITransaction transaction) throws WebdavException {
    LOG.debug("Check authentication for transaction {}", transaction);
  }

  @Override
  public void commit(ITransaction transaction) throws WebdavException {
    LOG.debug("Commit transaction {}", transaction);
  }

  @Override
  public void rollback(ITransaction transaction) throws WebdavException {
    LOG.debug("Rollback transaction {}", transaction);
  }

  @Override
  public void createFolder(ITransaction transaction, final String uri) throws WebdavException {
    LOG.debug("Create folder {} at {}", uri, transaction);
    String folderUri = S3Properties.normalizeFolderUri(uri);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(0);
    try {
      this.s3client.putObject(this.s3Properties.getBucketName(), this.s3Properties.getKeyspacePrefix() + folderUri, new ByteArrayInputStream(new byte[0]), metadata);
    } catch (AmazonServiceException e) {
      throw mapAmazonServiceException(e);
    }
  }

  @Override
  public void createResource(ITransaction transaction, final String resourceUri) throws WebdavException {
    LOG.debug("Create resource {} at {}", resourceUri, transaction);
    //not needed for s3; the resource content is the same as creating the resource.
  }

  @Override
  public InputStream getResourceContent(ITransaction transaction, final String resourceUri) throws WebdavException {
    LOG.debug("Read resource {} at {}", resourceUri, transaction);
    try {
      S3Object object = this.s3client.getObject(this.s3Properties.getBucketName(), this.s3Properties.getKeyspacePrefix() + S3Properties.normalizeResourceUri(resourceUri));
      return object.getObjectContent();
    } catch (AmazonServiceException e) {
      throw mapAmazonServiceException(e);
    }
  }

  @Override
  public long setResourceContent(ITransaction transaction, final String resourceUri, InputStream content, long contentLength) throws WebdavException {
    LOG.debug("Set resource content for {} at {}", resourceUri, transaction);

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(contentLength);
    try {
      PutObjectResult result = this.s3client.putObject(this.s3Properties.getBucketName(), this.s3Properties.getKeyspacePrefix() + S3Properties.normalizeResourceUri(resourceUri), content, metadata);
      return result.getMetadata().getContentLength();
    } catch (AmazonServiceException e) {
      throw mapAmazonServiceException(e);
    }
  }

  @Override
  public String[] getChildrenNames(ITransaction transaction, final String uri) throws WebdavException {
    LOG.debug("List children names of folder {} at {}", uri, transaction);

    List<String> children = new ArrayList<>();
    String keyspacePrefix = this.s3Properties.getKeyspacePrefix();
    String folderUri = S3Properties.normalizeFolderUri(uri);
    try {
      ObjectListing objects = this.s3client.listObjects(new ListObjectsRequest(this.s3Properties.getBucketName(), keyspacePrefix + folderUri, null, "/", null));
      while (objects != null) {
        for (String folder : objects.getCommonPrefixes()) {
          children.add(folder.substring(keyspacePrefix.length() + folderUri.length()));
        }

        for (S3ObjectSummary object : objects.getObjectSummaries()) {
          String name = object.getKey().substring(keyspacePrefix.length());
          if (name.startsWith(folderUri)) {
            name = name.substring(folderUri.length());
          }
          if (!name.isEmpty()) {
            children.add(name);
          }
        }

        if (objects.isTruncated()) {
          objects = this.s3client.listNextBatchOfObjects(objects);
        } else {
          objects = null;
        }
      }

      return children.toArray(new String[children.size()]);
    } catch (AmazonServiceException e) {
      throw mapAmazonServiceException(e);
    }
  }

  @Override
  public long getResourceLength(ITransaction transaction, String resourceUri) throws WebdavException {
    LOG.debug("Get resource length for {} at {}", resourceUri, transaction);
    try {
      ObjectMetadata objectMetadata = this.s3client.getObjectMetadata(this.s3Properties.getBucketName(), this.s3Properties.getKeyspacePrefix() + S3Properties.normalizeResourceUri(resourceUri));
      return objectMetadata.getContentLength();
    } catch (AmazonServiceException e) {
      throw mapAmazonServiceException(e);
    }
  }

  @Override
  public void removeObject(ITransaction transaction, String uri) throws WebdavException {
    LOG.debug("Remove resource {} at {}", uri, transaction);
    try {
      this.s3client.deleteObject(this.s3Properties.getBucketName(), this.s3Properties.getKeyspacePrefix() + S3Properties.normalizeResourceUri(uri));
    } catch (AmazonServiceException e) {
      throw mapAmazonServiceException(e);
    }
  }

  @Override
  public StoredObject getStoredObject(ITransaction transaction, String uri) throws WebdavException {
    LOG.debug("Get stored object {} at {}", uri, transaction);
    try {
      if (uri.isEmpty() || uri.endsWith("/")) {
        //if it ends in a '/', assume it's a folder:
        return readFolderObject(uri);
      } else {
        return readResourceObject(uri);
      }
    } catch (AmazonServiceException e) {
      throw mapAmazonServiceException(e);
    }
  }

  StoredObject readResourceObject(final String uri) {
    try {
      ObjectMetadata objectMetadata = this.s3client.getObjectMetadata(this.s3Properties.getBucketName(), this.s3Properties.getKeyspacePrefix() + S3Properties.normalizeResourceUri(uri));
      StoredObject result = new StoredObject();
      result.setFolder(false);
      result.setResourceLength(objectMetadata.getContentLength());
      result.setLastModified(objectMetadata.getLastModified());
      result.setCreationDate(objectMetadata.getLastModified());
      return result;
    } catch (AmazonServiceException e) {
      if (e.getStatusCode() == 404) {
        //if the resource isn't there, check to make sure it's there as a folder.
        return readFolderObject(uri);
      }
      throw e;
    }
  }

  StoredObject readFolderObject(final String uri) {
    ObjectListing objects = this.s3client.listObjects(this.s3Properties.getBucketName(), this.s3Properties.getKeyspacePrefix() + S3Properties.normalizeFolderUri(uri));

    if (objects.getCommonPrefixes().isEmpty() && objects.getObjectSummaries().isEmpty()) {
      return null; //looks like there's no directory there.
    }

    StoredObject result = new StoredObject();

    long length = 0;
    long earliest = System.currentTimeMillis();
    for (S3ObjectSummary summary : objects.getObjectSummaries()) {
      length = Math.max(length, summary.getSize());
      earliest = Math.min(earliest, summary.getLastModified().getTime());
    }

    result.setFolder(true);
    result.setResourceLength(length);
    result.setLastModified(new Date(earliest));
    result.setCreationDate(result.getLastModified());
    return result;
  }

  WebdavException mapAmazonServiceException(AmazonServiceException e) {
    WebdavException target;
    switch (e.getStatusCode()) {
      case 404:
        target = new ObjectNotFoundException();
        break;
      case 403:
        target = new AccessDeniedException();
        break;
      case 401:
        target = new UnauthenticatedException();
        break;
      case 409:
        target = new ObjectAlreadyExistsException();
        break;
      default:
        target = new WebdavException(e.getMessage(), e);
        break;
    }

    LOG.debug(e.getMessage(), target);

    return target;
  }
}
