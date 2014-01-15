package org.commonjava.web.dav.servlet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.sf.webdav.spi.WebdavRequest;

public class ServletWebdavRequest
    implements WebdavRequest
{

    private final HttpServletRequest req;

    public ServletWebdavRequest( final HttpServletRequest req )
    {
        this.req = req;
    }

    @Override
    public String getMethod()
    {
        return req.getMethod();
    }

    @Override
    public Principal getUserPrincipal()
    {
        return req.getUserPrincipal();
    }

    @Override
    public String getRequestURI()
    {
        return req.getRequestURI();
    }

    @Override
    public Set<String> getHeaderNames()
    {
        final ArrayList<String> list = Collections.list( req.getHeaderNames() );
        return new HashSet<String>( list );
    }

    @Override
    public String getHeader( final String name )
    {
        return req.getHeader( name );
    }

    @Override
    public Set<String> getAttributeNames()
    {
        final ArrayList<String> list = Collections.list( req.getAttributeNames() );
        return new HashSet<String>( list );
    }

    @Override
    public String getAttribute( final String name )
    {
        final Object val = req.getAttribute( name );
        return val == null ? null : val.toString();
    }

    @Override
    public Set<String> getParameterNames()
    {
        final ArrayList<String> list = Collections.list( req.getParameterNames() );
        return new HashSet<String>( list );
    }

    @Override
    public String getParameter( final String name )
    {
        return req.getParameter( name );
    }

    @Override
    public String getPathInfo()
    {
        return req.getPathInfo();
    }

    @Override
    public Locale getLocale()
    {
        return req.getLocale();
    }

    @Override
    public String getServerName()
    {
        return req.getServerName();
    }

    @Override
    public String getContextPath()
    {
        return req.getContextPath();
    }

    @Override
    public String getServletPath()
    {
        return req.getServletPath();
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        return req.getInputStream();
    }

    @Override
    public int getContentLength()
    {
        return req.getContentLength();
    }

}
