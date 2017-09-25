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
package net.sf.webdav.impl;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import net.sf.webdav.spi.WebdavConfig;

@Alternative
@Named
public class SimpleWebdavConfig
    implements WebdavConfig
{

    private boolean lazyCreate;

    private boolean omitContentLength;

    private String alt404;

    private String defaultIndex;

    public SimpleWebdavConfig withLazyFolderCreationOnPut()
    {
        this.lazyCreate = true;
        return this;
    }

    public SimpleWebdavConfig withoutLazyFolderCreationOnPut()
    {
        this.lazyCreate = false;
        return this;
    }

    public SimpleWebdavConfig withOmitContentLengthHeader()
    {
        this.omitContentLength = true;
        return this;
    }

    public SimpleWebdavConfig withoutOmitContentLengthHeader()
    {
        this.omitContentLength = false;
        return this;
    }

    public SimpleWebdavConfig withAlt404Path( final String path )
    {
        this.alt404 = path;
        return this;
    }

    public SimpleWebdavConfig withoutAlt404Path()
    {
        this.alt404 = null;
        return this;
    }

    public SimpleWebdavConfig withDefaultIndex( final String path )
    {
        this.defaultIndex = path;
        return this;
    }

    public SimpleWebdavConfig withoutDefaultIndex()
    {
        this.defaultIndex = null;
        return this;
    }

    @Override
    public boolean isLazyFolderCreationOnPut()
    {
        return lazyCreate;
    }

    @Override
    public boolean isOmitContentLengthHeaders()
    {
        return omitContentLength;
    }

    @Override
    public String getAlt404Path()
    {
        return alt404;
    }

    @Override
    public String getDefaultIndexPath()
    {
        return defaultIndex;
    }

}
