package org.commonjava.web.vertx.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Locale;
import java.util.Set;

import net.sf.webdav.spi.WebdavRequest;

import org.vertx.java.core.http.HttpServerRequest;

public class VertXWebdavRequest
    implements WebdavRequest
{

    private final HttpServerRequest request;

    public VertXWebdavRequest( final HttpServerRequest request )
    {
        this.request = request;
    }

    @Override
    public String getMethod()
    {
        return request.method();
    }

    @Override
    public Principal getUserPrincipal()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRequestURI()
    {
        return request.absoluteURI()
                      .toString();
    }

    @Override
    public Set<String> getHeaderNames()
    {
        return request.headers()
                      .names();
    }

    @Override
    public String getHeader( final String name )
    {
        return request.headers()
                      .get( name );
    }

    @Override
    public Set<String> getAttributeNames()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttribute( final String name )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getParameterNames()
    {
        return request.params()
                      .names();
    }

    @Override
    public String getParameter( final String name )
    {
        return request.params()
                      .get( name );
    }

    @Override
    public String getPathInfo()
    {
        return request.path();
    }

    @Override
    public Locale getLocale()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServerName()
    {
        return request.absoluteURI()
                      .getHost();
    }

    @Override
    public String getContextPath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServletPath()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getContentLength()
    {
        // TODO Auto-generated method stub
        return 0;
    }

}
