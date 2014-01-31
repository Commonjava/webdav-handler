package org.commonjava.web.vertx.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Locale;
import java.util.Set;

import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.util.RequestUtil;

import org.commonjava.util.logging.Logger;
import org.commonjava.web.vertx.util.VertXInputStream;
import org.vertx.java.core.http.HttpServerRequest;

public class VertXWebdavRequest
    implements WebdavRequest
{

    private static final String CONTENT_LENGTH = "Content-Length";

    private static final String ACCEPT_LANGUAGE = "Accept-Language";

    private final Logger logger = new Logger( getClass() );

    private final HttpServerRequest request;

    private final String contextPath;

    private final Principal userPrincipal;

    private final String serviceTopPath;

    private final String serviceSubPath;

    private URI requestUri;

    public VertXWebdavRequest( final HttpServerRequest request, final String contextPath, final String serviceTopPath, final String serviceSubPath,
                               final Principal userPrincipal )
        throws WebdavException
    {
        this.request = request;
        this.serviceTopPath = serviceTopPath;
        this.contextPath = contextPath;
        this.serviceSubPath = serviceSubPath;
        this.userPrincipal = userPrincipal;

        final String hostHeader = request.headers()
                                         .get( "Host" );

        try
        {
            String hostAndPort = request.absoluteURI()
                                        .getHost();

            final int port = request.absoluteURI()
                                    .getPort();
            if ( port != 80 && port != 443 )
            {
                hostAndPort += ":" + port;
            }

            final String uri = request.absoluteURI()
                                      .toString();

            final int idx = uri.indexOf( hostAndPort );

            final StringBuilder sb = new StringBuilder();
            sb.append( uri.substring( 0, idx ) );
            sb.append( hostHeader );
            sb.append( uri.substring( idx + hostAndPort.length() ) );

            this.requestUri = new URI( sb.toString() );
        }
        catch ( final URISyntaxException e )
        {
            throw new WebdavException( "Failed to construct requestUri: %s", e, e.getMessage() );
        }
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
        return requestUri.toString();
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
                      .get( "q:" + name );
    }

    @Override
    public String getPathInfo()
    {
        logger.info( "Path-Info: '%s'", serviceSubPath );
        return serviceSubPath;
    }

    @Override
    public Locale getLocale()
    {
        return RequestUtil.parseLocale( getHeader( ACCEPT_LANGUAGE ) );
    }

    @Override
    public String getServerName()
    {
        logger.info( "Server-Name: '%s'", request.absoluteURI()
                                                 .getHost() );
        return requestUri.getHost();
    }

    @Override
    public String getContextPath()
    {
        logger.info( "Context-Path: '%s'", contextPath );
        return contextPath;
    }

    @Override
    public String getServicePath()
    {
        logger.info( "Service-Path: '%s'", serviceTopPath );
        return serviceTopPath;
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
