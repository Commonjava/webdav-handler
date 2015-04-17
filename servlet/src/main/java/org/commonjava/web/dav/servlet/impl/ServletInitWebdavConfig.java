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
package org.commonjava.web.dav.servlet.impl;

import javax.servlet.ServletConfig;

import net.sf.webdav.spi.WebdavConfig;

public class ServletInitWebdavConfig
    implements WebdavConfig
{

    public static final String GLOBAL_CONFIG_PREFIX = "dav.";

    public static final String LOCAL_ROOT_DIR = "local-root";

    public static final String LAZY_CREATE = "lazy-creation";

    public static final String OMIT_CONTENT_LENGTH = "omit-content-length";

    public static final String ALT_404_PATH = "alt-404-path";

    public static final String DEFAULT_INDEX_PATH = "default-index-path";

    private final ServletConfig servletConfig;

    public ServletInitWebdavConfig( final ServletConfig servletConfig )
    {
        this.servletConfig = servletConfig;
    }

    @Override
    public boolean isLazyFolderCreationOnPut()
    {
        return Boolean.parseBoolean( get( LAZY_CREATE, "false" ) );
    }

    @Override
    public boolean isOmitContentLengthHeaders()
    {
        return Boolean.parseBoolean( get( OMIT_CONTENT_LENGTH, "false" ) );
    }

    @Override
    public String getAlt404Path()
    {
        return get( ALT_404_PATH, null );
    }

    @Override
    public String getDefaultIndexPath()
    {
        return get( DEFAULT_INDEX_PATH, null );
    }

    protected String get( final String key, final String def )
    {
        String val = servletConfig.getInitParameter( key );
        if ( val == null )
        {
            val = servletConfig.getServletContext()
                               .getInitParameter( GLOBAL_CONFIG_PREFIX + key );
        }

        if ( val == null )
        {
            val = def;
        }

        return val;
    }
}
