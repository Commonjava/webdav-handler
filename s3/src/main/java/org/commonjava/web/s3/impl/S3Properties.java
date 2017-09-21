package org.commonjava.web.s3.impl;

/**
 * Properties carrying the S3 bucket name and (optional) prefix.
 *
 * @author Ryan Heaton
 */
public class S3Properties {

  private final String bucketName;

  private final String keyspacePrefix;

  public S3Properties(String bucketName) {
    this(bucketName, "");
  }

  public S3Properties(String bucketName, String keyspacePrefix) {
    this.bucketName = bucketName;
    this.keyspacePrefix = keyspacePrefix == null ? "" : normalizeFolderUri(keyspacePrefix);
  }

  public String getBucketName() {
    return bucketName;
  }

  public String getKeyspacePrefix() {
    return keyspacePrefix;
  }

  static String normalizeFolderUri(final String folderUri) {
    String result = normalizeResourceUri(folderUri);

    if (!result.isEmpty() && !result.endsWith("/")) {
      //make sure the folder has a slash at the end.
      result = result + "/";
    }
    return result;
  }

  static String normalizeResourceUri(final String resourceUri) {
    //strip the first slash.
    String result = resourceUri;
    while (!result.isEmpty() && result.charAt(0) == '/') {
      result = result.substring(1);
    }

    //take out any double slashes (bug in the webdav servlet).
    return result.replaceAll("//", "/");
  }

}
