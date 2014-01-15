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
