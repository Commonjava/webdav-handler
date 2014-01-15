package org.commonjava.web.dav.servlet.impl;

import javax.servlet.ServletContext;

import net.sf.webdav.spi.IMimeTyper;

public class ServletMimeTyper
    implements IMimeTyper
{

    private final ServletContext servletContext;

    public ServletMimeTyper( final ServletContext servletContext )
    {
        this.servletContext = servletContext;
    }

    @Override
    public String getMimeType( final String path )
    {
        return servletContext.getMimeType( path );
    }

}
