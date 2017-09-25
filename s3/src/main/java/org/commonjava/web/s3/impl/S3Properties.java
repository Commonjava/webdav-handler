/**
 * Copyright (C) 2006-2017 Apache Software Foundation (https://sourceforge.net/p/webdav-servlet, https://github.com/Commonjava/webdav-handler)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
