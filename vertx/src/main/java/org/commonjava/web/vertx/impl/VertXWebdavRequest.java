package org.commonjava.web.vertx.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Locale;
import java.util.Set;

import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.util.RequestUtil;

import org.commonjava.web.vertx.util.VertXInputStream;
import org.vertx.java.core.http.HttpServerRequest;

public class VertXWebdavRequest
    implements WebdavRequest
{

    private static final String CONTENT_LENGTH = "Content-Length";

    private static final String ACCEPT_LANGUAGE = "Accept-Language";

    private final HttpServerRequest request;

    private final String contextPath;

    private final Principal userPrincipal;

    private final String serviceSubpath;

    public VertXWebdavRequest( final HttpServerRequest request, final String contextPath, final String serviceSubpath, final Principal userPrincipal )
    {
        this.request = request;
        this.serviceSubpath = serviceSubpath;
        this.contextPath = contextPath;
        this.userPrincipal = userPrincipal;
    }

    @Override
    public String getMethod()
    {
        return request.method();
    }

    @Override
    public Principal getUserPrincipal()
    {
        return userPrincipal;
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
        // TODO Implement this for debugging in WebdavService...
        return null;
    }

    @Override
    public String getAttribute( final String name )
    {
        // TODO Implement this for RequestDispatcher.include() equivalent support...
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
        return RequestUtil.parseLocale( getHeader( ACCEPT_LANGUAGE ) );
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
        return contextPath;
    }

    @Override
    public String getServicePath()
    {
        return serviceSubpath;
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        return new VertXInputStream( request );
    }

    @Override
    public int getContentLength()
    {
        final String val = getHeader( CONTENT_LENGTH );
        return val == null ? -1 : Integer.parseInt( val );
    }

}
