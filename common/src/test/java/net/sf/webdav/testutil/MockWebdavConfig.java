/**
 * Copyright (C) 2006 Apache Software Foundation (jdcasey@commonjava.org)
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
package net.sf.webdav.testutil;

import net.sf.webdav.spi.WebdavConfig;

public class MockWebdavConfig
    implements WebdavConfig
{

    private boolean lazyFolderCreationOnPut;

    private boolean omitContentLengthHeaders;

    private String alt404Path;

    private String defaultIndexPath;

    @Override
    public boolean isLazyFolderCreationOnPut()
    {
        return lazyFolderCreationOnPut;
    }

    @Override
    public boolean isOmitContentLengthHeaders()
    {
        return omitContentLengthHeaders;
    }

    @Override
    public String getAlt404Path()
    {
        return alt404Path;
    }

    @Override
    public String getDefaultIndexPath()
    {
        return defaultIndexPath;
    }

    public void setLazyFolderCreationOnPut( final boolean lazyFolderCreationOnPut )
    {
        this.lazyFolderCreationOnPut = lazyFolderCreationOnPut;
    }

    public void setOmitContentLengthHeaders( final boolean omitContentLengthHeaders )
    {
        this.omitContentLengthHeaders = omitContentLengthHeaders;
    }

    public void setAlt404Path( final String alt404Path )
    {
        this.alt404Path = alt404Path;
    }

    public void setDefaultIndexPath( final String defaultIndexPath )
    {
        this.defaultIndexPath = defaultIndexPath;
    }

}
